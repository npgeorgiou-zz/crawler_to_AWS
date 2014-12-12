package crawlers;

import crawlerUtils.Filter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocketFactory;
import model.Company;
import model.Job;
import model.Field;

public class JobindexCrawler {

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

    private Date previousJobDate;

    //constructor
    public JobindexCrawler(String URL, String area, String jobCategory, String foundAt, LangDetect languageDetector) throws IOException {
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
        METRICS = new Metrics("JobindexPage");
        dbUtils = new DatabaseUtils();

        this.previousJobDate = new Date();

        try {
            setTrustAllCerts();
        } catch (Exception e) {
            exceptions++;
            e.printStackTrace(System.out);
        }

    }

    public Metrics scan() throws IOException {
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
                ArrayList<Job> paidJobs = scanDocForPaid(doc, "PaidJob", area, foundAt);
                // save Jobs
                duplicateJobs += dbUtils.deleteDuplicatesAndAddtoDB(paidJobs);
                // repeat for unpaid Jobs
                ArrayList<Job> externalJobs = scanDocForExternal(doc, "jix_robotjob ", area, foundAt);
                duplicateJobs += dbUtils.deleteDuplicatesAndAddtoDB(externalJobs);
            } catch (ParseException pe) {
                exceptions++;
                pe.printStackTrace(System.out);
            }
            // connect to next pages, if there are any
            try {
                Elements pages = doc.getElementsByClass("jix_pagination_pages");
                Element page = pages.get(0);
                Elements links = page.select("a[href]");
                if (links.size() > 0) { // if here are other pages
                    String nextPage = links.get(links.size() - 1).text();
                    //System.out.println("next page is : " + nextPage);

                    if (!nextPage.matches(".*\\d.*")) {// if next page text is not a number, so if we still have next pages
                        URL = sanitizeUrl(links.get(links.size() - 1).attr("abs:href"));
                    } else {
                        break;
                    }
                } else {
                    //System.out.println("this is the last page");
                    break;
                }
            } catch (Exception e) {
                exceptions++;
                e.printStackTrace(System.out);
            }
            URL = URL.replaceAll("ø", "%C3%B8").replaceAll("å", "%C3%A5").replaceAll("æ", "%C3%A6");
            try {
                doc = Jsoup.connect(URL).timeout(20000).followRedirects(true).get();
                System.out.println("Connected to: " + URL);
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

    private ArrayList<Job> scanDocForPaid(Document doc, String tag, String city, String foundAt) throws ParseException {
        // get paid jobs divs
        ArrayList<Job> jobs = new ArrayList<>();
        Elements paidJobDivs = doc.getElementsByClass(tag);
        allJobs += paidJobDivs.size();

        // detect if div text is english or danish
        for (Element paidJobDiv : paidJobDivs) {
            Elements jobtexts = paidJobDiv.select("p, li");
            StringBuilder toDetect = new StringBuilder();
            for (Element text : jobtexts) {
                toDetect.append(text.text());
            }

            try {
                if (!languageDetector.detect(toDetect.toString()).equalsIgnoreCase("en")) {
                    // System.out.println("LANGUAGE NOT ENGLISH " + toDetect.toString());
                    // System.out.println(" ");
                    // Not english, not caring anymore for this adv.

                } else {// Language seems to be English

                    // System.out.println("LANGUAGE IS ENGLISH " + toDetect.toString());
                    // System.out.println(" ");
                    // get small teaser text. if index 1 is empty, take index 2
                    String jobSmallText = jobtexts.get(1).ownText();
                    if (jobSmallText.isEmpty()) {
                        jobSmallText = jobtexts.get(2).ownText();
                    } else {
                    }

                    // get date that the ad was posted
                    Date jobDate;
                    DateFormat df = new SimpleDateFormat("d MMMM yyyy", new Locale("da", "DK"));
                    String temp;

                    if (!paidJobDiv.select("CITE").text().isEmpty()) {// if ad has a date (sometimes they dont)
                        try {
                            String[] array = paidJobDiv.select("CITE").first().outerHtml().split(", ");
                            temp = paidJobDiv.select("CITE").first().outerHtml().split(", ")[array.length - 1];
                            temp = temp.replaceAll("&nbsp;", " ");
                            temp = temp.replaceAll("\\.", "");
                            temp = temp.substring(0, temp.lastIndexOf("</"));
                            temp = removeSpaces(temp);
                            temp = temp.concat(" " + Calendar.getInstance(new Locale("da", "DK")).get(Calendar.YEAR));
                            jobDate = convertSTRtoDATE(temp);
                            previousJobDate = jobDate;
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            exceptions++;
                            //put last used date
                            jobDate = previousJobDate;
                            System.out.println("PREVIOUS DATE! 1");
                            System.out.println("> " + jobDate);
                        }
                    } else {
                        //put last used date
                        jobDate = previousJobDate;
                        System.out.println("PREVIOUS DATE! 1");
                        System.out.println("> " + jobDate);
                    }

                    // get job link and job announcer and title
                    Elements links = paidJobDiv.select("a[href]");
                    Element joblink = links.get(1);
                    Element jobannouncer = links.get(2);
                    String jobTitle = joblink.text();
                    String jobAnnouncer = jobannouncer.text();

                    // get ad link url
                    String jobURL = null;
                    try {
                        try {
                            jobURL = sanitizeUrl(joblink.attr("abs:href"));
                        } catch (UnsupportedEncodingException uee) {
                            uee.printStackTrace(System.out);
                            exceptions++;
                            continue;
                        }
                        jobURL = jobURL.split("&url=")[1];
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        aioobe.printStackTrace(System.out);
                        exceptions++;
                    }

                    // check if link leads to a pdf, or a ashx and handle them
                    // here as java will not open it
                    if (jobURL.contains(".pdf") || jobURL.contains(".ashx")) {
                        Job identifiedJob = null;
                        try {
                            //clean company and title
                            jobTitle = filter.homogeniseJobTitle(jobTitle);
                            jobAnnouncer = filter.homogeniseCompanyName(jobAnnouncer);
                            jobURL = filter.homogeniseURL(jobURL);
                            ArrayList fields = new ArrayList();
                            identifiedJob = new Job(jobTitle, jobAnnouncer, new URL(jobURL), jobDate, city, foundAt, fields);
                        } catch (MalformedURLException e) {
                            e.printStackTrace(System.out);
                        }
                        Field f = new Field(jobCategory);
                        identifiedJob.addField(f);
                        identifiedJob = filter.filterTitle(identifiedJob, jobCategory, jobTitle);
                        jobs.add(identifiedJob);
                        jobsInEnglish++;
                    } else {

                        // Open connection to joblink
                        Document internalDocument = null;
                        try {
                            internalDocument = Jsoup.connect(jobURL).timeout(20000).get();
                            internalDocument.select("head,img,script,href").remove();
                            String textToDetect = "";
                            try {
                                // check if the job link page is in english
                                if (!internalDocument.text().trim().isEmpty()) { // if page has text content

                                    //if its a jobindex page, then we know in which divs is the jobtext ;)
                                    if (jobURL.contains("jobindex.dk/")) {
                                        Element mainDiv = internalDocument.select("div[class = jobcontent]").first();
                                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div class="jix_jobtext_add_wrap">
                                            mainDiv = internalDocument.select("div[class = jix_jobtext_add_wrap]").first();
                                        }
                                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div class="PaidJob">
                                            mainDiv = internalDocument.select("div[class = PaidJob]").first();
                                        }
                                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is the whoe doc>
                                            mainDiv = internalDocument;
                                        }
                                        textToDetect = mainDiv.text();
                                    } else {
                                        textToDetect = internalDocument.text();
                                    }
                                    if (languageDetector.detect(textToDetect).equalsIgnoreCase("en")) {// and if is english
                                        danish = false;
                                    } else {
                                        danish = true;
                                    }

                                }
                            } catch (LangDetectException e) {
                                exceptions++;
                                e.printStackTrace(System.out);
                            }

                            // check if ad required danish even if its written in English
                            if (filter.checkIfRequiresDanish(textToDetect)) {
                                //System.out.println("Danish requirement:" + internalDocument.baseUri());
                                danish = true;
                            }

                            if (danish == false) {
                                System.out.println("________________________________________________" + "\r\n");
                                // job is what we are looking for, sout info and add it to arrayList
                                //System.out.println("job date: " + jobDate);
//                                System.out.println("Job Title: " + jobTitle);
                                //System.out.println("Job Announcer: " + jobAnnouncer);
                                //System.out.println("Small text: " + jobSmallText);
                                //System.out.println("Ad url: " + jobURL);
                                //System.out.println(" ");

                                ArrayList fields = new ArrayList();

                                //clean company and title
                                jobTitle = filter.homogeniseJobTitle(jobTitle);
                                jobAnnouncer = filter.homogeniseCompanyName(jobAnnouncer);
                                jobURL = filter.homogeniseURL(jobURL);
                                Job identifiedJob = new Job(jobTitle, jobAnnouncer, new URL(jobURL), jobDate, city, foundAt, fields);

                                Field f = new Field(jobCategory);
                                identifiedJob.addField(f);

                                identifiedJob = filter.filterTitle(identifiedJob, jobCategory, jobTitle);

                                jobs.add(identifiedJob);
                                jobsInEnglish++;

//                                //TEMPORARILY HERE: SAVE COMPANY AND LOGO
//                                //********* take logo
//                                Element img = paidJobDiv.select("img[src]").first();
//                                String logoLink = img.absUrl("src");
//                                System.out.println(logoLink);
//
//                                String imgName = "";
//                                try {
//                                    imgName = logoLink.split("/logo/")[1];
//                                } catch (ArrayIndexOutOfBoundsException aioob) {
//                                    imgName = logoLink.split("/brand/")[1];
//                                }
//
//                                System.out.println(imgName);
//                                String logoPath = "";
//                                //save logo
//                                URL url;
//                                try {
//                                    url = new URL(logoLink);
//                                    InputStream in = new BufferedInputStream(url.openStream());
//                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                                    byte[] buf = new byte[1024];
//                                    int n = 0;
//                                    try {
//                                        while (-1 != (n = in.read(buf))) {
//                                            out.write(buf, 0, n);
//                                        }
//
//                                        out.close();
//                                        in.close();
//                                        byte[] response = out.toByteArray();
//
//                                        FileOutputStream fos = new FileOutputStream("C://Users//ksptsinplanet//WebstormProjects//mySite//public//img//logos//" + imgName);
//                                        fos.write(response);
//                                        fos.close();
//                                        //make logoPath to save in DB
//                                        logoPath = "img/logos/" + imgName;
//                                    } catch (MalformedURLException ex) {
//                                        Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, ex);
//                                    }
//                                } catch (IOException ex) {
//                                    Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, ex);
//                                }
//
//                                ////********* take logo end
//                                Company c = new Company(jobAnnouncer, logoPath);
//                                dbUtils.addCompanyToDatabase(c);
                            }
                        } catch (IOException e) {
                            e.printStackTrace(System.out);
                            exceptions++;
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                            exceptions++;
                        }
                    }
                }
            } catch (LangDetectException e) {
                e.printStackTrace(System.out);
                exceptions++;
            }
            danish = false;
        }
        return jobs;
    }

    private ArrayList<Job> scanDocForExternal(Document doc, String tag, String city, String foundAt) throws ParseException {

        ArrayList<Job> jobs = new ArrayList<>();

        // get unpaid ad divs
        Elements externalJobsDivs = doc.select("div.jix_robotjob");
        allJobs += externalJobsDivs.size();
        for (Element externalJobDiv : externalJobsDivs) {
            // Remove images
            Elements media = externalJobDiv.select("[src]");
            for (Element src : media) {
                src.remove();
            }

            // get job title
            Elements links = externalJobDiv.select("a[href]");
            Element joblink = links.get(0);
            String jobTitle = joblink.text();

            // Most small ads have jobAnnouncer and place in bold in this format : Announcer, place.
            // So, in case the ad has bolds,take them, store them, clear them from text.
            // If it doesnt, store placeholders
            String jobAnnouncer = "";
            String place = "";
            Elements bolds = externalJobDiv.select("b");
            if (bolds.size() == 2) {
                // get job announcer and place from bold tags in order to clear them from text later
                Element firstbold = bolds.get(0);
                Element secondbold = bolds.get(1);
                jobAnnouncer = firstbold.text();
                place = secondbold.text();
            } else {
                jobAnnouncer = externalJobDiv.select("CITE").first().text().split(", ")[0];
                //System.out.println(jobAnnouncer);
                place = "";
            }

            // put date in String in order to clear it from text later
            String Date = "";
            try {
                Date = externalJobDiv.select("CITE").first().text().split(", ")[1];
            } catch (Exception e) {
                e.printStackTrace(System.out);
                exceptions++;
                //Exception in CITE date getting. Possible cause: No date or comma seperator in CITE tag
            }

            // get ad text
            String jobText = "";
            try {
                jobText = externalJobDiv.text().substring(joblink.text().length() + 1, externalJobDiv.text().indexOf(" Gem job"))
                        .replaceAll(jobAnnouncer, "").replaceAll(place, "").replaceAll(Date, "");
                //replace ... ,... . ,  and other shit that remains after removing these from jobtext
                jobText = jobText.split("\\.\\.\\.")[0];
                jobText = jobText.split(" \\, ")[0];
            } catch (Exception e) {
                e.printStackTrace(System.out);
                exceptions++;
            }

            // detect if div text is english or danish
            try {
                if (!languageDetector.detect(jobText).equalsIgnoreCase("en")) {
                    //System.out.println("LANGUAGE NOT ENGLISH " + jobSmallText);
                    //System.out.println(" ");
                    // Not english, not caring anymore for this adv.
                } else {// Language seems to be English

                    //System.out.println("LANGUAGE IS ENGLISH " + jobSmallText);
                    //System.out.println(" ");
                    // get date that the ad was posted
                    Date jobDate;
                    DateFormat df = new SimpleDateFormat("d MMMM yyyy", new Locale("da", "DK"));
                    String temp;

                    try {
                        temp = externalJobDiv.select("CITE").first().outerHtml().split(", ")[1];
                        temp = temp.replaceAll("&nbsp;", " ");
                        temp = temp.replaceAll("\\.", "");
                        temp = temp.substring(0, temp.lastIndexOf("</"));
                        temp = removeSpaces(temp);
                        temp = temp.concat(" " + Calendar.getInstance(new Locale("da", "DK")).get(Calendar.YEAR));
                        jobDate = convertSTRtoDATE(temp);

                        previousJobDate = jobDate;
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        exceptions++;
                        //something here failed, bad date format on site, so put previosuley used date
                        jobDate = previousJobDate;
                        System.out.println("PREVIOUS DATE! 0");
                        System.out.println("> " + jobDate);
                    }

                    // get ad link url
                    String jobURL = null;
                    try {
                        try {
                            jobURL = sanitizeUrl(joblink.attr("abs:href"));
                        } catch (UnsupportedEncodingException uee) {
                            uee.printStackTrace(System.out);
                            exceptions++;
                            continue;
                        }
                        jobURL = jobURL.split("&url=")[1];

                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        aioobe.printStackTrace(System.out);
                        exceptions++;
                    }
                    // check if link leads to a pdf, or a ashx and handle them
                    // here as java will not open it
                    if (jobURL.contains(".pdf") || jobURL.contains(".ashx")) {
                        Job identifiedJob = null;
                        try {
                            //clean company and title
                            jobTitle = filter.homogeniseJobTitle(jobTitle);
                            jobAnnouncer = filter.homogeniseCompanyName(jobAnnouncer);
                            jobURL = filter.homogeniseURL(jobURL);

                            ArrayList fields = new ArrayList();
                            identifiedJob = new Job(jobTitle, jobAnnouncer, new URL(jobURL), jobDate, city, foundAt, fields);
                        } catch (MalformedURLException e) {
                            e.printStackTrace(System.out);
                            exceptions++;
                        }
                        Field f = new Field(jobCategory);
                        identifiedJob.addField(f);

                        identifiedJob = filter.filterTitle(identifiedJob, jobCategory, jobTitle);
                        jobs.add(identifiedJob);
                        jobsInEnglish++;
                    } else {
                        // Open connection to joblink
                        Document internalDocument = null;
                        try {
                            internalDocument = Jsoup.connect(jobURL).timeout(20000).get();
                            internalDocument.select("head,img,script,href").remove();
                            String textToDetect = "";
                            try {
                                // check if the job link page is in english
                                if (!internalDocument.text().trim().isEmpty()) { // if page has text content

                                    //if its a jobindex page, then we know in which divs is the jobtext ;)
                                    if (jobURL.contains("jobindex.dk/")) {
                                        Element mainDiv = internalDocument.select("div[class = jobcontent]").first();
                                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div class="jix_jobtext_add_wrap">
                                            mainDiv = internalDocument.select("div[class = jix_jobtext_add_wrap]").first();
                                        }
                                        if (mainDiv == null) {//if this page format doesnt exist, then maindiv is <div class="PaidJob">
                                            mainDiv = internalDocument.select("div[class = PaidJob]").first();
                                        }
                                        if (mainDiv == null) {//if this page format doesnt exist, then just set all site
                                            mainDiv = internalDocument;
                                        }
                                        jobText = mainDiv.text();
                                        textToDetect = mainDiv.text();
                                    } else {
                                        textToDetect = internalDocument.text();
                                    }

                                    if (languageDetector.detect(textToDetect).equalsIgnoreCase("en")) {// and if is english
                                        danish = false;
                                    } else {
                                        danish = true;
                                    }
                                }
                            } catch (LangDetectException e) {
                                exceptions++;
                                e.printStackTrace(System.out);
                            }

                            // check if ad required danish even if its written in English
                            if (filter.checkIfRequiresDanish(textToDetect)) {
                                //System.out.println("Danish requirement:" + internalDocument.baseUri());
                                danish = true;
                            }

                            if (danish == false) {
                                System.out.println("________________________________________________" + "\r\n");
                                // job is what we are looking for, sout info and add it to arrayList
                                //System.out.println(" job date: " + jobDate);
                                System.out.println(" Job Title: " + jobTitle);
                                //System.out.println(" Job Announcer: " + jobAnnouncer);
                                //System.out.println("Small text: " + jobSmallText);
                                //System.out.println("Ad url: " + jobURL);
                                //System.out.println(" ");

                                ArrayList fields = new ArrayList();

                                //clean company and title
                                jobTitle = filter.homogeniseJobTitle(jobTitle);
                                jobAnnouncer = filter.homogeniseCompanyName(jobAnnouncer);
                                jobURL = filter.homogeniseURL(jobURL);
                                Job identifiedJob = new Job(jobTitle, jobAnnouncer, new URL(jobURL), jobDate,  city, foundAt, fields);

                                Field f = new Field(jobCategory);
                                identifiedJob.addField(f);
                                identifiedJob = filter.filterTitle(identifiedJob, jobCategory, jobTitle);

                                jobs.add(identifiedJob);
                                jobsInEnglish++;

                            }
                        } catch (IOException e) {
                            exceptions++;
                            e.printStackTrace(System.out);
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                            exceptions++;
                        }

                    }
                }
            } catch (LangDetectException e) {
                //System.out.println("Exeption in language detection of ad (Possible case: no text to analyse)");
                exceptions++;
                e.printStackTrace(System.out);
            }
            danish = false;
        }
        return jobs;

    }

    private String removeSpaces(String input) {
        input = input.replaceAll("^\\s*", "");
        input = input.trim();
        return input;
    }

    private String sanitizeUrl(String msg) throws UnsupportedEncodingException {
        String decoded = java.net.URLDecoder.decode(msg, "UTF-8");
        return decoded;
    }

    private void setTrustAllCerts() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }
        };

        try {
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("SSLv3");

            } catch (NoSuchAlgorithmException e) {
                exceptions++;
                e.printStackTrace(System.out);
            }

            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory factory = sslContext.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(factory);
        } catch (KeyManagementException e) {
            exceptions++;
            e.printStackTrace(System.out);
        }
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    private Date convertSTRtoDATE(String mystr) {
        try {
            Date date = new SimpleDateFormat("d MMMM yyyy", new Locale("da", "DK")).parse(mystr);
            return date;
        } catch (ParseException e) {
            exceptions++;
            e.printStackTrace(System.out);
            return null;
        }

    }

}