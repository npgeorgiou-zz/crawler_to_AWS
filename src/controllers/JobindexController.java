package controllers;

import model.Metrics;
import crawlers.JobindexCrawler;
import com.cybozu.labs.langdetect.LangDetectException;
import java.io.IOException;
import java.util.Map;
import crawlerUtils.LangDetect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sharedUtilities.JobCategories;

class JobindexController {

    //declare variables
    private LangDetect languageDetector;
    private static final String profilesPath = "C:\\Users\\ksptsinplanet\\git\\crawler\\Crawler\\profiles";

    private final Metrics METRICS;

    Map<String, String> AC;
    List<AdmDivision> allAdmDivisions;
    String[] areas;

    //constructor
    public JobindexController() {
        METRICS = new Metrics("Jobindex");

        setUpLangDetector();

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

    //methods
    public Metrics start() {
        //scan engineer ######################################################################
        try {
            String fields[] = {"byggeteknik", "elektronik", "kemi", "teknikledelse", "maskiningenioer", "medicinal", "produktionsteknik"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://tech.jobindex.dk/job/ingenioer" + "/" + field + "/" + subd;
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.ENGINEER, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

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
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.IT, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

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
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.SERVICE, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

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
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.SERVICE, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

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
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.BUSINESS, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

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
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.RES_EDU, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

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
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.BUSINESS, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

                    }
                }
            }
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields2) {
                        String URL = "http://www.jobindex.dk/job/ledelse" + "/" + field + "/" + subd;
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.BUSINESS, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

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
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.MED_SOC, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

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
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.STUDENT, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

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
                        JobindexCrawler crawlerInstance = new JobindexCrawler(URL, d.division, JobCategories.LEADERSHIP, "Jobindex", languageDetector);
                        Metrics m = crawlerInstance.scan();
                        updateMetrics(METRICS, m.getAllJobs(), m.getJobsInEnglish(), m.getDuplicateJobs(), m.getExceptions());

                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

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

    //Initialize the language detector with the profiles
    private void setUpLangDetector() {
        languageDetector = new LangDetect();
        try {
            languageDetector.init(profilesPath);
        } catch (LangDetectException lde) {
            lde.printStackTrace(System.out);
        }
    }

    private void updateMetrics(Metrics m, int allJobs, int jobsInEnglish, int duplicateJobs, int exceptions) {
        m.setAllJobs(METRICS.getAllJobs() + allJobs);
        m.setJobsInEnglish(METRICS.getJobsInEnglish() + jobsInEnglish);
        m.setDuplicateJobs(METRICS.getDuplicateJobs() + duplicateJobs);
        m.setExceptions(METRICS.getExceptions() + exceptions);
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
