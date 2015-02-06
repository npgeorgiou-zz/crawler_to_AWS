package crawlers;

import crawlerUtils.Filter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Field;
import model.Job;

public class MonsterCrawler {
    // declare variables

    private boolean danish = false;

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
    public MonsterCrawler(String URL, String area, String jobCategory, String foundAt, LangDetect languageDetector) throws IOException {
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
        METRICS = new Metrics("MonsterPage");
        dbUtils = new DatabaseUtils();

        try {
            setTrustAllCerts();
        } catch (Exception e) {
            exceptions++;
            e.printStackTrace(System.out);
        }

    }

    public Metrics scan() {
        // connect to URL
        URL = URL.replaceAll("ø", "%C3%B8").replaceAll("å", "%C3%A5").replaceAll("æ", "%C3%A6")
                .replaceAll("ä", "%C3%A4").replaceAll("ö", "%C3%B6");
        Document doc = new Document("");
        try {
            doc = Jsoup.connect(URL).timeout(200000).followRedirects(true).get();
//            System.out.println("############### NEW CRAWLER");
//            System.out.println("############ Connected to: " + URL);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            exceptions++;
            Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, e);
        }
        while (true) {

            try {
                // read page and return arrayList of paid Jobs
                ArrayList<Job> paidJobs = scanPage(doc, "odd", "even", area, foundAt);
                // save Jobs
                duplicateJobs += dbUtils.deleteDuplicatesAndAddtoDB(paidJobs);
//                dbUtils.addToDatabase();
            } catch (ParseException pe) {
                exceptions++;
                pe.printStackTrace(System.out);
            }
            // connect to next pages, if there are any
            try {
                Element paginationDiv = doc.getElementsByClass("navigationBar").first();
                if (paginationDiv != null) {// if there is pagination, therefore if there are other pages
//                    System.out.println("there is pagination");

                    Element aWithClassLast = paginationDiv.getElementsByClass("last").first();
                    if (aWithClassLast == null) {//this isnt the last page
//                        System.out.println("this isnt the last page");
//                        System.out.println(">> " + URL);
                        URL = paginationDiv.select("a").last().attr("abs:href");
//                        System.out.println(">> " + URL);
//                        System.out.println(URL);
                    } else {
//                        System.out.println("this is the last page");
                        break;
                    }

                } else {
                    break;
                }

            } catch (Exception e) {
                exceptions++;
                e.printStackTrace(System.out);
            }
            URL = URL.replaceAll("ø", "%C3%B8").replaceAll("å", "%C3%A5").replaceAll("æ", "%C3%A6")
                    .replaceAll("ä", "%C3%A4").replaceAll("ö", "%C3%B6");
            try {

                doc = Jsoup.connect(URL).timeout(200000).followRedirects(true).get();
//                System.out.println("############ changed page in pagination");
//                System.out.println("############ Connected to: " + URL);
            } catch (IOException e) {
                exceptions++;
                e.printStackTrace(System.out);
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

    private ArrayList<Job> scanPage(Document doc, String tag1, String tag2, String city, String foundAt)
            throws ParseException {
        // get paid jobs table rows
        ArrayList<Job> jobs = new ArrayList<>();
        Elements paidJobTableRowsOdd = doc.getElementsByClass(tag1);
        Elements paidJobTableRowsEven = doc.getElementsByClass(tag2);
        Elements allRows = new Elements();
        allRows.addAll(paidJobTableRowsOdd);
        allRows.addAll(paidJobTableRowsEven);
        allJobs += allRows.size();

        // scan all rows
        for (Element paidJobRow : allRows) {
            // get ad link url
            Element firstLink = paidJobRow.select("a[href]").first();
            String jobURL = null;
            jobURL = firstLink.attr("abs:href");

            // Open connection to joblink
            Document internalDocument = null;
            try {
//                System.out.println(jobURL);
                internalDocument = Jsoup.connect(jobURL).timeout(20000).get();
                Element internalDocumentBody = internalDocument.select("body").first();

                try {
                    if (!internalDocumentBody.text().trim().isEmpty()) { // if page has text content
                        //take main div that holds ad text
                        Element mainDiv = internalDocumentBody.select("div[itemprop = description]").first();
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div id="jobBodyContent">
                            mainDiv = internalDocumentBody.select("div[id = jobBodyContent]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
                            mainDiv = internalDocumentBody.select("div[id = CJT-jobBodyContent]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
                            mainDiv = internalDocumentBody.select("div[id = CJT-jobdesc]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
                            mainDiv = internalDocumentBody.select("div[id = contentleft]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
                            mainDiv = internalDocumentBody.select("div[id = left_wrapper]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
                            mainDiv = internalDocumentBody.select("div[id = left_column]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div id="TrackingJobBody">
                            mainDiv = internalDocumentBody.select("span[id = TrackingJobBody]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div id="mycontent">
                            mainDiv = internalDocumentBody.select("div[id = textWrap]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div id="mycontent">
                            mainDiv = internalDocumentBody.select("div[id = mycontent]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div class = "jDespHolder">
                            mainDiv = internalDocumentBody.select("div[id = jobdesc]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div class = "jDespHolder">
                            mainDiv = internalDocumentBody.select("div[class = main]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <span itemprop="description">
                            mainDiv = internalDocumentBody.select("span[itemprop = description]").first();
                        }
                        if (mainDiv == null) {//if this page format doesnt exist, set whole doc
                            mainDiv = internalDocumentBody;
                        }
                        String mainText = mainDiv.text();
                        // if the text of the mainDiv is nothing, put whole document text as text
                        if (mainText.equals("")) {//if this page format doesnt exist, set whole doc
                            mainText = internalDocumentBody.text();
                        }

                        if (languageDetector.detect(mainText).equalsIgnoreCase("en")) {// and if is english
                            danish = false;
                        } else {
                            danish = true;
                        }

                        // check if ad required danish even if its written in English
                        if (filter.checkIfRequiresDanish(mainText)) {
//                            System.out.println("Danish requirement:" + internalDocument.baseUri());
                            danish = true;
                        }

                        if (danish == false) {
                            // job is what we are looking for
//                            System.out.println("LANGUAGE IS ENGLISH " + mainText);
//                            System.out.println(" ");

                            // now we try to bypass monster internal pages, if we can
                            String realURL = "";
                            Elements scriptElement = internalDocument.getElementsByTag("script");
                            for (Element s : scriptElement) {
                                if (s.html().contains("ApplyOnlineUrl: ")) {
                                    realURL = s.html().split("ApplyOnlineUrl: '")[1];
                                    realURL = realURL.split("',")[0];

                                    if (!(realURL.contains("mit.monster.dk") || realURL.contains("mitt.monster.se")) && realURL.length() != 0) {//if realURL is not an internal monster url, or is not like : ApplyOnlineUrl: ''                                      
                                        jobURL = realURL;
                                    }
                                }
                            }

                            // get info that we need
                            //get job title
                            String jobTitle = paidJobRow.select("div[class = jobTitleContainer]").text();
                            //get company
                            //String jobAnnouncer = paidJobRow.select("div[class = companyContainer]").text().replace("Virksomhed: ", "");
                            String jobAnnouncer = paidJobRow.select("div[class = companyContainer]").select("a[href]").attr("title");
                            //get publication date
                            String daysPublished = paidJobRow.select("div[class = fnt20]").text().replace("Publiceret: ", "").replace(" dage siden", "").replace("Publicerad: ", "").replace(" dagar sedan", "");
                            Calendar cal = Calendar.getInstance();
                            Date jobDate;
                            int daysPublishedAsInt;
                            if (daysPublished.equals("I dag")) {
                                jobDate = cal.getTime();
                            } else {
                                daysPublishedAsInt = Integer.parseInt(daysPublished);
                                cal.add(Calendar.DATE, -daysPublishedAsInt);
                                jobDate = cal.getTime();
                            }
                            System.out.println("________________________________________________" + "\r\n");
                            //sout info and add it to arrayList
                            //System.out.println("job date: " + jobDate);
                            //System.out.println("Job Title: " + jobTitle);
                            //System.out.println("Job Announcer: " + jobAnnouncer);
                            //System.out.println("Small text: " + mainText);
                            //System.out.println("Ad url: " + jobURL);
                            //System.out.println(" ");

                            ArrayList fields = new ArrayList();

                            jobTitle = filter.homogeniseJobTitle(jobTitle);
                            jobAnnouncer = filter.homogeniseCompanyName(jobAnnouncer);
                            jobURL = filter.homogeniseURL(jobURL);

                            Job identifiedJob = new Job(jobTitle, jobAnnouncer, new URL(jobURL), jobDate, mainText, 1, city, foundAt, fields);

                            Field f = new Field(jobCategory);
                            identifiedJob.addField(f);
                            identifiedJob = filter.filterTitle(identifiedJob, jobCategory, jobTitle);

                            jobs.add(identifiedJob);
                            jobsInEnglish++;

                        } else {
//                            System.out.println("LANGUAGE NOT ENGLISH " + mainText);
//                            System.out.println(" ");
                        }

                    }
                } catch (LangDetectException e) {
                    exceptions++;
                    e.printStackTrace(System.out);
                }

            } catch (IOException e) {
                exceptions++;
                e.printStackTrace(System.out);
            }
        }
        return jobs;
    }

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
            e.printStackTrace(System.out);
        }
    }

}
