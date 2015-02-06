package crawlers;

import crawlerUtils.Filter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import dbUtils.DatabaseUtils;
import crawlerUtils.LangDetect;
import com.cybozu.labs.langdetect.LangDetectException;
import com.google.gson.Gson;
import model.Metrics;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.Job;
import model.Field;

public class JobnetCrawler {

    // declare variables
    private static boolean danish = false;

    //--- metrics
    private final Metrics METRICS;
    int allJobs;
    int jobsInEnglish;
    int duplicateJobs;
    int exceptions;
    //----

    private String URL;
    private String area;
    private String jobCategory;
    private String foundAt;
    private LangDetect languageDetector;
    private Filter filter;
    private DatabaseUtils dbUtils;

    private Gson gson;

    // constructor
    public JobnetCrawler(String URL, String area, String jobCategory, String foundAt, LangDetect languageDetector) throws IOException {
        gson = new Gson();

        this.URL = URL;
        this.area = area;
        this.jobCategory = jobCategory;
        this.foundAt = foundAt;
        this.languageDetector = languageDetector;

        this.allJobs = 0;
        this.jobsInEnglish = 0;
        this.duplicateJobs = 0;
        this.exceptions = 0;

        filter = new Filter();
        METRICS = new Metrics("JobnetPage");
        dbUtils = new DatabaseUtils();

        try {
            setTrustAllCerts();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            exceptions++;
        }
    }

    public Metrics scan() {
        // connect to URL
        String cleanURL = URL.replaceAll("ø", "%C3%B8").replaceAll("å", "%C3%A5").replaceAll("æ", "%C3%A6");
        //ignore content type to get the json
        Document doc = new Document("");
        try {
            doc = Jsoup.connect(cleanURL).ignoreContentType(true).timeout(20000).followRedirects(true).get();
//            System.out.println("############### NEW CRAWLER");
//            System.out.println("############ Connected to: " + cleanURL);
//            System.out.println("");
        } catch (IOException e) {
            e.printStackTrace(System.out);
            exceptions++;
            Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, e);
        }

        
        ArrayList<Job> jobs = new ArrayList<>();
        String reply = doc.text();
        jsonReply r = gson.fromJson(reply, jsonReply.class);

        allJobs += Integer.parseInt(r.NumberOfMatches);

        for (jsonJob j : r.JobPostingDigests) {
            try {
                if (languageDetector.detect(j.Body.toLowerCase()).equalsIgnoreCase("en")) {
                    System.out.println("_________________________________________________");
                    // Open connection to joblink
                    String jobURL = "https://job.jobnet.dk/CV/FindJob/details/" + j.Id;
                    try {
                        Document internalDocument = null;
//                        System.out.println(jobURL);
                        internalDocument = Jsoup.connect(jobURL).timeout(20000).get();
                        Elements internalDocumentBody = internalDocument.select("body");

                        if (!internalDocumentBody.text().trim().isEmpty()) {
                            String jobText = internalDocument.select("div#DetailsJobDescription").text();
                            if (!jobText.equals("")) {//if we found content in ad
                                if (languageDetector.detect(jobText).equalsIgnoreCase("en")) {// and if is english
                                    danish = false;
                                } else {
                                    danish = true;
                                }
                                
                                // check if ad required danish even if its written in English
                                if (filter.checkIfRequiresDanish(jobText)) {
//                                    System.out.println("Danish requirement:" + internalDocument.baseUri());
                                    danish = true;
                                }
                                
                                System.out.println("¤¤¤¤¤¤ " + j.HiringOrgName);
                                //dont take any vestas jobs. The url is jobnet url, and they format the title in a way
                                //that doenst allow duplicate filtering. I get Vestas from the other sites anyways
                                //get job title
                                String company = j.HiringOrgName;
                                if (company.toLowerCase().contains("vestas")) {
                                    danish = true;
                                    System.out.println("¤¤¤¤¤¤ FOUND");
                                }

                                if (danish == false) {
                                    // job is what we are looking for
//                                    System.out.println("LANGUAGE IS ENGLISH " + jobText);
//                                    System.out.println(" ");

                                    // get info that we need
                                    //get title and company
                                    String jobTitle = j.Headline;
                                    String jobAnnouncer = j.HiringOrgName;
                                    //get publication date
                                    String datePublished = j.PostingCreated;
//                                    System.out.println(datePublished);
                                    Date jobDate;
                                    try {
                                        jobDate = new SimpleDateFormat("dd-MM-yyyy", new Locale("da", "DK")).parse(datePublished);
                                    } catch (ParseException e) {
                                        exceptions++;
                                        e.printStackTrace(System.out);
                                        jobDate = new Date();//TODO put last used date
//                                        System.out.println("HERETO");
                                    }

                                    //sout info and add it to arrayList
//                                    System.out.println("job date: " + jobDate);
//                                    System.out.println("Job Title: " + jobTitle);
//                                    System.out.println("Job Announcer: " + jobAnnouncer);
//                                    System.out.println("Small text: " + jobText);
//                                    System.out.println("Ad url: " + jobURL);
//                                    System.out.println(" ");
                                    ArrayList fields = new ArrayList();

                                    jobTitle = filter.homogeniseJobTitle(jobTitle);
                                    jobAnnouncer = filter.homogeniseCompanyName(jobAnnouncer);
                                    jobURL = filter.homogeniseURL(jobURL);
                                    Job identifiedJob = new Job(jobTitle, jobAnnouncer, new URL(jobURL), jobDate, jobText, 1, area, foundAt, fields);

                                    Field f = new Field(jobCategory);
                                    identifiedJob.addField(f);

                                    identifiedJob = filter.filterTitle(identifiedJob, jobCategory, jobTitle);

                                    jobs.add(identifiedJob);
                                    jobsInEnglish++;

                                } else {
//                                    System.out.println("LANGUAGE NOT ENGLISH " + jobText);
//                                    System.out.println(" ");
                                }
                            } else {
//                                System.out.println("NO CONTENT IN AD");
                            }
                            System.out.println("_________________________________________________");
                        }
                    } catch (IOException e) {
                        exceptions++;
//                        e.printStackTrace(System.out);
                    }
                } else {
//                    System.out.println("LANGUAGE NOT ENGLISH " + j.Body);
//                    System.out.println(" ");
                }
            } catch (LangDetectException ex) {
                exceptions++;
                ex.printStackTrace(System.out);
                Logger.getLogger(JobnetCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        // save Jobs
        duplicateJobs += dbUtils.deleteDuplicatesAndAddtoDB(jobs);
        //set metrics to return
        METRICS.setAllJobs(allJobs);
        METRICS.setJobsInEnglish(jobsInEnglish);
        METRICS.setDuplicateJobs(duplicateJobs);
        METRICS.setExceptions(exceptions);
        return METRICS;
    }

    //BEGIN ********* Utility calsses for JSON //*********
    class jsonReply {

        //variables
        String NumberOfMatches;
        jsonBoundingBoxOfJobPostingLocations BoundingBoxOfJobPostingLocations;
        jsonJob[] JobPostingDigests;

        //constructor
        //methods
        @Override
        public String toString() {
            return ">>> " + NumberOfMatches + " " + BoundingBoxOfJobPostingLocations + " " + JobPostingDigests;
        }
    }

    class jsonBoundingBoxOfJobPostingLocations {

        //variables
        jsonSouthWestCorner SouthWestCorner;
        jsonNorthEastCorner NorthEastCorner;

        //constructor
        //methods
    }

    class jsonSouthWestCorner {

        //variables
        Object Latitude;
        Object Longitude;

        //constructor
        //methods
    }

    class Location {

        //variables
        Object Latitude;
        Object Longitude;

        //constructor
        //methods
    }

    class jsonNorthEastCorner {

        //variables
        Object Latitude;
        Object Longitude;

        //constructor
        //methods
    }

    class jsonJob {

        //variables
        String Id;
        String Headline;
        String Body;
        String WeeklyWorkTime;
        String ApplicationDeadline;
        String PostingCreated;
        String PostalCode;
        String PostalDistrict;
        boolean InternalJpp;
        Location Location;
        String WorkLocation;
        String HiringOrgName;
        String DiscoAmsCode;
        String DiscoAmsName;
        boolean AnonymousEmployer;

        //constructor
        public jsonJob(String Id, String Headline, String Body, String WeeklyWorkTime, String ApplicationDeadline, String PostingCreated, String PostalCode, String PostalDistrict, boolean InternalJpp, Location Location, String WorkLocation, String HiringOrgName, String DiscoAmsCode, String DiscoAmsName, boolean AnonymousEmployer) {
            this.Id = Id;
            this.Headline = Headline;
            this.Body = Body;
            this.WeeklyWorkTime = WeeklyWorkTime;
            this.ApplicationDeadline = ApplicationDeadline;
            this.PostingCreated = PostingCreated;
            this.PostalCode = PostalCode;
            this.PostalDistrict = PostalDistrict;
            this.InternalJpp = InternalJpp;
            this.Location = Location;
            this.WorkLocation = WorkLocation;
            this.HiringOrgName = HiringOrgName;
            this.DiscoAmsCode = DiscoAmsCode;
            this.DiscoAmsName = DiscoAmsName;
            this.AnonymousEmployer = AnonymousEmployer;
        }

        //methods
        @Override
        public String toString() {
            return this.Id + ", " + this.Headline + ", " + this.Body + ", " + this.WeeklyWorkTime + ", " + this.ApplicationDeadline + ", " + this.PostingCreated + ", "
                    + this.PostalCode + ", " + this.PostalDistrict + ", " + this.InternalJpp + ", " + this.Location + ", " + this.WorkLocation
                    + ", " + this.HiringOrgName + ", " + this.DiscoAmsCode + ", " + this.DiscoAmsName + ", " + this.AnonymousEmployer;
        }

    }
    //STOP ********* Utility classes for JSON //*********

    private void setTrustAllCerts() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            // We can not recover from this exception.
            e.printStackTrace(System.out);
            exceptions++;
        }
    }

}
