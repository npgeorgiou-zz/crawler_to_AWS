package di;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

    // Declare variables
    Config config;
    private static Logger instance = null;
    private static PrintWriter printWriter;
    FileWriter fw;

    protected Logger(DI di) {
        config = di.getConfig();
    }

    public Logger getInstance(DI di) {
        if (instance == null) {
            instance = new Logger(di);
        }
        return instance;
    }

    public void write(String fileAlias, String str) {
        String fileName = config.getProp(fileAlias);

        System.out.println(fileName);
        try {
            fw = new FileWriter(fileName, true);
            fw.write(str + "\n");
            fw.close();
        } catch (IOException ex) {
        }

    }

}
