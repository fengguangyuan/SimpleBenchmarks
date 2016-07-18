import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;

public class MyLogger {
    private Logger logger = Logger.getLogger(MyLogger.class.toString());
    private FileHandler fh = null;
    private String filename = null;

    public MyLogger(String filename) {
        this.filename = filename;
        init(filename);
    }

    private void init() {
        try {
            // set log file
            fh = new FileHandler("../logs/simple-logger.logs", true);
            logger.addHandler(fh);
            // set log formatter
            LogFormatter lf = new LogFormatter();
            fh.setFormatter(lf);

            logger.setLevel(Level.ALL);
            logger.info("A simple logger has started!");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void init(String filename) {
        try {
            // set log file
            fh = new FileHandler(filename, true);
            logger.addHandler(fh);
            // set log formatter
            LogFormatter lf = new LogFormatter();
            fh.setFormatter(lf);

            logger.setLevel(Level.ALL);
            logger.info("A simple logger has started!");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void info(String str) {
        logger.info(str);
    }

    public void log(Level level, String str) {
        logger.log(level, str);
    }

    public void main(String[] args) {
        // MyLogger.defaultLogger.log(Level.INFO, "This is first words!");
    }
}

class LogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        return "[" + record.getLevel() + "] : " + record.getMessage() +"\n";
    }
}
