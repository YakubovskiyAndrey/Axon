package ua.yakubovskiy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessingRequest {

    private final String urlString;

    private final String requestMethod;

    private final Logger logger;

    private final long timeout;

    private final StringBuilder stringBuilder = new StringBuilder();

    private final Map<Double, Double> previousAsks = new HashMap<>();

    private final Map<Double, Double> previousBids = new HashMap<>();

    public ProcessingRequest(String urlString, String requestMethod, Logger logger, long timeout) {
        this.urlString = urlString;
        this.requestMethod = requestMethod;
        this.logger = logger;
        this.timeout = timeout;
    }

    public void start() {
        try {
            while (true) {
                String response = getResponse();
                getAsks(response);
                getBids(response);
                logger.log(Level.INFO, String.valueOf(stringBuilder));
                stringBuilder.setLength(0);
                Thread.sleep(timeout);
            }
        } catch (InterruptedException | JSONException | IOException e) {
            logger.log(Level.WARNING, String.valueOf(e));
            Thread.currentThread().interrupt();
        }
    }

    private void getBids(String response) throws JSONException {
        Map<Double, Double> newPrices = new HashMap<>();
        JSONObject jsonObject = new JSONObject(response);
        JSONArray jsonArray = jsonObject.getJSONArray("bids");
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONArray orderJson = jsonArray.getJSONArray(i);
            double price = Double.parseDouble(orderJson.getString(0));
            double size = Double.parseDouble(orderJson.getString(1));

            if (previousBids.containsKey(price)){
                if(previousBids.get(price) != size){
                    stringBuilder.append("update [bid] (").
                            append(price).append(", ").append(size).append(")").append("\n");
                }
            }else {
                stringBuilder.append("new [bid] (").
                        append(price).append(", ").append(size).append(")").append("\n");
            }
            newPrices.put(price, size);
        }

        if (!previousBids.isEmpty()){
            previousBids.entrySet().stream()
                    .filter(doubleDoubleEntry -> !newPrices.containsKey(doubleDoubleEntry.getKey()))
                    .forEach(doubleDoubleEntry -> stringBuilder.append("delete [bid] (").
                            append(doubleDoubleEntry.getKey()).append(", ").
                            append(doubleDoubleEntry.getValue()).append(")").append("\n"));
        }

        previousBids.clear();
        previousBids.putAll(newPrices);
    }

    private void getAsks(String response) throws JSONException {
        Map<Double, Double> newPrices = new HashMap<>();
        JSONObject jsonObject = new JSONObject(response);
        JSONArray jsonArray = jsonObject.getJSONArray("asks");
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONArray orderJson = jsonArray.getJSONArray(i);
            double price = Double.parseDouble(orderJson.getString(0));
            double size = Double.parseDouble(orderJson.getString(1));

            if (previousAsks.containsKey(price)){
                if(previousAsks.get(price) != size){
                    stringBuilder.append("update [ask] (").
                            append(price).append(", ").append(size).append(")").append("\n");
                }
            }else {
                stringBuilder.append("new [ask] (").
                        append(price).append(", ").append(size).append(")").append("\n");
            }
            newPrices.put(price, size);
        }

        if (!previousAsks.isEmpty()){
            previousAsks.entrySet().stream()
                    .filter(doubleDoubleEntry -> !newPrices.containsKey(doubleDoubleEntry.getKey()))
                    .forEach(doubleDoubleEntry -> stringBuilder.append("delete [ask] (").
                            append(doubleDoubleEntry.getKey()).append(", ").
                            append(doubleDoubleEntry.getValue()).append(")").append("\n"));
        }

        previousAsks.clear();
        previousAsks.putAll(newPrices);
    }

    private String getResponse() throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.connect();
        StringBuilder sb = new StringBuilder();

        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }catch (IOException e){
                logger.log(Level.WARNING, String.valueOf(e));
            }
        }
        return sb.toString();
    }
}
