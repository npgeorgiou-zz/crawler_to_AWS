package controllers;

import model.Metrics;
import crawlers.MonsterCrawler;
import com.cybozu.labs.langdetect.LangDetectException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import crawlerUtils.LangDetect;
import sharedUtilities.JobCategories;

class MonsterController {
    //declare variables

    private static LangDetect languageDetector;
    private static final String profilesPath = "C:\\Users\\ksptsinplanet\\git\\crawler\\Crawler\\profiles";

    private final Metrics METRICS;

    Map<String, String> AC;
    String[] areas;

    //constructor
    public MonsterController() {
        METRICS = new Metrics("Monster");

        setUpLangDetector();

        AC = new HashMap<>();
        AC.put("Copenhagen", "Hovedstaden");
        AC.put("Zealand", "Sjælland");
        AC.put("North Jylland", "Nordjylland");
        AC.put("Middle Jylland", "Midtjylland");
        AC.put("South Denmark", "Syddanmark");

        areas = AC.keySet().toArray(new String[AC.size()]);

    }

    //methods
    public Metrics start() {
        //scan engineer ######################################################################
        try {
            String field = "Ingeniørarbejde";

            for (String area : areas) {
                String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                MonsterCrawler crawlerInstance = new MonsterCrawler(URL, area, JobCategories.ENGINEER, "Monster", languageDetector);
                Metrics m = crawlerInstance.scan();
                updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan it ######################################################################
        try {
            String field = "IT-softwareudvikling";

            for (String area : areas) {
                String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                MonsterCrawler crawlerInstance = new MonsterCrawler(URL, area, JobCategories.IT, "Monster", languageDetector);
                Metrics m = crawlerInstance.scan();
                updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan ledelse ######################################################################
        try {
            String fields[] = {"Projekt-programstyring", "Ledelse-Management"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                    MonsterCrawler crawlerInstance = new MonsterCrawler(URL, area, JobCategories.LEADERSHIP, "Monster", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan handel ######################################################################
        try {
            String fields[] = {"Kundesupport-kundepleje", "Bespisning-indkvartering"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                    MonsterCrawler crawlerInstance = new MonsterCrawler(URL, area, JobCategories.SERVICE, "Monster", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan industri ######################################################################
        try {
            String fields[] = {"Produktion-drift", "Logistik-transport", "Byggeri-håndværk", "Installation-vedligeholdelse-reparation", "Kvalitetssikring-sikkerhed"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                    MonsterCrawler crawlerInstance = new MonsterCrawler(URL, area, JobCategories.SERVICE, "Monster", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan salg ######################################################################
        try {
            String fields[] = {"Salgs-forretningsudvikling", "Markedsføring", "Kreativ-funktion-design", "Journalistik"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                    MonsterCrawler crawlerInstance = new MonsterCrawler(URL, area, JobCategories.BUSINESS, "Monster", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan undervisning ######################################################################
        try {
            String field = "Forskning-og-udvikling-videnskab";

            for (String area : areas) {
                String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                MonsterCrawler crawlerInstance = new MonsterCrawler(URL, area, JobCategories.RES_EDU, "Monster", languageDetector);
                Metrics m = crawlerInstance.scan();
                updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan kontor ######################################################################
        try {

            String fields[] = {"Finansiering-og-regnskab", "Personale", "Juridisk", "Administration-kontor"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                    MonsterCrawler crawlerInstance = new MonsterCrawler(URL, area, JobCategories.BUSINESS, "Monster", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

//        //scan social ######################################################################
//        //#######nothing like this in this site
//        try {
//            String field = "Finansiering-og-regnskab";
//            
//
//            for (String area : areas) {
//                MonsterCrawler crawlerInstance = new MonsterCrawler("http://jobsoeg.monster.dk",area, "medicalAndSocial");                    CrawlerMetrics m = crawlerInstance.scan();
//
//                    allJobs += m.getAllJobs();
//                    jobsInEnglish += m.getJobsInEnglish();
//                    duplicateJobs += m.getDuplicateJobs();
//                    
//                    exceptions += m.getExceptions();
//            }
//
//        } catch (IOException e1) {
//            e1.printStackTrace(System.out);
//        }
//        //scan oevrige ######################################################################
//        //#######nothing like this in this site
//        try {
//            String fields[] = {"frivilligt", "kontorelev", "student", "studiepraktik", "oevrige", "elev", "kurseroevrige"};                    CrawlerMetrics m = crawlerInstance.scan();
//
//                    allJobs += m.getAllJobs();
//                    jobsInEnglish += m.getJobsInEnglish();
//                    duplicateJobs += m.getDuplicateJobs();
//                    
//                    exceptions += m.getExceptions();
//            
//            for (String field : fields) {
//                for (String area : areas) {
//                    MonsterCrawler crawlerInstance = new MonsterCrawler("http://www.jobindex.dk/job/oevrige",area, "studentAndInternshipAndOther");                    CrawlerMetrics m = crawlerInstance.scan();
//
//                    allJobs += m.getAllJobs();
//                    jobsInEnglish += m.getJobsInEnglish();
//                    duplicateJobs += m.getDuplicateJobs();
//                    
//                    exceptions += m.getExceptions();
//                }
//            }
//        } catch (IOException e1) {
//            e1.printStackTrace(System.out);
//        }
       

        //metrics
        System.out.println("METRICS:");
        System.out.println("Jobs in site: " + "\t" + METRICS.getAllJobs());
        System.out.println("Jobs in english: " + "\t" + METRICS.getJobsInEnglish());
        System.out.println("Duplicates: " + "\t" + METRICS.getDuplicateJobs());
        System.out.println("Exceptions: " + "\t" + METRICS.getExceptions());

        //set metrics to return
        METRICS.setAllJobs(METRICS.getAllJobs());
        METRICS.setJobsInEnglish(METRICS.getJobsInEnglish());
        METRICS.setDuplicateJobs(METRICS.getDuplicateJobs());
        METRICS.setExceptions(METRICS.getExceptions());
        return METRICS;
    }

    private void updateMetrics(Metrics m, int allJobs, int jobsInEnglish, int duplicateJobs, int exceptions) {
        m.setAllJobs(METRICS.getAllJobs() + allJobs);
        m.setJobsInEnglish(METRICS.getJobsInEnglish() + jobsInEnglish);
        m.setDuplicateJobs(METRICS.getDuplicateJobs() + duplicateJobs);
        m.setExceptions(METRICS.getExceptions() + exceptions);
    }

    //Initialize the language detector with the profiles
    private void setUpLangDetector() {
        languageDetector = new LangDetect();
        try {
            languageDetector.init(profilesPath);
        } catch (LangDetectException lde) {
            lde.printStackTrace(System.out);
        }
    }
}
