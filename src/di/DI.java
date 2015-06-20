package di;

import com.cybozu.labs.langdetect.LangDetectException;

public class DI {

    private Config c = null;
    private Db db = null;
    private LangDetect languageDetector = null;
    private Filter filter = null;
    private Logger fileWriter = null;

    public DI DI() {
        setConfig();
        setDb();
        setLanguageDetector();
        setFilter();
        return this;
    }

    private Config setConfig() {
        c = new Config("C:\\Users\\ksptsinplanet\\Documents\\NetBeansProjects\\GlobejobCrawler_ToLocalHost\\config.properties");
        return c;
    }

    public Config getConfig() {
        if (c == null) {
            c = setConfig();
        }
        return c;
    }

    private Db setDb() {
        db = new Db();
        return db;
    }

    public Db getDB() {
        if (db == null) {
            db = setDb();
        }
        return db;
    }

    private LangDetect setLanguageDetector() {
        //Initialize the language detector with the profiles
        languageDetector = new LangDetect();
        try {
            String profilesPath = c.getProp("profiles_path");
            languageDetector.init(profilesPath);
        } catch (LangDetectException lde) {
            lde.printStackTrace(System.out);
        }
        return languageDetector;
    }

    public LangDetect getLanguageDetector() {
        if (languageDetector == null) {
            languageDetector = setLanguageDetector();
        }
        return languageDetector;
    }

    private Filter setFilter() {
        filter = new Filter(this);
        return filter;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = setFilter();
        }
        return filter;
    }

    private Logger setLogger() {
        fileWriter = new Logger(this);
        return fileWriter;
    }

    public Logger getLogger() {
        if (fileWriter == null) {
            fileWriter = setLogger();
        }
        return fileWriter;
    }

}
