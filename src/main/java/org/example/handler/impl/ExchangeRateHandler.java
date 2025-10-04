package org.example.handler.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.example.data.ExchangeRateList;
import org.example.handler.CachedObjectHandler;
import org.example.json.ExchangeRateListAdapter;
import org.example.json.LocalDateTimeAdapter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class ExchangeRateHandler implements CachedObjectHandler<ExchangeRateList> {

    private final Gson gson;

    public ExchangeRateHandler() {
        this.gson = new GsonBuilder().registerTypeAdapter(ExchangeRateList.class, new ExchangeRateListAdapter())
                                     .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                                     .create();
    }

    @Override
    public ExchangeRateList jsonToObject(String jsonValue) {
        if (StringUtils.isBlank(jsonValue)) {
            return null;
        }

        return gson.fromJson(jsonValue, ExchangeRateList.class);
    }

    @Override
    public String objectToJSON(ExchangeRateList object) {
        if (Objects.isNull(object)) {
            return StringUtils.EMPTY;
        }

        return gson.toJson(object);
    }

    @Override
    public Optional<LocalDateTime> getCreationDate(ExchangeRateList object) {
        return Optional.ofNullable(object)
                .map(ExchangeRateList::getCreatedAt);
    }
}
