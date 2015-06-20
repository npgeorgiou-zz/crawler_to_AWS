package crawlers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import di.DI;
import model.Metrics;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Job;
import model.Field;

public class JobbsafariCrawler extends BaseCrawler {

    // declare variables
    public JobbsafariCrawler(DI di, String URL, String area, String jobCategory, String foundAt) throws IOException {
        super(di, URL, area, jobCategory, foundAt);
    }

    public Metrics scan() {
        // connect to URL
        URL = URL.replaceAll("ø", "%C3%B8")
                .replaceAll("å", "%C3%A5")
                .replaceAll("æ", "%C3%A6");
        Document doc = new Document("");
        try {
            doc = Jsoup.connect(URL).timeout(20000).followRedirects(true).get();
            // System.out.println("############ Connected to: " + URL);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            METRICS.incrementExceptions();
            Logger.getLogger(JobbsafariCrawler.class.getName()).log(Level.SEVERE, null, e);
        }

        while (true) {

            try {

                // read page and return arrayList of paid Jobs
                ArrayList<Job> paidJobs = scanDocForPaid(doc, "PaidJob", area, foundAt);

                // save Jobs
                int duplicateJobs = di.getDB().deleteDuplicatesAndAddtoDB(paidJobs);
                METRICS.setDuplicateJobs(METRICS.getDuplicateJobs() + duplicateJobs);

                // repeat for unpaid Jobs
                ArrayList<Job> externalJobs = scanDocForExternal(doc, "jix_robotjob ", area, foundAt);
                duplicateJobs = di.getDB().deleteDuplicatesAndAddtoDB(externalJobs);
                METRICS.setDuplicateJobs(METRICS.getDuplicateJobs() + duplicateJobs);
            } catch (ParseException pe) {
                METRICS.incrementExceptions();
                pe.printStackTrace(System.out);
            }
            // connect to next pages, if there are any
            try {
                Elements pages = doc.getElementsByClass("jix_pagination_pages");
                Element page = pages.get(0);
                Elements links = page.select("a[href]");

                // if there are other pages
                if (links.size() > 0) {
                    String nextPage = links.get(links.size() - 1).text();

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
                METRICS.incrementExceptions();
                e.printStackTrace(System.out);
            }
            URL = URL.replaceAll("ø", "%C3%B8").replaceAll("å", "%C3%A5").replaceAll("æ", "%C3%A6");
            try {
                doc = Jsoup.connect(URL).timeout(20000).followRedirects(true).get();
//                System.out.println("Connected to: " + URL);
            } catch (IOException e) {
                METRICS.incrementExceptions();
                e.printStackTrace(System.out);
                Logger.getLogger(JobbsafariCrawler.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        //set metrics to return
        return METRICS;
    }

    private ArrayList<Job> scanDocForPaid(Document doc, String tag, String city, String foundAt) throws ParseException {
        // get paid jobs divs
        ArrayList<Job> jobs = new ArrayList<>();
        Elements paidJobDivs = doc.getElementsByClass(tag);
        METRICS.setAllJobs(METRICS.getAllJobs() + paidJobDivs.size());

        // detect if div text is english or danish
        for (Element paidJobDiv : paidJobDivs) {
            Elements jobtexts = paidJobDiv.select("p, li");
            StringBuilder toDetect = new StringBuilder();
            for (Element text : jobtexts) {
                toDetect.append(text.text());
            }

            if (!isEnglish(toDetect.toString())) {
                System.out.println("LANGUAGE NOT ENGLISH " + toDetect.toString());
                System.out.println(" ");
                // Not english, not caring anymore for this adv.

            } else {
                    // Language seems to be English
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

                // if ad has a date (sometimes they dont)
                if (!paidJobDiv.select("CITE").text().isEmpty()) {
                    try {
                        String[] array = paidJobDiv.select("CITE").first().outerHtml().split(", ");
                        temp = paidJobDiv.select("CITE").first().outerHtml().split(", ")[array.length - 1];
                        temp = temp.replaceAll("&nbsp;", " ");
                        temp = temp.replaceAll("\\.", "");
                        temp = temp.substring(0, temp.lastIndexOf("</"));
                        temp = removeSpaces(temp);
                        temp = temp.concat(" " + Calendar.getInstance(new Locale("da", "DK")).get(Calendar.YEAR));
                        jobDate = convertSTRtoDATE(temp);
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        METRICS.incrementExceptions();
                            //put todays date
                        //TODO put last used date
                        jobDate = new Date();
                    }
                } else {
                        //put todays date
                    //TODO put last used date
                    jobDate = new Date();
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
                        METRICS.incrementExceptions();
                        continue;
                    }
                    jobURL = jobURL.split("&url=")[1];
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    aioobe.printStackTrace(System.out);
                    METRICS.incrementExceptions();
                }

                // Check if link leads to a pdf, or a ashx and handle them here as java will not open it
                if (jobURL.contains(".pdf") || jobURL.contains(".ashx")) {
                    Job identifiedJob = null;

                    identifiedJob = new Job(di, jobTitle, jobAnnouncer, jobURL, jobDate, jobSmallText, 1, city, foundAt, new ArrayList());

                    Field f = new Field(jobCategory);
                    identifiedJob.addField(f);
                    identifiedJob = di.getFilter().filterTitleForPossibleChangeInFields(identifiedJob, jobCategory, jobTitle);

                    jobs.add(identifiedJob);
                    METRICS.incrementJobsInEnglish();
                } else {

                    // Open connection to joblink
                    Document internalDocument = null;
                    try {
                        internalDocument = Jsoup.connect(jobURL).timeout(20000).get();
                        internalDocument.select("head,img,script,href").remove();
                        String textToDetect = "";

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
                            danish = !isEnglish(textToDetect); // and if is english

                        }

                        // check if ad required danish even if its written in English
                        if (di.getFilter().checkIfRequiresDanish(textToDetect)) {
                            // System.out.println("Danish requirement:" + internalDocument.baseUri());
                            danish = true;
                        }

                        if (danish == false) {
                            System.out.println("________________________________________________" + "\r\n");

                            Job identifiedJob = new Job(di, jobTitle, jobAnnouncer, jobURL, jobDate, jobSmallText, 1, city, foundAt, new ArrayList());
                            Field f = new Field(jobCategory);
                            identifiedJob.addField(f);
                            identifiedJob = di.getFilter().filterTitleForPossibleChangeInFields(identifiedJob, jobCategory, jobTitle);

                            jobs.add(identifiedJob);
                            METRICS.incrementJobsInEnglish();
                        }
                    } catch (IOException e) {
                        e.printStackTrace(System.out);
                        METRICS.incrementExceptions();
                    }
                }
            }

            danish = false;
        }
        return jobs;
    }

    private ArrayList<Job> scanDocForExternal(Document doc, String tag, String city, String foundAt) throws ParseException {

        ArrayList<Job> jobs = new ArrayList<>();

        // Get unpaid ad divs
        Elements externalJobsDivs = doc.select("div.jix_robotjob");
        METRICS.setAllJobs(METRICS.getAllJobs() + externalJobsDivs.size());

        for (Element externalJobDiv : externalJobsDivs) {
            // Remove images
            Elements media = externalJobDiv.select("[src]");
            for (Element src : media) {
                src.remove();
            }

            // Get job title
            Elements links = externalJobDiv.select("a[href]");
            Element joblink = links.get(0);
            String jobTitle = joblink.text();

            // Most small ads have jobAnnouncer and place in bold in this format : Announcer, place.
            // So, in case the ad has bolds,take them, store them, clear them from text.
            // If it doesnt, store placeholders
            String jobAnnouncer = "";
            String place = "";
            Elements bolds = externalJobDiv.select("b");
            if (bolds.size() == 2) {// get job announcer and place from bold tags in order to clear them from text later
                Element firstbold = bolds.get(0);
                Element secondbold = bolds.get(1);
                jobAnnouncer = firstbold.text();
                place = secondbold.text();
            } else {
                jobAnnouncer = externalJobDiv.select("CITE").first().text().split(", ")[0];
                //System.out.println(jobAnnouncer);
                place = "";
            }

            // Put date in String in order to clear it from text later
            String Date = "";
            try {
                Date = externalJobDiv.select("CITE").first().outerHtml().split(", ")[1];
            } catch (Exception e) {
                e.printStackTrace(System.out);
                METRICS.incrementExceptions();
                // System.out.println("Exception in CITE date getting. Possible cause: No date or comma seperator in CITE tag");
            }

            // Get ad text
            String jobText = "";
            try {
                jobText = externalJobDiv.text().substring(joblink.text().length() + 1, externalJobDiv.text().indexOf(" Spara jobb"))
                        .replaceAll(jobAnnouncer, "").replaceAll(place, "").replaceAll(Date, "");
            } catch (Exception e) {
                e.printStackTrace(System.out);
                METRICS.incrementExceptions();
            }

            // Detect if div text is english or danish
            if (!isEnglish(jobText)) {
                // System.out.println("LANGUAGE NOT ENGLISH " + jobText);
            } else {
                    // Language seems to be English

                // Get date that the ad was posted
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
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                    METRICS.incrementExceptions();
                    // Put todays date
                    jobDate = new Date();
                }

                // Get ad link url
                String jobURL = null;
                try {
                    try {
                        jobURL = sanitizeUrl(joblink.attr("abs:href"));
                    } catch (UnsupportedEncodingException uee) {
                        uee.printStackTrace(System.out);
                        METRICS.incrementExceptions();
                        continue;
                    }
                    jobURL = jobURL.split("&url=")[1];

                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    aioobe.printStackTrace(System.out);
                    METRICS.incrementExceptions();
                }

                // Check if link leads to a pdf, or a ashx and handle them here as java will not open it
                if (jobURL.contains(".pdf") || jobURL.contains(".ashx")) {
                    Job identifiedJob = null;

                    identifiedJob = new Job(di, jobTitle, jobAnnouncer, jobURL, jobDate, jobText, 0, city, foundAt, new ArrayList());

                    Field f = new Field(jobCategory);
                    identifiedJob.addField(f);
                    identifiedJob = di.getFilter().filterTitleForPossibleChangeInFields(identifiedJob, jobCategory, jobTitle);

                    jobs.add(identifiedJob);
                    METRICS.incrementJobsInEnglish();
                } else {

                    // Open connection to joblink
                    Document internalDocument = null;
                    try {
                        internalDocument = Jsoup.connect(jobURL).timeout(20000).get();
                        internalDocument.select("head,img,script,href").remove();
                        String textToDetect = "";

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
                            // check for danish
                            danish = !isEnglish(textToDetect);
                        }

                        // Check if ad required danish even if its written in English
                        if (di.getFilter().checkIfRequiresDanish(textToDetect)) {
                            // System.out.println("Danish requirement:" + internalDocument.baseUri());
                            danish = true;
                        }

                        if (danish == false) {
                            System.out.println("________________________________________________" + "\r\n");

                            Job identifiedJob = new Job(di, jobTitle, jobAnnouncer, jobURL, jobDate, jobText, 0, city, foundAt, new ArrayList());
                            Field f = new Field(jobCategory);
                            identifiedJob.addField(f);
                            identifiedJob = di.getFilter().filterTitleForPossibleChangeInFields(identifiedJob, jobCategory, jobTitle);

                            jobs.add(identifiedJob);
                            METRICS.incrementJobsInEnglish();
                        }
                    } catch (IOException e) {
                        METRICS.incrementExceptions();
                        e.printStackTrace(System.out);
                    }
                }
            }

            danish = false;
        }
        return jobs;

    }

    /**
     * @param input string to replace
     * @return string without spaces
     */
    private String removeSpaces(String input) {
        input = input.replaceAll("^\\s*", "");
        input = input.trim();
        return input;
    }

    /**
     * String to date formatter : d MMMM yyyy with locale DA
     *
     * @param mystr the string to format into date
     * @return the date
     */
    private Date convertSTRtoDATE(String mystr) {
        try {
            Date date = new SimpleDateFormat("d MMMM yyyy", new Locale("sv", "SE")).parse(mystr);
            return date;
        } catch (ParseException e) {
            METRICS.incrementExceptions();
            e.printStackTrace(System.out);
            return null;
        }

    }
}
