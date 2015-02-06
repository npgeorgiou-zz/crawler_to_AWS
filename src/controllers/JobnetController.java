package controllers;

import model.Metrics;
import crawlers.JobnetCrawler;
import com.cybozu.labs.langdetect.LangDetectException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import crawlerUtils.LangDetect;
import sharedUtilities.JobCategories;

class JobnetController {

    //declare variables
    private LangDetect languageDetector;
    private static final String profilesPath = "C:\\Users\\ksptsinplanet\\git\\crawler\\Crawler\\profiles";

    private final Metrics METRICS;

    Map<String, String> AC;
    Map<String, String> FC;
    String[] areas;

    //constructor
    public JobnetController() {
        METRICS = new Metrics("Jobnet");

        setUpLangDetector();

        AC = new HashMap<>();
        AC.put("Copenhagen", "1084"); // Hovedstaden og Bornholm
        AC.put("Zealand", "1085"); // Øvrige Sjælland
        AC.put("Middle Jylland", "1082"); // Midtjylland
        AC.put("North Jylland", "1081"); // Nordjylland
        AC.put("South Denmark", "1083"); // Syddanmark
        AC.put("Greenland & Faroe", "9999"); // Grønland

        FC = new HashMap<>();
        //FC.put("Hotjob", "999"); add &hotjob=1 to URL for this
        FC.put("Bygge og anlæg", "40000");
        FC.put("Design, formgivning og grafisk arbejde", "150000");
        FC.put("Elever", "230000");
        FC.put("Hotel, restauration, køkken, kantine", "130000");
        FC.put("Industriel produktion", "60000");
        FC.put("It og teleteknik", "110000");
        FC.put("Jern, metal og auto", "50000");
        FC.put("Kontor, administration, regnskab og finans", "80000");
        FC.put("Landbrug, skovbrug, gartneri, fiskeri og dyrepleje", "170000");
        FC.put("Ledelse", "220000");
        FC.put("Medie, kultur, turisme, idr æt og underholdning", "100000");
        FC.put("Nærings- og nydelsesmiddel", "190000");
        FC.put("Pædagogisk, socialt og kirkeligt arbejde", "30000");
        FC.put("Rengøring, ejendomsservice og renovation", "140000");
        FC.put("Salg, indkøb og markedsføring", "90000");
        FC.put("Sundhed, omsorg og personlig pleje", "10000");
        FC.put("Tekstil og bekl ædning", "180000");
        FC.put("Transport, post, lager- og maskinførerarbejde", "120000");
        FC.put("Træ, møbel, glas og keramik", "200000");
        FC.put("Undervisning og vejledning", "20000");
        FC.put("Vagt, sikkerhed og overvågning", "160000");

        areas = AC.keySet().toArray(new String[AC.size()]);
    }

    //methods
    public Metrics start() {
//        //scan engineer ######################################################################
//        //nothing like this on this site
//        try {
//            String[] fields = {"Bygge og anlæg"};
//
//            for (String area : areas) {
//                for (String field : fields) {
//                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
//                    Crawler crawlerInstance = new Crawler(URL, field, area, "engineer");
//
//                }
//            }
//
//        } catch (IOException e1) {
//            e1.printStackTrace(System.out);
//        }

        //scan it ######################################################################
        try {
            String[] fields = {"It og teleteknik"};

            for (String area : areas) {
                for (String field : fields) {
                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
                    JobnetCrawler crawlerInstance = new JobnetCrawler(URL, area, JobCategories.IT, "Jobnet", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan handel ######################################################################
        try {
            String[] fields = {"Hotel, restauration, køkken, kantine", "Nærings- og nydelsesmiddel",
                "Rengøring, ejendomsservice og renovation", "Vagt, sikkerhed og overvågning"};

            for (String area : areas) {
                for (String field : fields) {
                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
                    JobnetCrawler crawlerInstance = new JobnetCrawler(URL, area, JobCategories.SERVICE, "Jobnet", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan industri ######################################################################
        try {
            String[] fields = {"Bygge og anlæg", "Industriel produktion", "Jern, metal og auto",
                "Landbrug, skovbrug, gartneri, fiskeri og dyrepleje", "Tekstil og bekl ædning",
                "Transport, post, lager- og maskinførerarbejde", "Træ, møbel, glas og keramik"};

            for (String area : areas) {
                for (String field : fields) {
                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
                    JobnetCrawler crawlerInstance = new JobnetCrawler(URL, area, JobCategories.SERVICE, "Jobnet", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan salg ######################################################################
        try {
            String[] fields = {"Salg, indkøb og markedsføring", "Design, formgivning og grafisk arbejde"};

            for (String area : areas) {
                for (String field : fields) {
                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
                    JobnetCrawler crawlerInstance = new JobnetCrawler(URL, area, JobCategories.BUSINESS, "Jobnet", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan undervisning ######################################################################
        try {
            String[] fields = {"Undervisning og vejledning"};

            for (String area : areas) {
                for (String field : fields) {
                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
                    JobnetCrawler crawlerInstance = new JobnetCrawler(URL, area, JobCategories.RES_EDU, "Jobnet", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

//        //scan kontor ######################################################################
        try {
            String[] fields = {"Kontor, administration, regnskab og finans"};

            for (String area : areas) {
                for (String field : fields) {
                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
                    JobnetCrawler crawlerInstance = new JobnetCrawler(URL, area, JobCategories.BUSINESS, "Jobnet", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan social ######################################################################
        try {
            String[] fields = {"Sundhed, omsorg og personlig pleje", "Pædagogisk, socialt og kirkeligt arbejde"};

            for (String area : areas) {
                for (String field : fields) {
                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
                    JobnetCrawler crawlerInstance = new JobnetCrawler(URL, area, JobCategories.MED_SOC, "Jobnet", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan ledelse ######################################################################
        try {
            String[] fields = {"Ledelse"};

            for (String area : areas) {
                for (String field : fields) {
                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
                    JobnetCrawler crawlerInstance = new JobnetCrawler(URL, area, JobCategories.LEADERSHIP, "Jobnet", languageDetector);
                    Metrics m = crawlerInstance.scan();
                    updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

//        //scan oevrige ######################################################################
        //#######nothing like this in this site
//        try {
//            String fields[] = {"frivilligt", "kontorelev", "student", "studiepraktik", "oevrige", "elev", "kurseroevrige"};
//            
//            for (String field : fields) {
//                for (String area : areas) {
//                    Crawler crawlerInstance = new Crawler("http://www.jobindex.dk/job/oevrige", field, area, "studentAndInternshipAndOther");
//                }
//            }
//        } catch (IOException e1) {
//            e1.printStackTrace(System.out);
//        }
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
