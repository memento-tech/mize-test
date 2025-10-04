package org.example.json;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.example.data.ExchangeRateList;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRateListAdapter implements JsonDeserializer<ExchangeRateList> {

    private static final String RATES_JSON_PARAM_NAME = "rates";

    @Override
    public ExchangeRateList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        var obj = json.getAsJsonObject();
        var ratesJson = obj.getAsJsonObject(RATES_JSON_PARAM_NAME);

        var rates = new HashMap<String, Double>();
        for (Map.Entry<String, JsonElement> entry : ratesJson.entrySet()) {
            rates.put(entry.getKey(), entry.getValue().getAsDouble());
        }

        var result = new ExchangeRateList();
        result.setCreatedAt(LocalDateTime.now());
        result.setRates(rates);
        return result;
    }
}
