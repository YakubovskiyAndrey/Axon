package ua.yakubovskiy;

import java.io.IOException;
import java.util.logging.*;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        Handler fileHandler = new FileHandler("%h/axonLog.log");
        fileHandler.setFormatter(new HandlerFormatter());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(fileHandler);
        ProcessingRequest processingRequest =
                new ProcessingRequest("https://api.binance.com/api/v3/depth?limit=5000&symbol=LINKUSDT",
                        "GET", LOGGER);
        processingRequest.start();
    }

    static class HandlerFormatter extends Formatter{
        @Override
        public String format(LogRecord record) {
            return record.getLevel() + ": " + record.getMessage();
        }
    }
}