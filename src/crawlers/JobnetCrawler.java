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
    private final Metrics METRICS;

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

        filter = new Filter();
        METRICS = new Metrics("JobnetPage");
        dbUtils = new DatabaseUtils();

        try {
            setTrustAllCerts();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            METRICS.incrementExceptions();
        }
    }

    public Metrics scan() {
        
        // connect to URL
        String cleanURL = URL.replaceAll("ø", "%C3%B8")
                .replaceAll("å", "%C3%A5")
                .replaceAll("æ", "%C3%A6");
        Document doc = new Document("");
        try {
            //ignore content type to get the json
            doc = Jsoup.connect(cleanURL)
                    .ignoreContentType(true)
                    .timeout(20000)
                    .followRedirects(true)
                    .get();
                    // System.out.println("############### NEW CRAWLER");
        } catch (IOException e) {
            e.printStackTrace(System.out);
            METRICS.incrementExceptions();
            Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, e);
        }

        ArrayList<Job> jobs = new ArrayList<>();
        String reply = doc.text();
        jsonReply r = gson.fromJson(reply, jsonReply.class);

        int allJobs = Integer.parseInt(r.NumberOfMatches);
        METRICS.setAllJobs(METRICS.getAllJobs() + allJobs);
        
        for (jsonJob j : r.JobPostingDigests) {
            try {
                if (languageDetector.detect(j.Body.toLowerCase()).equalsIgnoreCase("en")) {
                    System.out.println("_________________________________________________");
                    // Open connection to joblink
                    String jobURL = "https://job.jobnet.dk/CV/FindJob/details/" + j.Id;
                    try {
                        Document internalDocument = null;
                        internalDocument = Jsoup.connect(jobURL)
                                .timeout(20000)
                                .get();
                        Elements internalDocumentBody = internalDocument.select("body");

                        if (!internalDocumentBody.text().trim().isEmpty()) {
                            String jobText = internalDocument.select("div#DetailsJobDescription").text();
                            
                            //if we found content in ad
                            if (!jobText.equals("")) {
                                if (languageDetector.detect(jobText).equalsIgnoreCase("en")) {// and if is english
                                    danish = false;
                                } else {
                                    danish = true;
                                }

                                // check if ad required danish even if its written in English
                                if (filter.checkIfRequiresDanish(jobText)) {
                                    danish = true;
                                }

                                //dont take any vestas jobs. The url is jobnet url, and they format the title in a way
                                //that doenst allow duplicate filtering. I get Vestas from the other sites anyways.
                                
                                //get job title
                                String company = j.HiringOrgName;
                                if (company.toLowerCase().contains("vestas")) {
                                    danish = true;
                                    System.out.println("¤¤¤¤¤¤ FOUND");
                                }

                                if (danish == false) {
                                    // get info that we need
                                    String jobTitle = j.Headline;
                                    String jobAnnouncer = j.HiringOrgName;
                                    String datePublished = j.PostingCreated;
                                    
                                    Date jobDate;
                                    try {
                                        jobDate = new SimpleDateFormat("dd-MM-yyyy", new Locale("da", "DK")).parse(datePublished);
                                    } catch (ParseException e) {
                                        METRICS.incrementExceptions();
                                        e.printStackTrace(System.out);
                                        //TODO put last used date
                                        jobDate = new Date();
                                    }
                                    
                                    System.out.println("_________________________________________________");
                                    
                                    //temporarily set text as ""
                                    jobText = "";

                                    Job identifiedJob = new Job(jobTitle, jobAnnouncer, new URL(jobURL), jobDate, jobText, 1, area, foundAt, new ArrayList());

                                    Field f = new Field(jobCategory);
                                    identifiedJob.addField(f);

                                    identifiedJob = filter.filterTitleForPossibleChangeInFields(identifiedJob, jobCategory, jobTitle);

                                    jobs.add(identifiedJob);
                                    METRICS.incrementJobsInEnglish();
                                } else {
//                                    System.out.println("LANGUAGE NOT ENGLISH " + jobText);
                                }
                            } else {
//                                System.out.println("NO CONTENT IN AD");
                            }
                        }
                    } catch (IOException e) {
                        METRICS.incrementExceptions();
                        e.printStackTrace(System.out);
                    }
                } else {
//                    System.out.println("LANGUAGE NOT ENGLISH " + j.Body);
                }
            } catch (LangDetectException ex) {
                METRICS.incrementExceptions();
                ex.printStackTrace(System.out);
                Logger.getLogger(JobnetCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        // save Jobs
        int duplicateJobs = dbUtils.deleteDuplicatesAndAddtoDB(jobs);
        METRICS.setDuplicateJobs(METRICS.getDuplicateJobs() + duplicateJobs);
        
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
            METRICS.incrementExceptions();
        }
    }

}
