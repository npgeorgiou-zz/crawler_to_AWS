package crawlers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import crawlerUtils.LangDetect;
import com.google.gson.Gson;
import model.Metrics;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.Job;
import model.Field;

public class JobnetCrawler extends BaseCrawler {

    // declare variables
    private Gson gson;

    // constructor
    public JobnetCrawler(String URL, String area, String jobCategory, String foundAt, LangDetect languageDetector) throws IOException {
        super(URL, area, jobCategory, foundAt, languageDetector);
        gson = new Gson();
    }

    public Metrics scan() {

        // connect to URL
        String cleanURL = URL.replaceAll("ø", "%C3%B8")
                .replaceAll("å", "%C3%A5")
                .replaceAll("æ", "%C3%A6");

        //ignore content type to get the json
        Document doc = getDocument(cleanURL, true);

        ArrayList<Job> jobs = new ArrayList<>();
        String reply = doc.text();
        jsonReply r = gson.fromJson(reply, jsonReply.class);

        int allJobs = Integer.parseInt(r.NumberOfMatches);
        METRICS.setAllJobs(METRICS.getAllJobs() + allJobs);

        for (jsonJob j : r.JobPostingDigests) {

            if (isEnglish(j.Body.toLowerCase())) {
                System.out.println(j.Body.toLowerCase());
                System.out.println("_________________________________________________");
                // Open connection to joblink
                String jobURL = "https://job.jobnet.dk/CV/FindJob/details/" + j.Id;

                Document internalDocument = null;
                internalDocument = getDocument(jobURL, true);
                Elements internalDocumentBody = internalDocument.select("body");

                if (!internalDocumentBody.text().trim().isEmpty()) {
                    String jobText = internalDocument.select("div#DetailsJobDescription").text();

                    //if we found content in ad
                    if (!jobText.equals("")) {
                        // and if is english
                        danish = !isEnglish(jobText);

                        // check if ad required danish even if its written in English
                        if (filter.checkIfRequiresDanish(jobText)) {
                            danish = true;
                        }

                        // dont take any vestas jobs. The url is jobnet url, and they format the title in a way
                        // that doenst allow duplicate filtering. I get Vestas from the other sites anyways.
                        // get job title
                        String company = j.HiringOrgName;
                        if (company.toLowerCase().contains("vestas")) {
                            danish = true;
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

                            // temporarily set text as ""
                            jobText = "";

                            Job identifiedJob = new Job(jobTitle, jobAnnouncer, jobURL, jobDate, jobText, 1, area, foundAt, new ArrayList());
                            Field f = new Field(jobCategory);
                            identifiedJob.addField(f);
                            identifiedJob = filter.filterTitleForPossibleChangeInFields(identifiedJob, jobCategory, jobTitle);

                            jobs.add(identifiedJob);
                            METRICS.incrementJobsInEnglish();
                        } else {
                            // System.out.println("LANGUAGE NOT ENGLISH " + jobText);
                        }
                    } else {
                        // System.out.println("NO CONTENT IN AD");
                    }
                }

            } else {
                // System.out.println("LANGUAGE NOT ENGLISH " + j.Body);
            }

        }
        // save Jobs
        int duplicateJobs = dbUtils.deleteDuplicatesAndAddtoDB(jobs);
        METRICS.setDuplicateJobs(METRICS.getDuplicateJobs() + duplicateJobs);

        return METRICS;
    }

    // set flag ignoreContentType to true ignores content type, allows us to take JSON
    private Document getDocument(String url, boolean ignoreContentType) {
        Document doc = new Document("");
        try {
            doc = Jsoup.connect(url)
                    .ignoreContentType(ignoreContentType)
                    .timeout(20000)
                    .followRedirects(true)
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
}
