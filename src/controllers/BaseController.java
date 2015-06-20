package controllers;

import di.Config;
import di.DI;
import model.Metrics;

public class BaseController {

    // Declare variables
    protected DI di;
    protected String jobsiteName;
    protected Config c;
    protected final Metrics METRICS; 

    protected final String ENGINEER;
    protected final String IT;
    protected final String BUSINESS_OFFICE;
    protected final String MARKETING;
    protected final String LEADERSHIP;
    protected final String SERVICE;
    protected final String INDUSTRY;
    protected final String RES_EDU;
    protected final String MED_SOC;
    protected final String STUDENT;

    // Constructor
    public BaseController(String jobsiteName, DI di) {
        this.di = di;
        this.jobsiteName = jobsiteName;
        c = di.getConfig();
        METRICS = new Metrics(jobsiteName);
        
        // Get categories names from Config file
        ENGINEER = c.getProp("engineer_dbname");
        IT = c.getProp("it_dbname");
        BUSINESS_OFFICE = c.getProp("business_dbname");
        MARKETING = c.getProp("marketing_dbname");
        LEADERSHIP = c.getProp("leadership_dbname");
        SERVICE = c.getProp("service_dbname");
        INDUSTRY = c.getProp("industry_dbname");
        RES_EDU = c.getProp("res_edu_dbname");
        MED_SOC = c.getProp("med_soc_dbname");
        STUDENT = c.getProp("student_dbname");      
    }

    // Methods
}
