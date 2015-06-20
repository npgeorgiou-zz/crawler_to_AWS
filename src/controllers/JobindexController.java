package controllers;

import model.Metrics;
import crawlers.JobindexCrawler;
import di.DI;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JobindexController extends BaseController {

    // declare variables
    Map<String, String> AC;
    List<AdmDivision> allAdmDivisions;
    String[] areas;

    // constructor
    public JobindexController(String jobsiteName, DI di) {
        super(jobsiteName, di);

        setUpAdministrativeDivisions();
    }

    // methods
    public Metrics start() {
        //scan engineer ######################################################################
        try {
            String fields[] = {"byggeteknik", "elektronik", "kemi", "teknikledelse", "maskiningenioer", "medicinal", "produktionsteknik"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://tech.jobindex.dk/job/ingenioer" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, ENGINEER, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan it ######################################################################
        try {
            String fields[] = {"database", "itdrift", "itkurser", "itledelse", "internet", "systemudvikling", "telekom", "virksomhedssystemer"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://it.jobindex.dk/job/it" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, IT, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan handel ######################################################################
        try {
            String fields[] = {"bud", "boernepasning", "detailhandel", "ejendomsservice", "hotel", "rengoering", "service", "sikkerhed"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://www.jobindex.dk/job/handel" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, SERVICE, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan industri ######################################################################       
        try {
            String fields[] = {"blik", "byggeri", "elektriker", "industri", "jern", "lager", "landbrug", "maling", "mekanik", "naeringsmiddel", "tekstil", "transport", "traeindustri", "toemrer"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://www.jobindex.dk/job/industri" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, SERVICE, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan salg ######################################################################
        try {
            String fields[] = {"design", "ejendomsmaegler", "franchise", "grafisk", "kommunikation", "kultur", "marketing", "salg", "salgskurser", "salgschef", "telemarketing"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://www.jobindex.dk/job/salg" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, BUSINESS_OFFICE, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan undervisning ######################################################################       
        try {
            String fields[] = {"bibliotek", "forskning", "institutions", "laerer", "paedagog", "voksenuddannelse"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://www.jobindex.dk/job/undervisning" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, RES_EDU, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan kontor ######################################################################
        try {
            String fields[] = {"ejendomsmaegler", "ejendomsservice", "finans", "indkoeb", "jura", "kontor", "kontorkurser", "kontorelev", "logistik", "offentlig", "oversaettelse", "sekretaer", "oekonomi", "oekonomichef"};
            String fields2[] = {"hrkurser", "personale"};

            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://www.jobindex.dk/job/kontor" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, BUSINESS_OFFICE, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields2) {
                        String URL = "http://www.jobindex.dk/job/ledelse" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, BUSINESS_OFFICE, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan social ######################################################################       
        try {
            String fields[] = {"laege", "laegesekretaer", "offentlig", "pleje", "psykologi", "socialraadgivning", "sygeplejerske", "tandlaege", "teknisksundhed", "terapi"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://www.jobindex.dk/job/social" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, MED_SOC, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan oevrige ######################################################################        
        try {
            String fields[] = {"kontorelev", "student", "studiepraktik", "elev", "kurseroevrige"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://www.jobindex.dk/job/oevrige" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, STUDENT, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan ledelse ######################################################################
        try {
            String fields[] = {"freelancekonsulent", "itledelse", "institutions", "leder", "teknikledelse", "projektledelse", "salgschef", "topledelse", "oekonomichef"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://www.jobindex.dk/job/ledelse" + "/" + field + "/" + subd;
                        JobindexCrawler crawler = new JobindexCrawler(di, URL, d.division, LEADERSHIP, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //metrics
        System.out.println(METRICS.toString());
        return METRICS;
    }

    private void setUpAdministrativeDivisions() {
        List<String> copenhagen = Arrays.asList("koebenhavn");
        AdmDivision Copenhagen = new AdmDivision("Copenhagen", copenhagen);

        List<String> sjælland = Arrays.asList("sjaelland");
        AdmDivision Sjaelland = new AdmDivision("Zealand", sjælland);

        List<String> southdenmark = Arrays.asList("fyn", "sydjylland");
        AdmDivision Syddanmark = new AdmDivision("South Denmark", southdenmark);

        List<String> nordjylland = Arrays.asList("nordjylland");
        AdmDivision Nordjylland = new AdmDivision("North Jylland", nordjylland);

        List<String> midtjylland = Arrays.asList("midtjylland");
        AdmDivision Midtjylland = new AdmDivision("Middle Jylland", midtjylland);

        List<String> groenlandAndfaroe = Arrays.asList("groenland");
        AdmDivision GroenlandAndFaroe = new AdmDivision("Greenland & Faroe", groenlandAndfaroe);

        List<String> øresund = Arrays.asList("skane");
        AdmDivision Øresund = new AdmDivision("Scania", øresund);

        allAdmDivisions = new ArrayList<>();
        allAdmDivisions.add(Copenhagen);
        allAdmDivisions.add(Sjaelland);
        allAdmDivisions.add(Nordjylland);
        allAdmDivisions.add(Midtjylland);
        allAdmDivisions.add(Syddanmark);
        allAdmDivisions.add(GroenlandAndFaroe);
        allAdmDivisions.add(Øresund);
    }

    class AdmDivision {

        //variables

        String division;
        List<String> subdivisions;

        //constructor
        public AdmDivision(String division, List<String> subdivisions) {
            this.division = division;
            this.subdivisions = subdivisions;
        }
    }
}
