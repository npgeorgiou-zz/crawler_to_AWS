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
import model.Company;
import model.Job;
import model.Field;

public class JobbankCrawler {

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
    // constructor

    public JobbankCrawler(String URL, String area, String jobCategory, String foundAt, LangDetect languageDetector) throws IOException {
        this.URL = URL;
        this.area = area;
        this.jobCategory = jobCategory;
        this.foundAt = foundAt;
        this.languageDetector = languageDetector;

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
        URL = URL.replaceAll("ø", "%C3%B8")
                .replaceAll("å", "%C3%A5")
                .replaceAll("æ", "%C3%A6");
        Document doc = getDocument(URL);

        while (true) {
            try {

                // read page and return arrayList of paid Jobs
                ArrayList<Job> paidJobs = scanDocForPaid(doc, area, foundAt);

                // save Jobs
                int duplicateJobs = dbUtils.deleteDuplicatesAndAddtoDB(paidJobs);
                METRICS.setDuplicateJobs(METRICS.getDuplicateJobs() + duplicateJobs);
            } catch (ParseException pe) {
                METRICS.incrementExceptions();
                pe.printStackTrace(System.out);
            }
            // connect to next pages, if there are any
            // NOTE: For now dont commect to next pages. Jobbank has a buggy pagination that doenst allow
            // knowledge of the next page. Just finish after 1 scan.
//            if (hasNextpage(doc)) {
//                URL = getNextPage(doc);
//                URL = URL.replaceAll("ø", "%C3%B8").replaceAll("å", "%C3%A5").replaceAll("æ", "%C3%A6");
//                doc = getDocument(URL);
//            } else {
//                break;
//            }
            break;
        }
        return METRICS;
    }

    private ArrayList<Job> scanDocForPaid(Document doc, String city, String foundAt) throws ParseException {

        // get paid jobs table rows
        ArrayList<Job> jobs = new ArrayList<>();
        Elements JobDivs = doc.getElementsByClass("jobItem");
        METRICS.setAllJobs(METRICS.getAllJobs() + JobDivs.size());

        // scan all jobs
        for (Element jobDiv : JobDivs) {

            // get job jobURL
            Element firstLink = jobDiv.select("div[class = header]").first().select("a[href]").first();
            String jobURL = firstLink.attr("abs:href");

            //get teaser text
            String teaserText;
            try {
                teaserText = jobDiv.select("div.longDescription").first().ownText();
            } catch (Exception e) {
                teaserText = "empty text, i might be english, so check my internal page";
            }

            //some longDescriptions are "";
            if (teaserText.equals("")) {
                teaserText = "empty text, i might be english, so check my internal page";
            }

            //detect teaser text, and if english open connection to joblink
            if (isEnglish(teaserText)) {
                danish = false;

                // Open connection to joblink
                Document internalJobPage = null;
                try {
                    // System.out.println(jobURL);
                    internalJobPage = getDocument(jobURL);

                    String jobText = "";
                    try {
                        // some rare pages dont have any description tag. set them to danish true so as to skip them
                        jobText = internalJobPage.select("div[itemprop = description]").first().text();
                    } catch (Exception e) {
                        METRICS.incrementExceptions();
                        // System.out.println("No desprition tag in internal page, i set danish as true, so as to skip this shit");
                        danish = true;
                    }
                    // check if job description tag has content
                    if (jobText.equals("")) {
                        // System.out.println("No desprition in internal page, i set danish as true, so as to skip this shit");
                        danish = true;
                    } else {
                        
                        //check if description is in english
                        danish = !isEnglish(jobText);

                    }
                    // check if ad required danish even if its written in English
                    if (filter.checkIfRequiresDanish(jobText)) {
                        // System.out.println("Danish requirement:" + internalJobPage.baseUri());
                        danish = true;
                    }

                    if (danish == false) {
                        // System.out.println("LANGUAGE IS ENGLISH " + description);
                        //all is ok so now we will overpass Jobbank and take the real job URL.
                        //the apply button has onclick="applyJob(555255, 'Akademikernes Jobbank') and this function does 
                        // an ajax req. to this url:url: '/ajax.asp?act=job/ansoeg/'+stillingid. This returns data of 
                        //this form: u$#$http://www.umu.se/umu/aktuellt/arkiv/lediga_tjanster/4-1114-14.html.
                        // do the same
                        String jobID = jobURL.split("/")[4];
                        String ajaxURL = "http://jobbank.dk/ajax.asp?act=job/ansoeg/" + jobID;
                        // System.out.println(ajaxURL);
                        Document ajaxResponse = Jsoup.connect(ajaxURL).timeout(20000).get();
                        String ajaxResponseBody = ajaxResponse.select("body").text();
                        String[] token = ajaxResponseBody.split("\\$#\\$");
                        // System.out.println(ajaxResponseBody);
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
                                    // System.out.println("HERE");
                                    try {
                                        doc = Jsoup.connect(jobURL).timeout(20000).followRedirects(true).get();
                                        Elements select = doc.select("meta[http-equiv=refresh]");
                                        String content = select.first().attr("content");
                                        jobURL = content.substring(content.indexOf("url=") + "url=".length());
                                        // System.out.println(jobURL);
                                    } catch (Exception ex) {
                                        ex.printStackTrace(System.out);
                                        METRICS.incrementExceptions();
                                    }
                                }

                                try {
                                    jobURL = sanitizeUrl(jobURL);
                                } catch (Exception e) {
                                    e.printStackTrace(System.out);
                                    METRICS.incrementExceptions();
                                }
                            }
                        }

                        // now take all the info for the job that we need, sout it and add it
                        // get job title
                        String jobTitle = firstLink.ownText();

                        // get company.
                        String jobAnnouncer = internalJobPage.select("i.glyphicon-home").first().parent().text();

                        // get publication date
                        Date jobDate;
                        try {
                            String datePublished = jobDiv.select("span.date").first().ownText();
                            jobDate = new SimpleDateFormat("dd.MM.yyyy", new Locale("da", "DK")).parse(datePublished);
                        } catch (ParseException e) {
                            METRICS.incrementExceptions();
                            //TODO put last used date
                            jobDate = new Date();
                        }
                        System.out.println("________________________________________________" + "\r\n");

                        // temporarily set text as "" so as not to overload DB
                        jobText = "";

                        Job identifiedJob = new Job(jobTitle, jobAnnouncer, jobURL, jobDate, jobText, 1, city, foundAt, new ArrayList());
                        Field f = new Field(jobCategory);
                        identifiedJob.addField(f);
                        identifiedJob = filter.filterTitleForPossibleChangeInFields(identifiedJob, jobCategory, jobTitle);

                        jobs.add(identifiedJob);
                        METRICS.incrementJobsInEnglish();
                    }

                } catch (IOException e) {
                    METRICS.incrementExceptions();
                    e.printStackTrace(System.out);
                }
            } else {
                danish = true;
                // System.out.println("LANGUAGE NOT ENGLISH " + teaserText);
            }

        }
        return jobs;
    }

    
//    private boolean hasNextpage(Document doc) {
//        boolean has = false;
//        try {
//            Element paginationUL = doc.getElementsByClass("pagination").first();
//            Elements paginationLI = paginationUL.select("li");
// 
//            if (paginationLI.size() == 1) {
//                // There is is only this page
//            } else {
//                Element currentPage = paginationUL.select("a.currentPage").first();
//                int index = paginationLI.indexOf(currentPage);
//                if (paginationLI.size() - 1 == index) {
//                    // This is the last page
//                } else {
//                    has = true;
//                }
//
//            }
//        } catch (Exception e) {
//            METRICS.incrementExceptions();
//            e.printStackTrace(System.out);
//        }
//        return has;
//    }
//    
//    // Returns null if an exception happens and coudlnt obtain result
//    private String getNextPage(Document doc) {
//        String url = null;
//        
//        try {
//            // Get pagination list
//            Element paginationUL = doc.getElementsByClass("pagination").first();
//            Elements paginationLI = paginationUL.select("li");
//            System.out.println(paginationLI);
//            
//            // Get the active list item
//            Element activeListItem = paginationLI.select("li.active").first();
//            System.out.println(activeListItem);
//            
//            // Get the a of the next list item
//            int index = paginationLI.indexOf(activeListItem);
//            url = sanitizeUrl(paginationLI.get(index + 1).select("a").attr("abs:href"));
//      
//        } catch (Exception e) {
//            METRICS.incrementExceptions();
//            e.printStackTrace(System.out);
//        }
//        return url;
//    }
    // Returns null if an exception happens and couldnt obtain result
    private Document getDocument(String url) {
        Document doc = new Document("");
        try {
            doc = Jsoup.connect(url)
                    .timeout(20000)
                    .followRedirects(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.124 Safari/537.36")
                    .get();
            System.out.println("############ Connected to: " + url);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            METRICS.incrementExceptions();
            Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, e);
            doc = null;
        }
        return doc;
    }

    private boolean isEnglish(String text) {
        boolean english = false;
        try {
            english = languageDetector.detect(text).equalsIgnoreCase("en");
        } catch (LangDetectException e) {
            METRICS.incrementExceptions();
            // e.printStackTrace(System.out);
        }

        return english;
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
            METRICS.incrementExceptions();
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
