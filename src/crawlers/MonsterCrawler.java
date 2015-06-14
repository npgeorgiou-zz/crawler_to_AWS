package crawlers;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import crawlerUtils.LangDetect;
import model.Metrics;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Field;
import model.Job;

public class MonsterCrawler extends BaseCrawler {

    // declare variables

    // constructor
    public MonsterCrawler(String URL, String area, String jobCategory, String foundAt, LangDetect languageDetector) throws IOException {
        super(URL, area, jobCategory, foundAt, languageDetector);
    }

    public Metrics scan() {
        // sanitize url
        URL = URL.replaceAll("ø", "%C3%B8")
                .replaceAll("å", "%C3%A5")
                .replaceAll("æ", "%C3%A6")
                .replaceAll("ä", "%C3%A4")
                .replaceAll("ö", "%C3%B6");
        Document doc = new Document("");
        try {
            doc = Jsoup.connect(URL).timeout(200000).followRedirects(true).get();
            // System.out.println("############ Connected to: " + URL);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            METRICS.incrementExceptions();
            Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, e);
        }
        while (true) {
            try {

                // read page and return arrayList of paid Jobs
                ArrayList<Job> paidJobs = scanPage(doc, "odd", "even", area, foundAt);

                // save Jobs
                int duplicateJobs = dbUtils.deleteDuplicatesAndAddtoDB(paidJobs);
                METRICS.setDuplicateJobs(METRICS.getDuplicateJobs() + duplicateJobs);
            } catch (ParseException pe) {
                METRICS.incrementExceptions();
                pe.printStackTrace(System.out);
            }
            // connect to next pages, if there are any
            try {
                Element paginationDiv = doc.getElementsByClass("navigationBar").first();

                // if there is pagination, therefore if there are other pages
                if (paginationDiv != null) {
                    Element aWithClassLast = paginationDiv.getElementsByClass("last").first();
                    if (aWithClassLast == null) {
                        //this isnt the last page
                        // System.out.println("this isnt the last page");
                        URL = paginationDiv.select("a").last().attr("abs:href");
                    } else {
                        // END
                        break;
                    }

                } else {
                    // END
                    break;
                }

            } catch (Exception e) {
                METRICS.incrementExceptions();
                e.printStackTrace(System.out);
            }
            URL = URL.replaceAll("ø", "%C3%B8")
                    .replaceAll("å", "%C3%A5")
                    .replaceAll("æ", "%C3%A6")
                    .replaceAll("ä", "%C3%A4")
                    .replaceAll("ö", "%C3%B6");
            try {
                doc = Jsoup.connect(URL).timeout(200000).followRedirects(true).get();
            } catch (IOException e) {
                METRICS.incrementExceptions();
                e.printStackTrace(System.out);
                Logger.getLogger(JobindexCrawler.class.getName()).log(Level.SEVERE, null, e);
            }
        }
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
        METRICS.setAllJobs(METRICS.getAllJobs() + allRows.size());

        // scan all rows
        for (Element paidJobRow : allRows) {

            // get ad link url
            String jobURL = paidJobRow.select("a[href]").first().attr("abs:href");

            // Open connection to joblink
            Document internalDocument = null;
            try {
                internalDocument = Jsoup.connect(jobURL).timeout(20000).get();
                Element internalDocumentBody = internalDocument.select("body").first();

                // if page has text content
                if (!internalDocumentBody.text().trim().isEmpty()) {

                    //take main div that holds ad text
                    String mainText = getMainDiv(internalDocumentBody);

                    // check for english
                    danish = !isEnglish(mainText);

                    // check if ad required danish even if its written in English
                    if (filter.checkIfRequiresDanish(mainText)) {
                        // System.out.println("Danish requirement:" + internalDocument.baseUri());
                        danish = true;
                    }

                    if (danish == false) {

                        // job is what we are looking for
                        // now we try to bypass monster internal pages, if we can
                        String realURL = "";
                        Elements scriptElement = internalDocument.getElementsByTag("script");
                        for (Element s : scriptElement) {
                            if (s.html().contains("ApplyOnlineUrl: ")) {
                                realURL = s.html().split("ApplyOnlineUrl: '")[1].split("',")[0];

                                //if realURL is not an internal monster url, or is not like : ApplyOnlineUrl: ''
                                if (!(realURL.contains("mit.monster.dk") || realURL.contains("mitt.monster.se")) 
                                        && realURL.length() != 0) {
                                    jobURL = realURL;
                                }
                            }
                        }

                        // get info that we need
                        // get job title
                        String jobTitle = paidJobRow.select("div[class = jobTitleContainer]").text();

                        // get company
                        String jobAnnouncer = paidJobRow.select("div[class = companyContainer]").select("a[href]").attr("title");

                        // get publication date
                        String daysPublished = paidJobRow.select("div[class = fnt20]").text()
                                .replace("Publiceret: ", "")
                                .replace(" dage siden", "")
                                .replace("Publicerad: ", "")
                                .replace(" dagar sedan", "");

                        Calendar cal = Calendar.getInstance();
                        Date jobDate;
                        if (daysPublished.equals("I dag")) {
                            jobDate = cal.getTime();
                        } else {
                            int daysPublishedAsInt = Integer.parseInt(daysPublished);
                            cal.add(Calendar.DATE, -daysPublishedAsInt);
                            jobDate = cal.getTime();
                        }
                        System.out.println("________________________________________________" + "\r\n");

                        // temporarily set text as "" so as not to overload DB
                        mainText = "";

                        Job identifiedJob = new Job(jobTitle, jobAnnouncer, jobURL, jobDate, mainText, 1, city, foundAt, new ArrayList());
                        Field f = new Field(jobCategory);
                        identifiedJob.addField(f);
                        identifiedJob = filter.filterTitleForPossibleChangeInFields(identifiedJob, jobCategory, jobTitle);

                        jobs.add(identifiedJob);
                        METRICS.incrementJobsInEnglish();
                    } else {
//                            System.out.println("LANGUAGE NOT ENGLISH " + mainText);
                    }
                }

            } catch (IOException e) {
                METRICS.incrementExceptions();
                e.printStackTrace(System.out);
            }
        }
        return jobs;
    }

    private String getMainDiv(Element internalDocumentBody) {
        Element mainDiv = internalDocumentBody.select("div[itemprop = description]").first();

        //if this page format doesnt exist, then maindiv is <div id="jobBodyContent">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[id = jobBodyContent]").first();
        }

        //if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[id = CJT-jobBodyContent]").first();
        }

        //if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[id = CJT-jobdesc]").first();
        }

        //if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[id = contentleft]").first();
        }

        //if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[id = left_wrapper]").first();
        }

        //if this page format doesnt exist, then maindiv is <div id="CJT-jobBodyContent">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[id = left_column]").first();
        }

        //if this page format doesnt exist, then maindiv is <div id="TrackingJobBody">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("span[id = TrackingJobBody]").first();
        }

        //if this page format doesnt exist, then maindiv is <div id="mycontent">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[id = textWrap]").first();
        }

        //if this page format doesnt exist, then maindiv is <div id="mycontent">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[id = mycontent]").first();
        }

        //if this page format doesnt exist, then maindiv is <div class = "jDespHolder">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[id = jobdesc]").first();
        }

        //if this page format doesnt exist, then maindiv is <div class = "jDespHolder">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("div[class = main]").first();
        }

        //if this page format doesnt exist, then maindiv is <span itemprop="description">
        if (mainDiv == null) {
            mainDiv = internalDocumentBody.select("span[itemprop = description]").first();
        }

        //if this page format doesnt exist, set whole doc
        if (mainDiv == null) {
            mainDiv = internalDocumentBody;
        }

        // if the text of the mainDiv is nothing, put whole document text as text
        String mainText = mainDiv.text();

        //if this page format doesnt exist, set whole doc
        if (mainText.equals("")) {
            mainText = internalDocumentBody.text();
        }

        return mainText;
    }
}
