package crawlerUtils;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Field;
import model.Job;
import sharedUtilities.Filters;
import sharedUtilities.JobCategories;

public class Filter {
    //variables

    //constructor
    public Filter() {
    }

    //methods
    public boolean checkIfRequiresDanish(String s) {
        StringTokenizer regex = new StringTokenizer(Filters.NORDICS, ",");
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
        //remove weird gap char
        cn = cn.replaceAll("\\u0096", "")
                .replaceAll("\\u00a0", "");
        //remove dots
        cn = cn.replaceAll("\\.", "");
        cn = cn.replaceAll("\\.\\.\\.", "");

        //remove some dirts tha are either more than one word on have -
        cn = cn.replaceAll("-koncernen", "");
        cn = cn.replaceAll("i Kobenhavn", "");
        cn = cn.replaceAll(" København", "");

        //remove everything that tends to come after some companies names e.g. MAN "DIESEL"
        String[] companyNameWords = cn.split(" ");
        String[] companyNameSolidsSmall = Filters.COMPANYNAMESOLIDS_SMALL.split(", ");
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
        String[] companyNameSolidsBig = Filters.COMPANYNAMESOLIDS_BIG.split(", ");
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
        StringTokenizer regex = new StringTokenizer(Filters.COMPANYNAMEDIRTS, ",");
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

        return cn;
    }

    private String replaceStrings(String cn) {
        if (cn.equalsIgnoreCase("KK-ELECTRONIC")) {
            cn = "KK Wind Solutions";
        }
        if (cn.equalsIgnoreCase("DHI")) {
            cn = "DHI Group";
        }
        if (cn.equalsIgnoreCase("OBJECT")) {
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
                || cn.equalsIgnoreCase("Ortopædkirurgisk Afdeling")
                || cn.equalsIgnoreCase("Direktionen (afdDI01)")) {
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
        jt = jt.replaceAll("/", " ");
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

        return jt;
    }

    public String homogeniseURL(String url) {
        //    http://www.milestonesys.com/Company/Milestone-Systems/jobsandcareers/Job-Details/?jobId=443#jobbank.dk
        if (url.contains("www.milestonesys.com")) {
            url = url.replaceAll("#jobbank.dk", "");
        }
        //    http://www.danskebank.com/da-dk/karriere/soeg-job/Pages/JobShow.aspx?JobPostingId=7066&Dis=        
        if (url.contains("www.danskebank.com")) {
            url = url.replaceAll("&Dis=", "");
        }

        return url;
    }

    public Job filterTitle(Job JobBeingFiletered, String jobCategory, String jobTitle) {
        boolean fieldChanged = false;

        switch (jobCategory) {
            case JobCategories.RES_EDU:
                if (filterCategory(Filters.STUDENT, jobTitle)) {//if title contains student words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.STUDENT + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
                break;
            case JobCategories.STUDENT:
                if (filterCategory(Filters.RES_EDU, jobTitle)) {//if title contains researchAndEdu words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.RES_EDU + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
                break;
            case JobCategories.LEADERSHIP:

                if (filterCategory(Filters.RES_EDU, jobTitle)) {//if title contains researchAndEdu words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.RES_EDU + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (filterCategory(Filters.STUDENT, jobTitle)) {//if title contains student words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.STUDENT + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (filterCategory(Filters.ENGINEER, jobTitle)) {//if title contains engineer words
                    System.out.println(jobCategory.toUpperCase() + " ++++ "
                            + JobCategories.ENGINEER + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.ENGINEER, "ADD");
                    fieldChanged = true;
                }

                if (filterCategory(Filters.IT, jobTitle)) {//if title contains IT words
                    System.out.println(jobCategory.toUpperCase() + " ++++ "
                            + JobCategories.IT + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.IT, "ADD");
                    fieldChanged = true;
                }
                if (filterCategory(Filters.INDUSTRY, jobTitle)) {//if title contains industry words
                    System.out.println(jobCategory.toUpperCase() + " ++++ "
                            + JobCategories.INDUSTRY + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.INDUSTRY, "ADD");
                    fieldChanged = true;
                }
//                if (filterCategory(Filters.MED_SOC, jobTitle)) {//if title contains M&S words
//                    System.out.println(jobCategory.toUpperCase() + " ++++ "
//                            + JobCategories.MED_SOC + " # " + "\t" + jobTitle + "\r\n");
//                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.MARKETING, "ADD");
//                    fieldChanged = true;
//                }
                if (filterCategory(Filters.BUSINESS, jobTitle)) {//if title contains business words
                    System.out.println(jobCategory.toUpperCase() + " ++++ "
                            + JobCategories.BUSINESS + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.BUSINESS, "ADD");
                    fieldChanged = true;
                }
                if (filterCategory(Filters.SERVICE, jobTitle)) {//if ititlet contains service words
                    System.out.println(jobCategory.toUpperCase() + " ++++ "
                            + JobCategories.SERVICE + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.SERVICE, "ADD");
                    fieldChanged = true;
                }

                break;

            case JobCategories.IT:

                if (filterCategory(Filters.RES_EDU, jobTitle)) {//if title contains researchAndEdu words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.RES_EDU + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (filterCategory(Filters.STUDENT, jobTitle)) {//if title contains student words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.STUDENT + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (filterCategory(Filters.ENGINEER, jobTitle)) {//if title contains engineer words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.ENGINEER + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.ENGINEER, "MOVE");
                    fieldChanged = true;
                }

                if (filterCategory(Filters.INDUSTRY, jobTitle)) {//if title contains industry words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.INDUSTRY + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.INDUSTRY, "MOVE");
                    fieldChanged = true;
                }

                if (filterCategory(Filters.MED_SOC, jobTitle)) {//if title contains MED&SOC words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.MED_SOC + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.MED_SOC, "MOVE");
                    fieldChanged = true;
                }
                if (filterCategory(Filters.BUSINESS, jobTitle)) {//if title contains business words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.BUSINESS + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.BUSINESS, "MOVE");
                    fieldChanged = true;
                }
//                if (filterCategory(Filters.LEADERSHIP, jobTitle)) {//if title contains leadership words
//                    System.out.println(jobCategory.toUpperCase() + " ++++ "
//                            + JobCategories.LEADERSHIP + " # " + "\t" + jobTitle + "\r\n");
//                    changeJobField(JobBeingFiletered, jobCategory, Filters.LEADERSHIP, "ADD");
//                    fieldChanged = true;
//                }
                if (filterCategory(Filters.SERVICE, jobTitle)) {//if title contains service words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.SERVICE + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.SERVICE, "MOVE");
                    fieldChanged = true;
                }
                //but if it also has it words, re add it category (so keep it in original category too)
                if (filterCategory(Filters.IT, jobTitle)) {//if title contains IT words
                    System.out.println(jobCategory.toUpperCase() + " [][] "
                            + "\t" + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.IT, "ADD");
                    fieldChanged = false;
                }

                break;

            case JobCategories.BUSINESS:

                if (filterCategory(Filters.RES_EDU, jobTitle)) {//if title contains researchAndEdu words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.RES_EDU + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (filterCategory(Filters.STUDENT, jobTitle)) {//if title contains student words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.STUDENT + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (filterCategory(Filters.ENGINEER, jobTitle)) {//if title contains engineer words
                    System.out.println(jobCategory.toUpperCase() + " ++++ "
                            + JobCategories.ENGINEER + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.ENGINEER, "ADD");
                    fieldChanged = true;
                }
                if (filterCategory(Filters.IT, jobTitle)) {//if title contains IT words
                    System.out.println(jobCategory.toUpperCase() + " ++++ "
                            + JobCategories.IT + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.IT, "ADD");
                    fieldChanged = true;
                }
                if (filterCategory(Filters.INDUSTRY, jobTitle)) {//if title contains industry words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.INDUSTRY + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.INDUSTRY, "MOVE");
                    fieldChanged = true;
                }
                if (filterCategory(Filters.MED_SOC, jobTitle)) {//if title contains M&S words
                    System.out.println(jobCategory.toUpperCase() + " ++++ "
                            + JobCategories.MED_SOC + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.MED_SOC, "ADD");
                    fieldChanged = true;
                }
//                if (filterCategory(Filters.LEADERSHIP, jobTitle)) {//if title contains leadership words
//                    System.out.println(jobCategory.toUpperCase() + " ++++  "
//                            + JobCategories.LEADERSHIP + " # " + "\t" + jobTitle + "\r\n");
//                    changeJobField(JobBeingFiletered, jobCategory, Filters.LEADERSHIP, "ADD");
//                    fieldChanged = true;
//                }
                if (filterCategory(Filters.SERVICE, jobTitle)) {//if ititlet contains service words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.SERVICE + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.SERVICE, "MOVE");
                    fieldChanged = true;
                }
                break;

            default:
                if (filterCategory(Filters.RES_EDU, jobTitle)) {//if title contains researchAndEdu words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.RES_EDU + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.RES_EDU, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (filterCategory(Filters.STUDENT, jobTitle)) {//if title contains student words
                    System.out.println(jobCategory.toUpperCase() + " ---> "
                            + JobCategories.STUDENT + " # " + "\t" + jobTitle + "\r\n");
                    changeJobField(JobBeingFiletered, jobCategory, JobCategories.STUDENT, "MOVE");
                    fieldChanged = true;
                    break;
                }
                if (filterCategory(Filters.ENGINEER, jobTitle)) {//if title contains engineer words
                    if (jobCategory.equals(JobCategories.ENGINEER)) {
                    } else {
                        System.out.println(jobCategory.toUpperCase() + " ---> "
                                + JobCategories.ENGINEER + " # " + "\t" + jobTitle + "\r\n");
                        changeJobField(JobBeingFiletered, jobCategory, JobCategories.ENGINEER, "MOVE");
                        fieldChanged = true;
                    }
                }
                if (filterCategory(Filters.IT, jobTitle)) {//if title contains IT words
                    if (jobCategory.equals(JobCategories.IT)) {
                    } else {
                        System.out.println(jobCategory.toUpperCase() + " ---> "
                                + JobCategories.IT + " # " + "\t" + jobTitle + "\r\n");
                        changeJobField(JobBeingFiletered, jobCategory, JobCategories.IT, "MOVE");
                        fieldChanged = true;
                    }
                }
                if (filterCategory(Filters.INDUSTRY, jobTitle)) {//if title contains industry words
                    if (jobCategory.equals(JobCategories.INDUSTRY)) {
                    } else {
                        System.out.println(jobCategory.toUpperCase() + " ---> "
                                + JobCategories.INDUSTRY + " # " + "\t" + jobTitle + "\r\n");
                        changeJobField(JobBeingFiletered, jobCategory, JobCategories.INDUSTRY, "MOVE");
                        fieldChanged = true;
                    }
                }
                if (filterCategory(Filters.MED_SOC, jobTitle)) {//if title contains M&S words
                    if (jobCategory.equals(JobCategories.MED_SOC)) {
                    } else {
                        System.out.println(jobCategory.toUpperCase() + " ---> "
                                + JobCategories.MED_SOC + " # " + "\t" + jobTitle + "\r\n");
                        changeJobField(JobBeingFiletered, jobCategory, JobCategories.MED_SOC, "MOVE");
                        fieldChanged = true;
                    }
                }
                if (filterCategory(Filters.BUSINESS, jobTitle)) {//if title contains business words
                    if (jobCategory.equals(JobCategories.BUSINESS)) {
                    } else {
                        System.out.println(jobCategory.toUpperCase() + " ---> "
                                + JobCategories.BUSINESS + " # " + "\t" + jobTitle + "\r\n");
                        changeJobField(JobBeingFiletered, jobCategory, JobCategories.BUSINESS, "MOVE");
                        fieldChanged = true;
                    }
                }
//                if (filterCategory(Filters.LEADERSHIP, jobTitle)) {//if title contains leadership words
//                    if (jobCategory.equals(Filters.LEADERSHIP)) {
//                    } else {
//                        System.out.println(jobCategory.toUpperCase() + " ++++  "
//                                + JobCategories.LEADERSHIP + " # " + "\t" + jobTitle + "\r\n");
//                        changeJobField(JobBeingFiletered, jobCategory, Filters.LEADERSHIP, "ADD");
//                        fieldChanged = true;
//                    }
//                }
                if (filterCategory(Filters.SERVICE, jobTitle)) {//if title contains service words
                    if (jobCategory.equals(JobCategories.SERVICE)) {
                    } else {
                        System.out.println(jobCategory.toUpperCase() + " ---> "
                                + JobCategories.SERVICE + " # " + "\t" + jobTitle + "\r\n");
                        changeJobField(JobBeingFiletered, jobCategory, JobCategories.SERVICE, "MOVE");
                        fieldChanged = true;
                    }
                }
        }
        if (!fieldChanged) {
            System.out.println(jobCategory.toUpperCase() + " [][] "
                    + "\t" + "\t" + " # " + "\t" + jobTitle + "\r\n");
        }
        return JobBeingFiletered;
    }

    public boolean filterCategory(String words, String t) {
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
