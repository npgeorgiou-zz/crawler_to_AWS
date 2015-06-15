package controllers;

import model.Metrics;
import crawlers.JobnetCrawler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JobnetController extends BaseController{

    // declare variables
    Map<String, String> AC;
    Map<String, String> FC;
    String[] areas;

    
    // constructor
    public JobnetController(String jobsiteName) {
        super(jobsiteName);

        setUpAreaCodes();
        setUpFieldCodes();
        areas = AC.keySet().toArray(new String[AC.size()]);
    }

    // methods
    public Metrics start() {
//        //scan engineer ######################################################################
//        //nothing like this on this site
//        try {
//            String[] fields = {"Bygge og anlæg"};
//
//            for (String area : areas) {
//                for (String field : fields) {
//                    String URL = "https://job.jobnet.dk/FindJobService/V1/Gateway.ashx/annonce?region=" + AC.get(area) + "&erhvervsomraade=" + FC.get(field) + "&start=1&antal=5000&sortering=publicering&format=json";
//                    Crawler crawler = new Crawler(URL, field, area, "engineer");
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
                    JobnetCrawler crawler = new JobnetCrawler(URL, area, IT, jobsiteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                    JobnetCrawler crawler = new JobnetCrawler(URL, area, SERVICE, jobsiteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                    JobnetCrawler crawler = new JobnetCrawler(URL, area, SERVICE, jobsiteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                    JobnetCrawler crawler = new JobnetCrawler(URL, area, BUSINESS_OFFICE, jobsiteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                    JobnetCrawler crawler = new JobnetCrawler(URL, area, RES_EDU, jobsiteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                    JobnetCrawler crawler = new JobnetCrawler(URL, area, BUSINESS_OFFICE, jobsiteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                    JobnetCrawler crawler = new JobnetCrawler(URL, area, MED_SOC, jobsiteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                    JobnetCrawler crawler = new JobnetCrawler(URL, area, LEADERSHIP, jobsiteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan oevrige ######################################################################
        //#######nothing like this in this site

        
        //metrics
        System.out.println(METRICS.toString()); 
        return METRICS;
    }
    
    private void setUpAreaCodes() {
        AC = new HashMap<>();
        AC.put("Copenhagen", "1084"); // Hovedstaden og Bornholm
        AC.put("Zealand", "1085"); // Øvrige Sjælland
        AC.put("Middle Jylland", "1082"); // Midtjylland
        AC.put("North Jylland", "1081"); // Nordjylland
        AC.put("South Denmark", "1083"); // Syddanmark
        AC.put("Greenland & Faroe", "9999"); // Grønland
    }
    
    private void setUpFieldCodes() {   
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
    }
}
