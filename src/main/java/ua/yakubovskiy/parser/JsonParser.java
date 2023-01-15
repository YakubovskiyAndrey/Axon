package ua.yakubovskiy.parser;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class JsonParser {

    public Map<Double, Double> getOrders(String response, String orderType){
        Map<Double, Double> newOrders = new HashMap<>();
        JSONObject jsonObject = new JSONObject(response);
        JSONArray jsonArray = jsonObject.getJSONArray(orderType);
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONArray orderJson = jsonArray.getJSONArray(i);
            newOrders.put(Double.parseDouble(orderJson.getString(0)),
                    Double.parseDouble(orderJson.getString(1)));
        }
        return newOrders;
    }
}
