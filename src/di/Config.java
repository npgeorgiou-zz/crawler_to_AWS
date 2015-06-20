package di;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    // Declare variables
    public static Properties prop;
    public String path;

    String value = "";

    // Contrsuctor
    public Config(String path){
        this.path = path;
        Config.prop = new Properties();
    }
    // Methods
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
