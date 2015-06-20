package bin;

import di.Config;
import controllers.JobbankController;
import controllers.JobbsafariController;
import controllers.JobindexController;
import controllers.JobnetController;
import controllers.MonsterController;
import di.Logger;
import model.Metrics;
import di.DI;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Master {

    private static final Metrics finalM = new Metrics("TOTAL");
    private static Config c;

    public static void main(String[] args) {

        // Instantiate DI
        DI di = new DI();
        
        // Run crawler every 24 hours
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                scan(di);
            }
        }, new Date(), 24 * 60 * 60 * 1000);

    }

    private static void scan(DI di) {

        // Empty DB
        di.getDB().flushDatabase();

        c = di.getConfig();
        
        // Jobbank       
        if (c.getProp("jobbank").equals("true")) {
            Metrics jbm = new JobbankController("Jobbank", di).start();
            System.out.println(jbm.toString());
            finalM.updateMetrics(jbm);
        }

        // Monster
        if (c.getProp("jobbank").equals("true")) {
            Metrics mm = new MonsterController("Monster", di).start();
            System.out.println(mm.toString());
            finalM.updateMetrics(mm);
        }

        // Jobindex
        if (c.getProp("jobbank").equals("true")) {
            Metrics jim = new JobindexController("Jobindex", di).start();
            System.out.println(jim.toString());
            finalM.updateMetrics(jim);
        }

        // Jobnet        
        if (c.getProp("jobbank").equals("true")) {
            Metrics jnm = new JobnetController("Jobnet", di).start();
            System.out.println(jnm.toString());
            finalM.updateMetrics(jnm);
        }

        // Jobbsafari        
        if (c.getProp("jobbank").equals("true")) {
            Metrics jsm = new JobbsafariController("Jobsafari", di).start();
            System.out.println(jsm.toString());
            finalM.updateMetrics(jsm);
        }
        
        System.out.println(finalM.toString());

        // Save results
        // dbUtils.addMetricToDatabase(finalM)
        // dbUtils.registerDbChange();
    }

}
