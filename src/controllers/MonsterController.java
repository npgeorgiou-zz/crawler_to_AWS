package controllers;

import model.Metrics;
import crawlers.MonsterCrawler;
import com.cybozu.labs.langdetect.LangDetectException;
import config.Config;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import crawlerUtils.LangDetect;
import sharedUtilities.JobCategories;

public class MonsterController {
    
    //declare variables
    private final String siteName;
    private static LangDetect languageDetector;
    private final Metrics METRICS;

    Map<String, String> AC;
    String[] areas;

    //constructor
    public MonsterController() {
        siteName = "Monster";
        METRICS = new Metrics(siteName);

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
                MonsterCrawler crawler = new MonsterCrawler(URL, area, JobCategories.ENGINEER, siteName, languageDetector);
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
                MonsterCrawler crawler = new MonsterCrawler(URL, area, JobCategories.IT, siteName, languageDetector);
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
                    MonsterCrawler crawler = new MonsterCrawler(URL, area, JobCategories.SERVICE, siteName, languageDetector);
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
                    MonsterCrawler crawler = new MonsterCrawler(URL, area, JobCategories.SERVICE, siteName, languageDetector);
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
                    MonsterCrawler crawler = new MonsterCrawler(URL, area, JobCategories.BUSINESS, siteName, languageDetector);
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
                MonsterCrawler crawler = new MonsterCrawler(URL, area, JobCategories.RES_EDU, siteName, languageDetector);
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
                    MonsterCrawler crawler = new MonsterCrawler(URL, area, JobCategories.BUSINESS, siteName, languageDetector);
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
                    MonsterCrawler crawler = new MonsterCrawler(URL, area, JobCategories.LEADERSHIP, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

//        //scan social ######################################################################
//        //#######nothing like this in this site
//        //scan oevrige ######################################################################
//        //#######nothing like this in this site
        //metrics
        System.out.println(METRICS.toString());
        return METRICS;
    }

    //Initialize the language detector with the profiles
    private void setUpLangDetector() {
        languageDetector = new LangDetect();
        try {
            String profilesPath = new Config().getProp("profiles_path");
            languageDetector.init(profilesPath);
        } catch (LangDetectException lde) {
            lde.printStackTrace(System.out);
        }
    }
}
