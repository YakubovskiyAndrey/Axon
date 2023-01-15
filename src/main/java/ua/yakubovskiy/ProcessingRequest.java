package ua.yakubovskiy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

public class ProcessingRequest {

    private final String urlString;

    private final String requestMethod;

    private final StringBuilder stringBuilder = new StringBuilder();

    private final Map<Double, Double> previousAsks = new HashMap<>();

    private final Map<Double, Double> previousBids = new HashMap<>();

    public ProcessingRequest(String urlString, String requestMethod) {
        this.urlString = urlString;
        this.requestMethod = requestMethod;
    }

    public void start() {
        try {
            String response = getResponse();
            getAsks(response);
            System.out.println(stringBuilder);
            stringBuilder.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        int status = connection.getResponseCode();

        StringBuilder sb = new StringBuilder();
        try (InputStream in = (status == HttpURLConnection.HTTP_OK)
                ? connection.getInputStream() : connection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
