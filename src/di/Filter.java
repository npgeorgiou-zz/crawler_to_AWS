package di;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Field;
import model.Job;

public class Filter {

    // Declare variables
    DI di;
    Config config;
    Logger logger;
    private final String ENGINEER = "NATURAL_SCIENCES";
    private final String IT = "IT";
    private final String BUSINESS_OFFICE = "BUSINESS_OFFICE";
    private final String MARKETING = "MARKETING";
    private final String LEADERSHIP = "LEADERSHIP";
    private final String SERVICE = "SERVICE";
    private final String INDUSTRY = "INDUSTRY";
    private final String RES_EDU = "RES_EDU";
    private final String MED_SOC = "MED_SOC";
    private final String STUDENT = "STUDENT";

    // Constructor
    public Filter(DI di) {
        this.di = di;
        config = di.getConfig();
        logger = di.getLogger();
    }

    // Methods
    public boolean checkIfRequiresDanish(String s) {
        String nordicKeywords = config.getProp("nordic_language_keywords");
        StringTokenizer regex = new StringTokenizer(nordicKeywords, ",");
        String data = "";
        while (regex.hasMoreElements()) {
            data = data + "(\\b" + regex.nextToken().replaceAll("^\\s+", "") + "\\b)|";
        }

        String PatternAsTokens = data.substring(0, data.length() - 1);

        Pattern myPattern = Pattern.compile(PatternAsTokens, Pattern.CASE_INSENSITIVE);
        Matcher matcher = myPattern.matcher(s);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public String homogeniseCompanyName(String cn) {

        String originalCn = cn;

        //remove weird gap char
        cn = cn.replaceAll("\\u0096", "")
                .replaceAll("\\u00a0", "");

        //.dk, .com
        cn = cn.replaceAll("\\.dk", "");
        cn = cn.replaceAll("\\.com", "");

        //remove dots
        cn = cn.replaceAll("\\.", "");
        cn = cn.replaceAll("\\.\\.\\.", "");

        //remove some dirts tha are either more than one word on have -
        cn = cn.replaceAll("-koncernen", "");
        cn = cn.replaceAll("i Kobenhavn", "");
        cn = cn.replaceAll(" København", "");

        //filter everything in parenthesis
        cn = cn.replaceAll("\\([^\\(^\\)]*\\)", "");

        //remove everything that tends to come after some companies names e.g. MAN "DIESEL"
        String[] companyNameWords = cn.split(" ");
        String[] companyNameSolidsSmall = config.getProp("company_name_solids_small").split(", ");
        int i = 0;
        for (String s : companyNameSolidsSmall) {
            for (String w : companyNameWords) {
                if (w.equalsIgnoreCase(s)) {
                    cn = companyNameSolidsSmall[i];
                }
            }
            i++;
        }

        //replace some company names mainly Unis that can vary e.g. Københavns Universitet, "Department of blablabla"
        int j = 0;
        String[] companyNameSolidsBig = config.getProp("company_name_solids_big").split(", ");
        for (String s : companyNameSolidsBig) {
            if (cn.toLowerCase().contains(s.toLowerCase())) {//to lowercase for safety in case it has different caps
                System.out.println("I found this:" + cn);
                cn = companyNameSolidsBig[j];
                System.out.println("I made this:" + cn);
            }
            j++;
        }

        //replace some Uni names
        if (cn.equalsIgnoreCase("KU")) {
            cn = "University of Copenhagen";
        }

        if (cn.equalsIgnoreCase("DTU")) {
            cn = "Technical University of Denmark";
        }

        if (cn.equalsIgnoreCase("SDU")) {
            cn = "University of Southern Denmark";
        }

        if (cn.equalsIgnoreCase("AAU")) {
            cn = "University of Aalborg";
        }

        if (cn.equalsIgnoreCase("AU")) {
            cn = "University of Aarhus";
        }

        if (cn.equalsIgnoreCase("ITU")) {
            cn = "IT University of Copenhagen";
        }

        if (cn.equalsIgnoreCase("RUC")) {
            cn = "University of Roskilde";
        }

        //remove generic unnecessary bits like A/S, aps etch
        StringTokenizer regex = new StringTokenizer(config.getProp("company_name_dirts"), ",");
        String data = "";

        while (regex.hasMoreElements()) {
            data = data + "(\\b" + regex.nextToken().replaceAll("^\\s", "") + "\\b)|";
        }

        String PatternAsTokens = data.substring(0, data.length() - 1);
        Pattern myPattern = Pattern.compile(PatternAsTokens, Pattern.CASE_INSENSITIVE);

        for (String w : companyNameWords) {
            System.out.println(w);
            Matcher matcher2 = myPattern.matcher(w);
            if (matcher2.find()) {
                System.out.println("I found this:" + cn);
                System.out.println(matcher2.group());
                cn = cn.replace(w, "");
                System.out.println("I made this:" + cn);
            }
        }

        //filter whitespaces that are more than one
        cn = cn.replaceAll(" +", " ");

        //remove spaces from beginning and end of string
        cn = cn.trim();

        //now from the clean name, replace some companies that tend to have 2 or more different names, with a standard
        cn = replaceStrings(cn);

        //make all letters small and then capitalize first letter of each word //TODO except of :in at etch
        if (cn.length() != 0) {
            cn = cn.toLowerCase();
            String[] cnWords = cn.split(" ");
            cn = "";
            for (String w : cnWords) {
                w = w.substring(0, 1).toUpperCase() + w.substring(1);
                cn = cn.concat(" ").concat(w);
            }
            cn = cn.substring(1);

            String[] cnWords2 = cn.split("/");
            cn = "";
            for (String w : cnWords2) {
                w = w.substring(0, 1).toUpperCase() + w.substring(1);
                cn = cn.concat("/").concat(w);
            }
            cn = cn.substring(1);

            String[] cnWords3 = cn.split("-");
            cn = "";
            for (String w : cnWords3) {
                w = w.substring(0, 1).toUpperCase() + w.substring(1);
                cn = cn.concat("-").concat(w);
            }
            cn = cn.substring(1);
        }

        // Log change
        if (!originalCn.equals(cn)) {
            String log = originalCn + " --> " + cn;
            logger.write("company_names", log);
        }
        return cn;
    }

    private String replaceStrings(String cn) {
        if (cn.contains("Maersk") || cn.equalsIgnoreCase("Mærsk")) {
            cn = "Maersk";
        }

        if (cn.equalsIgnoreCase("KK-ELECTRONIC")) {
            cn = "KK Wind Solutions";
        }

        if (cn.equalsIgnoreCase("DHI")) {
            cn = "DHI Group";
        }
        if (cn.equalsIgnoreCase("Alk-Abelló")) {
            cn = "ALK";
        }
        if (cn.equalsIgnoreCase("OBJECT")
                || cn.equalsIgnoreCase("Vero Moda")) {
            cn = "Bestseller";
        }
        if (cn.equalsIgnoreCase("HP")) {
            cn = "Hewlett-Packard";
        }
        if (cn.equalsIgnoreCase("DNV GL")) {
            cn = "DET NORSKE VERITAS";
        }
        if (cn.equalsIgnoreCase("AP Møller - Mærsk (Maersk Drilling)")) {
            cn = "Maersk Drilling";
        }
        if (cn.equalsIgnoreCase("DMSC, FILIAL AF EUROPEAN SPALLATION SOURCE ESS")) {
            cn = "European Spallation Source ESS";
        }
        if (cn.equalsIgnoreCase("BørneUngeKlinikken")
                || cn.equalsIgnoreCase("Institut For Sygdomsforebyggelse")
                || cn.equalsIgnoreCase("Ortopædkirurgisk Afdeling")) {
            cn = "Region Hovedstaden";
        }

        if (cn.equalsIgnoreCase("Io-Interactive")) {
            cn = "IO Interactive";
        }
        if (cn.equalsIgnoreCase("JACK & JONES")) {
            cn = "Bestseller";
        }
        if (cn.equalsIgnoreCase("Nilfisk Advance")) {
            cn = "Nilfisk-Advance";
        }
        if (cn.equalsIgnoreCase("William Demant")
                || (cn.equalsIgnoreCase("Oticon"))) {
            cn = "Oticon/William Demant";
        }
        if (cn.equalsIgnoreCase("TipTopJobcom")) {
            cn = "TipTopJob";
        }

        if (cn.equalsIgnoreCase("Københavns Universitet")
                || cn.equalsIgnoreCase("Institut for Lægemiddeldesign og Farmakologi")
                || cn.equalsIgnoreCase("Institut for Farmaci")
                || cn.equalsIgnoreCase("NNF Center for Basic Metabolic Research")
                || cn.equalsIgnoreCase("Institut for Plante- og Miljøvidenskab")
                || cn.equalsIgnoreCase("BRIC - Biotech Research and Innovation Center")
                || cn.equalsIgnoreCase("DanStem")
                || cn.equalsIgnoreCase("Institut for Statskundskab,s Universitet")
                || cn.equalsIgnoreCase("Institut for Statskundskab")
                || cn.equalsIgnoreCase("Institut for Cellulær og Molekylær Medicin")
                || cn.equalsIgnoreCase("Kemisk Institut")
                || cn.equalsIgnoreCase("Institut for Medier, Erkendelse og Formidling")
                || cn.equalsIgnoreCase("Center for Protein Research")
                || cn.equalsIgnoreCase("Institut for Naturfagenes Didaktik")
                || cn.equalsIgnoreCase("Institut for Klinisk Medicin")
                || cn.equalsIgnoreCase("Niels Bohr Institutet")
                || cn.equalsIgnoreCase("SAXO-Instituttet")
                || cn.equalsIgnoreCase("Det Sundhedsvidenskabelige Fakultet (SUND)")
                || cn.equalsIgnoreCase("Institut For Folkesundhedsvidenskab")
                || cn.equalsIgnoreCase("Institut For Intl Sundhed, Immunologi Og Mikrobio")
                || cn.equalsIgnoreCase("Institut For Veterinær Sygdomsbiologi (sund)")
                || cn.equalsIgnoreCase("Institut For Neurovidenskab Og Farmakologi")
                || cn.equalsIgnoreCase("Biologisk Institut")) {
            cn = "University of Copenhagen";
        }

        if (cn.equalsIgnoreCase("Danmarks Tekniske Universitet")
                || cn.equalsIgnoreCase("Energikonvertering- og lagring")) {
            cn = "Technical University of Denmark";
        }

        if (cn.equalsIgnoreCase("Syddansk Universitet")) {
            cn = "University of Southern Denmark";
        }

        if (cn.equalsIgnoreCase("Aalborg Universitet")) {
            cn = "University of Aalborg";
        }

        if (cn.equalsIgnoreCase("Aarhus Universitet")) {
            cn = "University of Aarhus";
        }

        if (cn.equalsIgnoreCase("IT-Universitetet")) {
            cn = "IT University of Copenhagen";
        }
        if (cn.equalsIgnoreCase("Roskilde Universitet")) {
            cn = "University of Roskilde";
        }
        return cn;
    }

    public String homogeniseJobTitle(String jt) {

        String originalJt = jt;

        //remove weird gap char
        jt = jt.replaceAll("\\u0096", "")
                .replaceAll("\\u00a0", "");

        //remove commas and dots from end
        jt = jt.replaceAll("[,.:]+$", "");

        //replace ! with .
        jt = jt.replaceAll("!", "");

        //remove numbers that are more than 2 and are in beginning of String
        if (jt.length() != 0) {
            if (Character.isDigit(jt.charAt(0)) && Character.isDigit(jt.charAt(1)) && Character.isDigit(jt.charAt(2))) {
                jt = jt.replaceFirst("^[0-9]+(?!$)", "");
                jt = jt.trim();

                //remove commas, dots, -s from beginning
                jt = jt.replaceFirst("^[,.:-]+(?!$)", "");
            }
        }

        //remove certain unnecessary things that create duplis from different jobsites
        jt = jt.replaceAll(", Novo Nordisk", "")
                .replaceAll("–", "-")
                .replaceAll("Post Doc", "PostDoc")
                .replaceAll("Graduate position: ", "")
                .replaceAll(" - Ballerup - GN ReSound, Denmark", "")
                .replaceAll(" - Ballerup - GN Netcom, Denmark", "")
                .replaceAll(", Frederikshavn", "")
                .replaceAll("- Copenhagen, Denmark", "")
                .replaceAll(" - Bredebro", "")
                .replaceAll(" - Biogen Idec", "")
                .replaceAll(", Denmark", "")
                .replaceAll(", - Codan", "");;

        //replace certain strings
        jt = jt.replaceAll("Århus", "Aarhus");

        //filter everything in parenthesis
        jt = jt.replaceAll("\\([^\\(^\\)]*\\)", "");

        //filter everything after bracket that doesnt close
        boolean unclosedBracket = false;
        int indexOfUnclosedBracket = 0;
        for (int i = 0; i < jt.length(); i++) {

            if (Character.toString(jt.charAt(i)).equals("(")) {
                unclosedBracket = true;
                indexOfUnclosedBracket = i;
            }
            if (Character.toString(jt.charAt(i)).equals(")")) {
                unclosedBracket = false;
                indexOfUnclosedBracket = 0;
            }
        }
        if (unclosedBracket == true) {
            jt = jt.substring(0, indexOfUnclosedBracket);
        }

        //filter whitespaces that are more than one
        jt = jt.replaceAll(" +", " ");

        //filter before and after whitespaces
        jt = jt.trim();

        //capitalise first letter
        if (jt.length() != 0) {
            jt = jt.substring(0, 1).toUpperCase() + jt.substring(1);
        }

        // Log change
        if (!originalJt.equals(jt)) {
            String log = originalJt + " --> " + jt;
            logger.write("job_titles", log);
        }

        return jt;
    }

    public String homogeniseURL(String url) {

        String originalUrl = url;

        // http://www.milestonesys.com/Company/Milestone-Systems/jobsandcareers/Job-Details/?jobId=443#jobbank.dk
        if (url.contains("www.milestonesys.com")) {
            url = url.replaceAll("#jobbank.dk", "");
        }
        // http://www.danskebank.com/da-dk/karriere/soeg-job/Pages/JobShow.aspx?JobPostingId=7066&Dis=        
        if (url.contains("www.danskebank.com")) {
            url = url.replaceAll("&Dis=", "");
        }
        // https://jobsearch.maersk.com/vacancies/publication?pinst=005056A52F591EE4A59878C61A774DD1&CallBackUrl=http://www.maersk.com/system/sapcallbackurl&userid=
        // https://jobsearch.maersk.com/vacancies/publication?PINST=005056A52F591EE4A59878C61A774DD1&APPLY=X
        if (url.contains("jobsearch.maersk.com")) {
            try {
                String divider = "pinst=";
                url = url.split(divider)[0].concat(divider).concat(url.split(divider)[1].split("&")[0]);
            } catch (Exception e) {
                //aioob ex because of url that didnt have the right format. Try second format
                String divider = "PINST=";
                url = url.split(divider)[0].concat(divider).concat(url.split(divider)[1].split("&")[0]);
            }

        }

        // Log change
        if (!originalUrl.equals(url)) {
            String log = originalUrl + " --> " + url;
            logger.write("urls", log);
        }

        return url;
    }

    public Job filterTitleForPossibleChangeInFields(Job JobBeingFiletered, String jobCategory, String jobTitle) {

        boolean fieldChanged = false;

        switch (jobCategory) {
            case RES_EDU:
                if (titleContainsFieldKeywords(config.getProp("student_keywords"), jobTitle)) {// If title contains student words
                    String log = jobCategory.toUpperCase() + " --> " + STUDENT + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
            case STUDENT:
                if (titleContainsFieldKeywords(config.getProp("resEdu_keywords"), jobTitle)) {// If title contains researchAndEdu words
                    String log = jobCategory.toUpperCase() + " --> " + RES_EDU + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
            case LEADERSHIP:
                if (titleContainsFieldKeywords(config.getProp("resEdu_keywords"), jobTitle)) {// If title contains researchAndEdu words
                    String log = jobCategory.toUpperCase() + " --> " + RES_EDU + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (titleContainsFieldKeywords(config.getProp("student_keywords"), jobTitle)) {// If title contains student words
                    String log = jobCategory.toUpperCase() + " --> " + STUDENT + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (titleContainsFieldKeywords(config.getProp("engineer_keywords"), jobTitle)) {// If title contains engineer words
                    String log = jobCategory.toUpperCase() + " ++ "
                            + ENGINEER + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, ENGINEER, "ADD");
                    fieldChanged = true;
                }

                if (titleContainsFieldKeywords(config.getProp("it_keywords"), jobTitle)) {// If title contains IT words
                    String log = jobCategory.toUpperCase() + " ++ " + IT + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, IT, "ADD");
                    fieldChanged = true;
                }
                if (titleContainsFieldKeywords(config.getProp("industry_keywords"), jobTitle)) {// If title contains industry words
                    String log = jobCategory.toUpperCase() + " ++ "
                            + INDUSTRY + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, INDUSTRY, "ADD");
                    fieldChanged = true;
                }
                if (titleContainsFieldKeywords(config.getProp("med_soc_keywords"), jobTitle)) {// If title contains M&S words
                    String log = jobCategory.toUpperCase() + " ++ " + MED_SOC + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, MARKETING, "ADD");
                    fieldChanged = true;
                }
                if (titleContainsFieldKeywords(config.getProp("business_keywords"), jobTitle)) {// If title contains business words
                    String log = jobCategory.toUpperCase() + " ++ " + BUSINESS_OFFICE + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, BUSINESS_OFFICE, "ADD");
                    fieldChanged = true;
                }
                if (titleContainsFieldKeywords(config.getProp("service_keywords"), jobTitle)) {//if ititlet contains service words
                    String log = jobCategory.toUpperCase() + " ++ " + SERVICE + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, SERVICE, "ADD");
                    fieldChanged = true;
                }
            case IT:
                if (titleContainsFieldKeywords(config.getProp("resEdu_keywords"), jobTitle)) {// If title contains researchAndEdu words
                    String log = jobCategory.toUpperCase() + " --> " + RES_EDU + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (titleContainsFieldKeywords(config.getProp("student_keywords"), jobTitle)) {// If title contains student words
                    String log = jobCategory.toUpperCase() + " --> " + STUDENT + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (titleContainsFieldKeywords(config.getProp("engineer_keywords"), jobTitle)) {// If title contains engineer words
                    String log = jobCategory.toUpperCase() + " --> " + ENGINEER + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, ENGINEER, "MOVE");
                    fieldChanged = true;
                }

                if (titleContainsFieldKeywords(config.getProp("industry_keywords"), jobTitle)) {// If title contains industry words
                    String log = jobCategory.toUpperCase() + " --> " + INDUSTRY + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, INDUSTRY, "MOVE");
                    fieldChanged = true;
                }

                if (titleContainsFieldKeywords(config.getProp("med_soc_keywords"), jobTitle)) {// If title contains MED&SOC words
                    String log = jobCategory.toUpperCase() + " --> " + MED_SOC + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, MED_SOC, "MOVE");
                    fieldChanged = true;
                }
                if (titleContainsFieldKeywords(config.getProp("business_keywords"), jobTitle)) {// If title contains business words
                    String log = jobCategory.toUpperCase() + " --> " + BUSINESS_OFFICE + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, BUSINESS_OFFICE, "MOVE");
                    fieldChanged = true;
                }
//                if (filterCategory(Filters.LEADERSHIP, jobTitle)) {// If title contains leadership words
//                    String log = jobCategory.toUpperCase() + " ++ "+ LEADERSHIP + "\t" + " # " + "\t" + jobTitle; fileWriter.getInstance(di).write("fields", log);  
//                    changeJobField(JobBeingFiletered, jobCategory, Filters.LEADERSHIP, "ADD");
//                    fieldChanged = true;
//                }
                if (titleContainsFieldKeywords(config.getProp("service_keywords"), jobTitle)) {// If title contains service words
                    String log = jobCategory.toUpperCase() + " --> " + SERVICE + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, SERVICE, "MOVE");
                    fieldChanged = true;
                }
                //but if it also has it words, re add it category (so keep it in original category too)
                if (titleContainsFieldKeywords(config.getProp("it_keywords"), jobTitle)) {// If title contains IT words
                    String log = jobCategory.toUpperCase() + " [] " + "\t" + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, IT, "ADD");
                    fieldChanged = false;
                }

            case BUSINESS_OFFICE:
                if (titleContainsFieldKeywords(config.getProp("resEdu_keywords"), jobTitle)) {// If title contains researchAndEdu words
                    String log = jobCategory.toUpperCase() + " --> " + RES_EDU + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (titleContainsFieldKeywords(config.getProp("student_keywords"), jobTitle)) {// If title contains student words
                    String log = jobCategory.toUpperCase() + " --> " + STUDENT + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (titleContainsFieldKeywords(config.getProp("engineer_keywords"), jobTitle)) {// If title contains engineer words
                    String log = jobCategory.toUpperCase() + " ++ " + ENGINEER + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, ENGINEER, "ADD");
                    fieldChanged = true;
                }
                if (titleContainsFieldKeywords(config.getProp("it_keywords"), jobTitle)) {// If title contains IT words
                    String log = jobCategory.toUpperCase() + " ++ " + IT + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, IT, "ADD");
                    fieldChanged = true;
                }
                if (titleContainsFieldKeywords(config.getProp("industry_keywords"), jobTitle)) {// If title contains industry words
                    String log = jobCategory.toUpperCase() + " --> " + INDUSTRY + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, INDUSTRY, "MOVE");
                    fieldChanged = true;
                }
                if (titleContainsFieldKeywords(config.getProp("med_soc_keywords"), jobTitle)) {// If title contains M&S words
                    String log = jobCategory.toUpperCase() + " ++ " + MED_SOC + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, MED_SOC, "ADD");
                    fieldChanged = true;
                }
//                if (filterCategory(Filters.LEADERSHIP, jobTitle)) {// If title contains leadership words
//                    String log = jobCategory.toUpperCase() + " ++  "+ LEADERSHIP + "\t" + " # " + "\t" + jobTitle; fileWriter.getInstance(di).write("fields", log);  
//                    changeJobField(JobBeingFiletered, jobCategory, Filters.LEADERSHIP, "ADD");
//                    fieldChanged = true;
//                }
                if (titleContainsFieldKeywords(config.getProp("service_keywords"), jobTitle)) {//if ititlet contains service words
                    String log = jobCategory.toUpperCase() + " --> " + SERVICE + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, SERVICE, "MOVE");
                    fieldChanged = true;
                }
            default:
                if (titleContainsFieldKeywords(config.getProp("resEdu_keywords"), jobTitle)) {// If title contains researchAndEdu words
                    String log = jobCategory.toUpperCase() + " --> " + RES_EDU + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (titleContainsFieldKeywords(config.getProp("student_keywords"), jobTitle)) {// If title contains student words
                    String log = jobCategory.toUpperCase() + " --> " + STUDENT + "\t" + " # " + "\t" + jobTitle;
                    logger.write("fields", log);
                    changeJobField(JobBeingFiletered, jobCategory, STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (titleContainsFieldKeywords(config.getProp("engineer_keywords"), jobTitle)) {// If title contains engineer words
                    if (jobCategory.equals(ENGINEER)) {
                    } else {
                        String log = jobCategory.toUpperCase() + " --> " + ENGINEER + "\t" + " # " + "\t" + jobTitle;
                        logger.write("fields", log);
                        changeJobField(JobBeingFiletered, jobCategory, ENGINEER, "MOVE");
                        fieldChanged = true;
                    }
                }
                if (titleContainsFieldKeywords(config.getProp("it_keywords"), jobTitle)) {// If title contains IT words
                    if (jobCategory.equals(IT)) {
                    } else {
                        String log = jobCategory.toUpperCase() + " --> " + IT + "\t" + " # " + "\t" + jobTitle;
                        logger.write("fields", log);
                        changeJobField(JobBeingFiletered, jobCategory, IT, "MOVE");
                        fieldChanged = true;
                    }
                }
                if (titleContainsFieldKeywords(config.getProp("industry_keywords"), jobTitle)) {// If title contains industry words
                    if (jobCategory.equals(INDUSTRY)) {
                    } else {
                        String log = jobCategory.toUpperCase() + " --> " + INDUSTRY + "\t" + " # " + "\t" + jobTitle;
                        logger.write("fields", log);
                        changeJobField(JobBeingFiletered, jobCategory, INDUSTRY, "MOVE");
                        fieldChanged = true;
                    }
                }
                if (titleContainsFieldKeywords(config.getProp("med_soc_keywords"), jobTitle)) {// If title contains M&S words
                    if (jobCategory.equals(MED_SOC)) {
                    } else {
                        String log = jobCategory.toUpperCase() + " --> " + MED_SOC + "\t" + " # " + "\t" + jobTitle;
                        logger.write("fields", log);
                        changeJobField(JobBeingFiletered, jobCategory, MED_SOC, "MOVE");
                        fieldChanged = true;
                    }
                }
                if (titleContainsFieldKeywords(config.getProp("business_keywords"), jobTitle)) {// If title contains business words
                    if (jobCategory.equals(BUSINESS_OFFICE)) {
                    } else {
                        String log = jobCategory.toUpperCase() + " --> " + BUSINESS_OFFICE + "\t" + " # " + "\t" + jobTitle;
                        logger.write("fields", log);
                        changeJobField(JobBeingFiletered, jobCategory, BUSINESS_OFFICE, "MOVE");
                        fieldChanged = true;
                    }
                }
//                if (filterCategory(Filters.LEADERSHIP, jobTitle)) {// If title contains leadership words
//                    if (jobCategory.equals(Filters.LEADERSHIP)) {
//                    } else {
//                        String log = jobCategory.toUpperCase() + " ++  "+ LEADERSHIP + "\t" + " # " + "\t" + jobTitle; fileWriter.getInstance(di).write("fields", log);  
//                        changeJobField(JobBeingFiletered, jobCategory, Filters.LEADERSHIP, "ADD");
//                        fieldChanged = true;
//                    }
//                }
                if (titleContainsFieldKeywords(config.getProp("service_keywords"), jobTitle)) {// If title contains service words
                    if (jobCategory.equals(SERVICE)) {
                    } else {
                        String log = jobCategory.toUpperCase() + " --> " + SERVICE + "\t" + " # " + "\t" + jobTitle;
                        logger.write("fields", log);
                        changeJobField(JobBeingFiletered, jobCategory, SERVICE, "MOVE");
                        fieldChanged = true;
                    }
                }
        }
        if (!fieldChanged) {
            String log = jobCategory.toUpperCase() + " [] " + "\t" + "\t" + "\t" + " # " + "\t" + jobTitle;
            logger.write("fields", log);
        }
        return JobBeingFiletered;
    }

    public boolean titleContainsFieldKeywords(String words, String t) {
        StringTokenizer regex = new StringTokenizer(words, ",");
        String data = "";
        while (regex.hasMoreElements()) {
            data = data + "(\\b" + regex.nextToken().replaceAll("^\\s+", "") + "\\b)|";
        }

        String PatternAsTokens = data.substring(0, data.length() - 1);

        Pattern myPattern = Pattern.compile(PatternAsTokens, Pattern.CASE_INSENSITIVE);
        Matcher matcher = myPattern.matcher(t);
        if (matcher.find()) {
            //System.out.println(">>> " + matcher.group());
            return true;
        }
        return false;
    }

    private void changeJobField(Job JobBeingFiletered, String currentField, String newField, String operation) {
        Field f = new Field(newField);

        switch (operation) {
            case "ADD":
                JobBeingFiletered.addField(f);
                break;
            case "MOVE":
                JobBeingFiletered.removeField(currentField);
                JobBeingFiletered.addField(f);
                break;
        }
    }
}
