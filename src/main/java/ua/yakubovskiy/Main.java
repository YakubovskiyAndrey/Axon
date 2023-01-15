package ua.yakubovskiy;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        Handler fileHandler = new FileHandler("%h/axonLog.log");
        fileHandler.setFormatter(new HandlerFormatter());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(fileHandler);

        ProcessingRequest processingRequest =
                new ProcessingRequest("https://api.binance.com/api/v3/depth?limit=5000&symbol=LINKUSDT",
                        "GET", LOGGER, 10);
        processingRequest.start();
    }

    static class HandlerFormatter extends Formatter{
        @Override
        public String format(LogRecord record) {
            return record.getLevel() + ": " + record.getMessage();
        }
    }
}