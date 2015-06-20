package controllers;

import model.Metrics;
import crawlers.MonsterCrawler;
import di.DI;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MonsterController extends BaseController {

    // declare variables
    Map<String, String> AC;
    String[] areas;

    // constructor
    public MonsterController(String jobsiteName, DI di) {
        super(jobsiteName, di);
        
        setUpAreaCodes();
        areas = AC.keySet().toArray(new String[AC.size()]);
    }

    // methods
    public Metrics start() {
        //scan engineer ######################################################################
        try {
            String field = "Ingeniørarbejde";

            for (String area : areas) {
                String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                MonsterCrawler crawler = new MonsterCrawler(di, URL, area, ENGINEER, jobsiteName);
                Metrics m = crawler.scan();
                METRICS.updateMetrics(m);
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan it ######################################################################
        try {
            String field = "IT-softwareudvikling";

            for (String area : areas) {
                String URL = "http://jobsoeg.monster.dk" + "/" + AC.get(area) + "+" + field + "_14";
                MonsterCrawler crawler = new MonsterCrawler(di, URL, area, IT, jobsiteName);
                Metrics m = crawler.scan();
                METRICS.updateMetrics(m);
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
                    MonsterCrawler crawler = new MonsterCrawler(di, URL, area, SERVICE, jobsiteName);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                    MonsterCrawler crawler = new MonsterCrawler(di, URL, area, SERVICE, jobsiteName);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                    MonsterCrawler crawler = new MonsterCrawler(di, URL, area, BUSINESS_OFFICE, jobsiteName);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
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
                MonsterCrawler crawler = new MonsterCrawler(di, URL, area, RES_EDU, jobsiteName);
                Metrics m = crawler.scan();
                METRICS.updateMetrics(m);
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
                    MonsterCrawler crawler = new MonsterCrawler(di, URL, area, BUSINESS_OFFICE, jobsiteName);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
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
                    MonsterCrawler crawler = new MonsterCrawler(di, URL, area, LEADERSHIP, jobsiteName);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan social ######################################################################
        //#######nothing like this in this site
        //scan oevrige ######################################################################
        //#######nothing like this in this site
        //metrics
        System.out.println(METRICS.toString());
        return METRICS;
    }

    private void setUpAreaCodes() {
        AC = new HashMap<>();
        AC.put("Copenhagen", "Hovedstaden");
        AC.put("Zealand", "Sjælland");
        AC.put("North Jylland", "Nordjylland");
        AC.put("Middle Jylland", "Midtjylland");
        AC.put("South Denmark", "Syddanmark");
    }
}
