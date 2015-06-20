package controllers;

import model.Metrics;
import crawlers.JobbsafariCrawler;
import di.DI;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JobbsafariController extends BaseController {

    // declare variables
    Map<String, String> AC;
    String[] areas;

    // constructor
    public JobbsafariController(String jobsiteName, DI di) {
        super(jobsiteName, di);

        setUpAreaCodes();
        areas = AC.keySet().toArray(new String[AC.size()]);
    }

    // methods
    public Metrics start() {
        //scan engineer ######################################################################
        try {
            String fields[] = {"byggteknik", "teknikchefer", "elektronik", "kemi", "lakemedel", "maskinteknik", "produktionsteknik"};

            for (String field : fields) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/teknik" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, ENGINEER, jobsiteName);
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
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, IT, jobsiteName);
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
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, SERVICE, jobsiteName);
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
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, SERVICE, jobsiteName);
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
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, BUSINESS_OFFICE, jobsiteName);
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
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, RES_EDU, jobsiteName);
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
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, BUSINESS_OFFICE, jobsiteName);
                    Metrics m = crawler.scan();
                    METRICS.updateMetrics(m);
                }
            }

            for (String field : fields2) {
                for (String area : areas) {
                    String URL = "http://www.jobbsafari.se/jobb/management" + "/" + field + "/" + AC.get(area);
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, BUSINESS_OFFICE, jobsiteName);
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
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, MED_SOC, jobsiteName);
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
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, STUDENT, jobsiteName);
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
                    JobbsafariCrawler crawler = new JobbsafariCrawler(di, URL, area, LEADERSHIP, jobsiteName);
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

    private void setUpAreaCodes() {
        AC = new HashMap<>();
        AC.put("Scania", "skane");
    }
}
