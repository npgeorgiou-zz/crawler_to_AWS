package bin;

import controllers.JobbankController;
import controllers.JobbsafariController;
import controllers.JobindexController;
import controllers.JobnetController;
import controllers.MonsterController;
import model.Metrics;
import dbUtils.DatabaseUtils;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Master {

    static Metrics finalM = new Metrics("TOTAL");
    private static final DatabaseUtils dbUtils = new DatabaseUtils();

    public static void main(String[] args) {
        
        // Run crawler every 24 hours
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                scan();
            }
        }, new Date(), 24 * 60 * 60 * 1000);

    }

    private static void scan() {

        // Empty DB
        dbUtils.flushDatabase();
         
               Metrics jnm = new JobnetController().start();
        finalM.updateMetrics(jnm);
        
        //Jobbank
        Metrics jbm = new JobbankController().start();
        finalM.updateMetrics(jbm);

        //Monster
        Metrics mm = new MonsterController().start();
        finalM.updateMetrics(mm);

        //Jobindex
        Metrics jim = new JobindexController().start();
        finalM.updateMetrics(jim);

        //Jobnet
 

        //Jobbsafari
        Metrics jsm = new JobbsafariController().start();
        finalM.updateMetrics(jsm);

        // Sout results
        System.out.println(jbm.toString());
        System.out.println(mm.toString());
        System.out.println(jim.toString());
        System.out.println(jnm.toString());
        System.out.println(jsm.toString()); 
        System.out.println(finalM.toString()); 

        // Save results
        // dbUtils.addMetricToDatabase(finalM)
        // dbUtils.registerDbChange();
    }

}
