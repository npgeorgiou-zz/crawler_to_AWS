package controllers;

import model.Metrics;
import dbUtils.DatabaseUtils;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Master {

    static Metrics finalM = new Metrics("TOTAL");
    private static final DatabaseUtils dbUtils = new DatabaseUtils();

    public static void main(String[] args) {

        Date date = new Date();
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                scan();
            }
        }, date, 24 * 60 * 60 * 1000);

    }

    private static void scan() {

        dbUtils.flushDatabase();



        //Jobbank
        Metrics jbm = new JobbankController().start();
        updateMetrics(finalM, jbm.getAllJobs(), jbm.getJobsInEnglish(), jbm.getDuplicateJobs(), jbm.getExceptions());

        //Monster
        Metrics mm = new MonsterController().start();
        updateMetrics(finalM, mm.getAllJobs(), mm.getJobsInEnglish(), mm.getDuplicateJobs(), mm.getExceptions());

        //Jobindex
        Metrics jim = new JobindexController().start();
        updateMetrics(finalM, jim.getAllJobs(), jim.getJobsInEnglish(), jim.getDuplicateJobs(), jim.getExceptions());

        //Jobnet
        Metrics jnm = new JobnetController().start();
        updateMetrics(finalM, jnm.getAllJobs(), jnm.getJobsInEnglish(), jnm.getDuplicateJobs(), jnm.getExceptions());

        //Jobbsafari
        Metrics jsm = new JobbsafariController().start();
        updateMetrics(finalM, jsm.getAllJobs(), jsm.getJobsInEnglish(), jsm.getDuplicateJobs(), jsm.getExceptions());

        //sout and save results
        soutMetrics(jbm);
//        dbUtils.addMetricToDatabase(jbm);

        soutMetrics(mm);
//        dbUtils.addMetricToDatabase(mm);

        soutMetrics(jim);
//        dbUtils.addMetricToDatabase(jim);

        soutMetrics(jnm);
//        dbUtils.addMetricToDatabase(jnm);

        soutMetrics(jsm);
//        dbUtils.addMetricToDatabase(jsm);

        soutMetrics(finalM);
    }

    private static void updateMetrics(Metrics m, int allJobs, int jobsInEnglish, int duplicateJobs, int exceptions) {
        m.setAllJobs(finalM.getAllJobs() + allJobs);
        m.setJobsInEnglish(finalM.getJobsInEnglish() + jobsInEnglish);
        m.setDuplicateJobs(finalM.getDuplicateJobs() + duplicateJobs);
        m.setExceptions(finalM.getExceptions() + exceptions);
    }

    private static void soutMetrics(Metrics m) {
        System.out.println(m.getCrawler() + " METRICS:");
        System.out.println("Jobs in site: " + "\t" + m.getAllJobs());
        System.out.println("Jobs in english: " + "\t" + m.getJobsInEnglish());
        System.out.println("Duplicates: " + "\t" + m.getDuplicateJobs());
        System.out.println("Exceptions: " + "\t" + m.getExceptions());
        System.out.println("");
    }

}
