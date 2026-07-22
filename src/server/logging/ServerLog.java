package server.logging;

import logging.AppLog;

public final class ServerLog {

    private static final AppLog LOG = new AppLog("server", "server.log");

    public static void info(String message) {
        LOG.info(message);
    }

    public static void warn(String message) {
        LOG.warn(message);
    }

    public static void error(String message, Throwable t) {
        LOG.error(message, t);
    }

    private ServerLog() {
    }
}
