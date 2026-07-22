package logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class AppLog {

    private final Logger logger;

    public AppLog(String name, String fileName) {
        this.logger = Logger.getLogger(name);
        try {
            FileHandler handler = new FileHandler(fileName, true);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Could not open " + fileName + ": " + e.getMessage());
        }
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warning(message);
    }

    public void error(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
    }
}
