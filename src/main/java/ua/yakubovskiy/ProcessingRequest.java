package ua.yakubovskiy;

import ua.yakubovskiy.parser.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProcessingRequest {

    private final String urlString;

    private final String requestMethod;

    private final Logger logger;

    private final int timeout;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private final JsonParser jsonParser = new JsonParser();

    private final Map<Double, Double> previousAsks = new HashMap<>();

    private final Map<Double, Double> previousBids = new HashMap<>();

    public ProcessingRequest(String urlString, String requestMethod, Logger logger, int timeout) {
        this.urlString = urlString;
        this.requestMethod = requestMethod;
        this.logger = logger;
        this.timeout = timeout;
    }

    public void start() {
        executorService.scheduleAtFixedRate(() -> {
            String response = getResponse();
            getAsks(response);
            getBids(response);
        }, 0, timeout, TimeUnit.SECONDS);
    }

    private HttpURLConnection getConnection(){
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.connect();
        } catch (Exception e) {
            logger.warning(String.valueOf(e));
            executorService.shutdown();
        }
        return connection;
    }

    private String getResponse() {
        HttpURLConnection connection = getConnection();
        StringBuilder sb = new StringBuilder();

        if(connection != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                logger.warning(String.valueOf(e));
                executorService.shutdown();
            }
            connection.disconnect();
        }
        return sb.toString();
    }

    private void getBids(String response) {
        String orderType = "bids";
        Map<Double, Double> newOrders = jsonParser.getOrders(response, orderType);

        double totalPreviousSize = previousBids.values().stream()
                .mapToDouble(v -> v).sum();
        double totalNewOrderSize = newOrders.values().stream()
                .mapToDouble(v -> v).sum();

        checkOrdersResizing(totalPreviousSize, totalNewOrderSize, orderType);

        newOrders.forEach((key, value) -> {
            double price = key;
            double size = value;
            if (previousBids.containsKey(price)) {
                if (previousBids.get(price) != size) {
                    logger.info("update [bid] (%f, %f)%n".formatted(price, size));
                }
            } else {
                logger.info("new [bid] (%f, %f)%n".formatted(price, size));
            }
        });

        if(!previousBids.isEmpty()) {
            previousBids.entrySet().stream()
                    .filter(doubleDoubleEntry -> !newOrders.containsKey(doubleDoubleEntry.getKey()))
                    .forEach(doubleDoubleEntry ->
                            logger.info("delete [bid] (%f, %f)%n".
                                    formatted(doubleDoubleEntry.getKey(), doubleDoubleEntry.getValue())));

            previousBids.clear();
        }
        previousBids.putAll(newOrders);
    }

    private void getAsks(String response) {
        String orderType = "asks";
        Map<Double, Double> newOrders = jsonParser.getOrders(response, orderType);

        double totalPreviousSize = previousAsks.values().stream()
                .mapToDouble(v -> v).sum();
        double totalNewOrderSize = newOrders.values().stream()
                .mapToDouble(v -> v).sum();

        checkOrdersResizing(totalPreviousSize, totalNewOrderSize, orderType);

        newOrders.forEach((key, value) -> {
            double price = key;
            double size = value;
            if (previousAsks.containsKey(price)) {
                if (previousAsks.get(price) != size) {
                    logger.info("update [ask] (%f, %f)%n".formatted(price, size));
                }
            } else {
                logger.info("new [ask] (%f, %f)%n".formatted(price, size));
            }
        });

        if(!previousAsks.isEmpty()) {
            previousAsks.entrySet().stream()
                    .filter(doubleDoubleEntry -> !newOrders.containsKey(doubleDoubleEntry.getKey()))
                    .forEach(doubleDoubleEntry ->
                            logger.info("delete [ask] (%f, %f)%n".
                                    formatted(doubleDoubleEntry.getKey(), doubleDoubleEntry.getValue())));

            previousAsks.clear();
        }
        previousAsks.putAll(newOrders);
    }

    private void checkOrdersResizing(double totalPreviousSize, double totalNewOrderSize, String orderType){
        if(totalPreviousSize > totalNewOrderSize){
            logger.info("%s decreased by %f%n".formatted(orderType, totalPreviousSize - totalNewOrderSize));
        } else if (totalPreviousSize < totalNewOrderSize) {
            logger.info("%s increased by %f%n".formatted(orderType, totalNewOrderSize - totalPreviousSize));
        }else {
            logger.info("%s have not changed".formatted(orderType));
        }
    }
}
