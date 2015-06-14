package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    public static Properties prop = new Properties();
    public String path = "C:\\Users\\ksptsinplanet\\Documents\\NetBeansProjects\\GlobejobCrawler_ToMySQL\\crawler.properties";

    String value = "";

    public String getProp(String title) {
        try {
            prop.load(new FileInputStream(path));
            value = prop.getProperty(title);
        } catch (IOException e) {
        }

        return value;
    }

    public void saveProp(String title, String value) {
        try {
            prop.setProperty(title, value);
            prop.store(new FileOutputStream(path), null);
        } catch (IOException e) {
        }
    }
}
