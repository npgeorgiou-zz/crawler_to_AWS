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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import dbUtils.DatabaseUtils;
import crawlerUtils.LangDetect;
import com.cybozu.labs.langdetect.LangDetectException;
import model.Metrics;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Job;
import model.Field;

public class JobbankCrawler {

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
    // constructor

    public JobbankCrawler(String URL, String area, String jobCategory, String foundAt, LangDetect languageDetector) throws IOException {
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
        METRICS = new Metrics("JobbankPage");
        dbUtils = new DatabaseUtils();

        try {
            setTrustAllCerts();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public Metrics scan() {
        // connect to URL
        URL = URL.replaceAll("ø", "%C3%B8").replaceAll("å", "%C3%A5").replaceAll("æ", "%C3%A6");

        Document doc = new Document("");
        try {
            doc = Jsoup.connect(URL).timeout(20000).followRedirects(true).get();
//            System.out.println("############### NEW CRAWLER");
            System.out.println("############ Connected to: " + URL);
//            System.out.println("");
        } catch (IOException e) {
            e.printStackTrace(System.out);
            exceptions++;
            Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, e);
        }

        while (true) {

            try {
                // read page and return arrayList of paid Jobs
                ArrayList<Job> paidJobs = scanDocForPaid(doc, area, foundAt);
                // save Jobs
                duplicateJobs += dbUtils.deleteDuplicatesAndAddtoDB(paidJobs);
//                dbUtils.addToDatabase();
            } catch (ParseException pe) {
                exceptions++;
                pe.printStackTrace(System.out);
            }
            // connect to next pages, if there are any
            try {
                Element paginationSpan = doc.getElementsByClass("resultPageNumbers").first();
                Elements paginationLinks = null;
                try {// need a try here because there are some cat/places with no jobs, therefore no pagination
                    paginationLinks = paginationSpan.select("a[href]");

                    if (paginationLinks.size() == 1) {// it is only this page
//                        System.out.println("this is the last page");
                        break;
                    } else {// we have other pages                   
                        Element currentPage = paginationSpan.select("a.currentPage").first();
                        int index = paginationLinks.indexOf(currentPage);
//                        System.out.println(paginationLinks.size() + " " + index);
                        if (paginationLinks.size() - 1 == index) {
//                            System.out.println("this is the last page");
                            break;
                        } else {
//                            System.out.println("next page is : " + index + 1);
                            URL = sanitizeUrl(paginationLinks.get(index + 1).attr("abs:href"));
//                            System.out.println(URL);
                        }

                    }
                } catch (Exception e) {
                    exceptions++;
//                    System.out.println("no links in this category, empty category");
                    break;
                }

            } catch (Exception e) {
                exceptions++;
                e.printStackTrace(System.out);
            }
            URL = URL.replaceAll("ø", "%C3%B8").replaceAll("å", "%C3%A5").replaceAll("æ", "%C3%A6");
            try {
                doc = Jsoup.connect(URL).timeout(20000).followRedirects(true).get();
                System.out.println("############ Connected to: " + URL);
            } catch (IOException e) {
                exceptions++;
//                e.printStackTrace(System.out);
                Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        //set metrics to return
        METRICS.setAllJobs(allJobs);
        METRICS.setJobsInEnglish(jobsInEnglish);
        METRICS.setDuplicateJobs(duplicateJobs);
        METRICS.setExceptions(exceptions);
        return METRICS;
    }

    private ArrayList<Job> scanDocForPaid(Document doc, String city, String foundAt)
            throws ParseException {
        // get paid jobs table rows
        ArrayList<Job> jobs = new ArrayList<>();

        Elements JobDivs = doc.getElementsByClass("jobItem");
        allJobs += JobDivs.size();
        // scan all jobs
        for (Element jobDiv : JobDivs) {

            // get job jobURL
            Element firstLink = jobDiv.select("div[class = header]").first().select("a[href]").first();
            String jobURL = firstLink.attr("abs:href");

            //get teaser text
            String teaserText = jobDiv.select("div.longDescription").first().ownText();
            //some ads dont have this, so set it to "";
            if (teaserText.equals("")) {
                teaserText = "empty text, i might be english, so check my internal page";
            }

            //detect teaser text, and if english open connection to joblink
            try {
                if (languageDetector.detect(teaserText).equalsIgnoreCase("en")) {
                    danish = false;

                    // Open connection to joblink
                    Document internalJobPage = null;
                    try {
//                        System.out.println(jobURL);
                        internalJobPage = Jsoup.connect(jobURL).timeout(20000).get();

                        String description = "";
                        try {// some rare pages dont have any description tag. set them to danish true so as to skip them
                            description = internalJobPage.select("div[itemprop = description]").first().text();
                        } catch (Exception e) {
                            exceptions++;
//                            System.out.println("No desprition tag in internal page, i set danish as true, so as to skip this shit");
                        }
                        // check if job description tag has content
                        if (description.equals("")) {
//                            System.out.println("No desprition in internal page, i set danish as true, so as to skip this shit");
                            danish = true;
                        } else {
                            //check if description is in english
                            try {
                                if (languageDetector.detect(description).equalsIgnoreCase("en")) {
                                    danish = false;
                                } else {
                                    danish = true;
                                }
                            } catch (LangDetectException e) {
                                exceptions++;
//                                e.printStackTrace(System.out);
                            }
                        }
                        // check if ad required danish even if its written in English
                        if (filter.checkIfRequiresDanish(description)) {
//                            System.out.println("Danish requirement:" + internalJobPage.baseUri());
                            danish = true;
                        }

                        if (danish == false) {
//                            System.out.println("LANGUAGE IS ENGLISH " + description);
//                            System.out.println(" ");
                            //all is ok so now we will overpass Jobband and take the real job URL.
                            //the apply button has onclick="applyJob(555255, 'Akademikernes Jobbank') and this function does an ajax req.
                            //to this url:url: '/ajax.asp?act=job/ansoeg/'+stillingid. This returns data of this form:
                            //u$#$http://www.umu.se/umu/aktuellt/arkiv/lediga_tjanster/4-1114-14.html.
                            // do the same
                            String jobID = jobURL.split("/")[4];
                            String ajaxURL = "http://jobbank.dk/ajax.asp?act=job/ansoeg/" + jobID;
//                            System.out.println(ajaxURL);
                            Document ajaxResponse = Jsoup.connect(ajaxURL).timeout(20000).get();
                            String ajaxResponseBody = ajaxResponse.select("body").text();
                            String[] token = ajaxResponseBody.split("\\$#\\$");
//                            System.out.println(ajaxResponseBody);
                            if (token[0].equals("u")) {
                                jobURL = token[1];
                                //some urls are a prefetch url that redirects to the final url. Try to get it.
                                if (jobURL.contains("prefetch")) {
                                    if (jobURL.contains("url=")) {
                                        jobURL = jobURL.split("url=")[1];
                                    }
                                    if (jobURL.contains("aspx?u=")) {
                                        jobURL = jobURL.split("aspx\\?u=")[1];
                                    }

                                    if (jobURL.contains("prefetch_net")) {
                                        System.out.println("HERE");
                                        try {
                                            doc = Jsoup.connect(jobURL).timeout(20000).followRedirects(true).get();
                                            Elements select = doc.select("meta[http-equiv=refresh]");
                                            String content = select.first().attr("content");
                                            jobURL = content.substring(content.indexOf("url=") + "url=".length());
                                            System.out.println(jobURL);
                                        } catch (Exception ex) {
                                            ex.printStackTrace(System.out);
                                            exceptions++;
                                        }
                                    }

                                    try {
                                        jobURL = sanitizeUrl(jobURL);
                                    } catch (Exception e) {
                                        e.printStackTrace(System.out);
                                        exceptions++;
                                    }

                                }
                            }

                            // now take all the info for the job that we need, sout it and add it
                            //job title
                            String jobTitle = firstLink.ownText();

                            //get company.
                            String jobAnnouncer = internalJobPage.select("i.glyphicon-briefcase").first().parent().text();
                            jobAnnouncer = jobAnnouncer.split(" hos ")[1];

                            //get publication date
                            Date jobDate;
                            try {
                                String datePublished = jobDiv.select("span.date").first().ownText();
                                jobDate = new SimpleDateFormat("dd.MM.yyyy", new Locale("da", "DK")).parse(datePublished);
                            } catch (ParseException e) {
                                exceptions++;
                                jobDate = new Date();//TODO put last used date
//                                System.out.println("HERETO");
                            }
                            System.out.println("________________________________________________" + "\r\n");
//                            System.out.println("Job date: " + jobDate);
//                            System.out.println("Job Title: " + jobTitle);
//                            System.out.println("Job Announcer: " + jobAnnouncer);
//                            System.out.println("Description: " + teaserText);
//                            System.out.println("Ad url: " + jobURL);
//                            System.out.println(" ");

                            ArrayList fields = new ArrayList();

                            jobTitle = filter.homogeniseJobTitle(jobTitle);
                            jobAnnouncer = filter.homogeniseCompanyName(jobAnnouncer);
                            jobURL = filter.homogeniseURL(jobURL);

                            Job identifiedJob = new Job(jobTitle, jobAnnouncer, new URL(jobURL), jobDate, city, foundAt, fields);

                            Field f = new Field(jobCategory);
                            identifiedJob.addField(f);

                            identifiedJob = filter.filterTitle(identifiedJob, jobCategory, jobTitle);

                            jobs.add(identifiedJob);
                            jobsInEnglish++;

                        }

                    } catch (IOException e) {
                        exceptions++;
                        e.printStackTrace(System.out);
                    }
                } else {
                    danish = true;
                    // Not english, not caring anymore for this adv.
                    // System.out.println("LANGUAGE NOT ENGLISH " + teaserText);
                    // System.out.println(" ");

                }
            } catch (LangDetectException e) {
                exceptions++;
                e.printStackTrace(System.out);
            }
        }
        return jobs;
    }

    /**
     * Allow https connection
     *
     * @throws Exception
     */
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
            exceptions++;
            // We can not recover from this exception.
            e.printStackTrace(System.out);
        }
    }

    static private String sanitizeUrl(String msg) throws UnsupportedEncodingException {
        String decoded = java.net.URLDecoder.decode(msg, "UTF-8");
        decoded = decoded.replaceAll("\\$x\\$", "&");
        return decoded;
    }
}
