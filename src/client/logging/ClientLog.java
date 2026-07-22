package client.logging;

import logging.AppLog;

public final class ClientLog {

    private static final AppLog LOG = new AppLog("client", "client.log");

    public static void info(String message) {
        LOG.info(message);
    }

    public static void warn(String message) {
        LOG.warn(message);
    }

    public static void error(String message, Throwable t) {
        LOG.error(message, t);
    }

    private ClientLog() {
    }
}
