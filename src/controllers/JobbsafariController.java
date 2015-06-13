package controllers;

import model.Metrics;
import crawlers.JobbsafariCrawler;
import com.cybozu.labs.langdetect.LangDetectException;
import config.Config;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import crawlerUtils.LangDetect;
import sharedUtilities.JobCategories;

public class JobbsafariController {

    //declare variables
    private final String siteName;
    private LangDetect languageDetector;
    private final Metrics METRICS;

    Map<String, String> AC;
    String[] areas;

    //constructor
    public JobbsafariController() {
        siteName = "Jobbsafari";
        METRICS = new Metrics(siteName);

        setUpLangDetector();

        AC = new HashMap<>();
        AC.put("Scania", "skane");

        areas = AC.keySet().toArray(new String[AC.size()]);
    }

    //methods
    public Metrics start() {
        //scan engineer ######################################################################
        try {
            String fields[] = {"byggteknik", "teknikchefer", "elektronik", "kemi", "lakemedel", "maskinteknik", "produktionsteknik"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/teknik" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.ENGINEER, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan it ######################################################################
        try {
            String fields[] = {"itchefer", "databas", "affarssystem", "itdrift", "internet", "systemutveckling", "telekom"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/it" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.IT, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan handel ######################################################################
        try {
            String fields[] = {"barnpassning", "bud", "detaljhandel", "fastighetsservice", "frisor", "hotell", "rengoring", "service", "sakerhet"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/detaljhandel" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.SERVICE, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan industri ######################################################################
        try {
            String fields[] = {"bygg", "byggnadstraarbetare", "elektriker", "industri", "lager", "lantbruk", "livsmedel", "mekanik", "metall", "maling", "plat", "textil", "transport", "traindustri"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/industri" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.SERVICE, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan salg ######################################################################
        try {
            String fields[] = {"forsaljningschef", "design", "fastighetsmaglare", "franchise", "forsaljning", "grafisk", "kommunikation", "kultur", "marketing", "telemarketing"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/forsaljning" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.BUSINESS, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan undervisning ######################################################################
        try {
            String fields[] = {"bibliotek", "forskning", "forskollarare", "institution", "larare", "vuxenutbildning"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/undervisning" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.RES_EDU, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan kontor ######################################################################
        try {
            String fields[] = {"ekonomichef", "ekonomi", "fastighetsmaglare", "fastighetsservice", "finans", "inkop", "juridik", "kontor", "kontorspraktikant", "logistik", "offentlig", "sekreterare", "tolk"};
            String fields2[] = {"personal"};
            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/kontor" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.BUSINESS, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }

            for (String field : fields2) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/management" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.BUSINESS, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan social ######################################################################
        try {
            String fields[] = {"lakare", "lakarsekreterare", "offentlig", "psykologi", "sjukskoterska", "socialt", "tandlakare", "sjukvardsteknik", "terapi", "vard"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/socialt" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.MED_SOC, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan oevrige ######################################################################
        try {
            String fields[] = {"kontorspraktikant", "trainee", "praktik"};
            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/andra" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.STUDENT, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan ledelse ######################################################################
        try {
            String fields[] = {"itchefer", "ekonomichef", "teknikchefer", "freelancekonsult", "foretagsledning", "institution", "chef", "projektledning"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/management" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(URL, area, JobCategories.LEADERSHIP, siteName, languageDetector);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

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
