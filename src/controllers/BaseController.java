package controllers;

import com.cybozu.labs.langdetect.LangDetectException;
import config.Config;
import crawlerUtils.LangDetect;
import model.Metrics;

public class BaseController {

    // declare variables
    protected String jobsiteName;
    protected final Metrics METRICS;
    protected static LangDetect languageDetector;

    // Get categories names from Config file
    Config c = new Config();
    protected final String ENGINEER = c.getProp("engineer_dbname");
    protected final String IT = c.getProp("it_dbname");
    protected final String BUSINESS_OFFICE = c.getProp("business_dbname");
    protected final String MARKETING = c.getProp("marketing_dbname");
    protected final String LEADERSHIP = c.getProp("leadership_dbname");
    protected final String SERVICE = c.getProp("service_dbname");
    protected final String INDUSTRY = c.getProp("industry_dbname");
    protected final String RES_EDU = c.getProp("res_edu_dbname");
    protected final String MED_SOC = c.getProp("med_soc_dbname");
    protected final String STUDENT = c.getProp("student_dbname");

    // constructor
    public BaseController(String jobsiteName) {
        METRICS = new Metrics(jobsiteName);
        this.jobsiteName = jobsiteName;
        setUpLangDetector();
    }

    //methods
    //Initialize the language detector with the profiles
    protected final void setUpLangDetector() {
        languageDetector = new LangDetect();
        try {
            String profilesPath = new Config().getProp("profiles_path");
            languageDetector.init(profilesPath);
        } catch (LangDetectException lde) {
            lde.printStackTrace(System.out);
        }
    }

}
