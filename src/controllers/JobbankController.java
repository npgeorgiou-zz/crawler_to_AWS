package controllers;

import model.Metrics;
import crawlers.JobbankCrawler;
import di.DI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobbankController extends BaseController{

    // declare variables
    Map<String, String> AC;
    Map<String, String> FC;
    List<AdmDivision> allAdmDivisions;

    // constructor
    public JobbankController(String jobsiteName, DI di) {
        super(jobsiteName, di);
        setUpAreaCodes();
        setUpFieldCodes();
        setUpAdministrativeDivisions();
    }

    // methods
    public Metrics start(){
        //scan engineer ######################################################################
        try {
            String[] fields = {"Anlæg, Byggeri & Konstruktion", "Arkitektur, Kunst & Design", "Elektro & Telekommunikation",
                "Fødevarer & Veterinær", "Kemi, Biotek & Materialer", "Klima, Miljø & Energi", "Landbrug & Natur", "Maskin & Design",
                "Matematik, Fysik & Nano"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://jobbank.dk/job/?act=find&key=" + "&udd=" + FC.get(field) + "&amt=" + AC.get(subd) + "&max=500&oprettet=";
                        JobbankCrawler crawler = new JobbankCrawler(di, URL, d.division, ENGINEER, jobsiteName);
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
            String[] fields = {"IT"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://jobbank.dk/job/?act=find&key=" + "&udd=" + FC.get(field) + "&amt=" + AC.get(subd) + "&max=500&oprettet=";
                        JobbankCrawler crawler = new JobbankCrawler(di, URL, d.division, IT, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);
                    }
                }

            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }
        //scan handel ######################################################################
        //#######nothing like this in this site
        //
        //scan industri ######################################################################
        //#######nothing like this in this site
        //
        //scan salg ######################################################################
        try {
            String[] fields = {"Marketing & Business"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://jobbank.dk/job/?act=find&key=" + "&udd=" + FC.get(field) + "&amt=" + AC.get(subd) + "&max=500&oprettet=";
                        JobbankCrawler crawler = new JobbankCrawler(di, URL, d.division, BUSINESS_OFFICE, jobsiteName);
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
            String[] fields = {"Undervisning & Pædagogik"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://jobbank.dk/job/?act=find&key=" + "&udd=" + FC.get(field) + "&amt=" + AC.get(subd) + "&max=500&oprettet=";
                        JobbankCrawler crawler = new JobbankCrawler(di, URL, d.division, RES_EDU, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);
                    }
                }

            }
        } catch (IOException e1) {
            e1.printStackTrace(System.out);
        }

        //scan kontor ######################################################################businessAndOffice
        try {
            String[] fields = {"Jura", "Administration"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://jobbank.dk/job/?act=find&key=" + "&udd=" + FC.get(field) + "&amt=" + AC.get(subd) + "&max=500&oprettet=";
                        JobbankCrawler crawler = new JobbankCrawler(di, URL, d.division, BUSINESS_OFFICE, jobsiteName);
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
            String[] fields = {"Medicinal & Sundhed"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://jobbank.dk/job/?act=find&key=" + "&udd=" + FC.get(field) + "&amt=" + AC.get(subd) + "&max=500&oprettet=";
                        JobbankCrawler crawler = new JobbankCrawler(di, URL, d.division, MED_SOC, jobsiteName);
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
            String[] fields = {"Organisation & Ledelse", "Human Resources"};
            for (AdmDivision d : allAdmDivisions) {
                for (String subd : d.subdivisions) {
                    for (String field : fields) {
                        String URL = "http://jobbank.dk/job/?act=find&key=" + "&udd=" + FC.get(field) + "&amt=" + AC.get(subd) + "&max=500&oprettet=";
                        JobbankCrawler crawler = new JobbankCrawler(di, URL, d.division, LEADERSHIP, jobsiteName);
                        Metrics m = crawler.scan();
                        METRICS.updateMetrics(m);
                    }
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
        AC.put("Storkøbenhavn", "2");
        AC.put("Frederiksborg området", "3");
        AC.put("Roskilde området", "14");
        AC.put("Vestsjælland", "4");
        AC.put("Storstrøm området", "5");
        AC.put("Fyn & Øer", "13");
        AC.put("Sønderjylland", "12");
        AC.put("Sydvestjylland", "11");
        AC.put("Vestjylland", "9");
        AC.put("Sydøstjylland", "10");
        AC.put("Viborg området", "7");
        AC.put("Aarhus området", "8");
        AC.put("Nordjylland", "6");
        AC.put("Bornholm", "20");
        AC.put("Øresundregionen", "21");
        AC.put("Grønland & Færøerne", "22");
    }

    private void setUpFieldCodes() {

        FC = new HashMap<>();
        FC.put("Administration", "20");
        FC.put("Anlæg, Byggeri & Konstruktion", "43");
        FC.put("Arkitektur, Kunst & Design", "29");
        FC.put("Elektro & Telekommunikation", "47");
        FC.put("Fødevarer & Veterinær", "32");
        FC.put("Human Resources", "38");
        FC.put("Humaniora", "28");
        FC.put("IT", "24");
        FC.put("Jura", "23");
        FC.put("Kemi, Biotek & Materialer", "44");
        FC.put("Klima, Miljø & Energi", "45");
        FC.put("Landbrug & Natur", "37");
        FC.put("Marketing & Business", "22");
        FC.put("Maskin & Design", "48");
        FC.put("Matematik, Fysik & Nano", "46");
        FC.put("Medicinal & Sundhed", "31");
        FC.put("Naturvidenskab", "30");
        FC.put("Organisation & Ledelse", "41");
        FC.put("Produktion, Logistik & Transport", "35");
        FC.put("Samfundsvidenskab", "34");
        FC.put("Sprog, Media & Kommunikation", "25");
        FC.put("Teknik & Teknologi", "33");
        FC.put("Undervisning & Pædagogik", "26");
        FC.put("Økonomi & Revision", "21");
    }

    private void setUpAdministrativeDivisions() {
        List<String> copenhagen = Arrays.asList("Storkøbenhavn", "Frederiksborg området");
        AdmDivision Copenhagen = new AdmDivision("Copenhagen", copenhagen);

        List<String> sjælland = Arrays.asList("Roskilde området", "Storstrøm området", "Vestsjælland");
        AdmDivision Sjaelland = new AdmDivision("Zealand", sjælland);

        List<String> southdenmark = Arrays.asList("Fyn & Øer", "Sønderjylland", "Sydvestjylland");
        AdmDivision Syddanmark = new AdmDivision("South Denmark", southdenmark);

        List<String> nordjylland = Arrays.asList("Nordjylland");
        AdmDivision Nordjylland = new AdmDivision("North Jylland", nordjylland);

        List<String> midtjylland = Arrays.asList("Viborg området", "Aarhus området", "Sydøstjylland", "Vestjylland");
        AdmDivision Midtjylland = new AdmDivision("Middle Jylland", midtjylland);

        List<String> groenlandAndfaroe = Arrays.asList("Grønland & Færøerne");
        AdmDivision GroenlandAndFaroe = new AdmDivision("Greenland & Faroe", groenlandAndfaroe);

        List<String> øresund = Arrays.asList("Øresundregionen");
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
