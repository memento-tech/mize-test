package org.example.handler.impl;

import org.apache.commons.lang3.StringUtils;
import org.example.data.ExchangeRateList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateHandlerTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @InjectMocks
    ExchangeRateHandler exchangeRateHandler;

    @Test
    void testJsonToObject_emptyValue() {
        assertNull(exchangeRateHandler.jsonToObject(StringUtils.EMPTY));
        assertNull(exchangeRateHandler.jsonToObject(null));
    }

    @Test
    void testJsonToObject_validValue() {
        var timestampFromJson = "1759492800";
        var value = "{\n" +
                    "  \"disclaimer\": \"Usage subject to terms: https://openexchangerates.org/terms\",\n" +
                    "  \"license\": \"https://openexchangerates.org/license\",\n" +
                    "  \"timestamp\": " + timestampFromJson + ",\n" +
                    "  \"base\": \"USD\",\n" +
                    "  \"rates\": {\n" +
                    "    \"AED\": 3.6725,\n" +
                    "    \"AFN\": 67.141725\n" +
                    "  }\n" +
                    "}";

        var jsonTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(timestampFromJson)),
                                TimeZone.getDefault().toZoneId());

        var result = exchangeRateHandler.jsonToObject(value);
        assertNotNull(result);
        assertNotEquals(jsonTime, result.getCreatedAt());

        var resultRates = result.getRates();
        assertNotNull(resultRates);
        assertFalse(resultRates.isEmpty());
        assertEquals(2, resultRates.size());
        assertTrue(resultRates.containsKey("AED"));
        assertTrue(resultRates.containsKey("AFN"));
    }

    @Test
    void testObjectToJson_nullObject() {
        assertEquals(StringUtils.EMPTY, exchangeRateHandler.objectToJSON(null));
    }

    @Test
    void testObjectToJson_validObject() {
        var now = LocalDateTime.now();
        var rates = new HashMap<String, Double>();
        rates.put("AED", 3.6725);
        rates.put("AFN", 67.141725);

        var obj = new ExchangeRateList();
        obj.setCreatedAt(now);
        obj.setRates(rates);

        var result = exchangeRateHandler.objectToJSON(obj);

        assertTrue(StringUtils.isNoneBlank(result));
        assertTrue(containsAll(result, now.format(FORMATTER), "AED", "3.6725", "AFN", "67.141725"));

    }

    @Test
    void testGetCreationDate_nullObject() {
        assertTrue(exchangeRateHandler.getCreationDate(null).isEmpty());
    }

    @Test
    void testGetCreationDate_validObject() {
        var now = LocalDateTime.now();
        var obj = new ExchangeRateList();
        obj.setCreatedAt(now);

        var optionalResult = exchangeRateHandler.getCreationDate(obj);
        assertTrue(optionalResult.isPresent());

        assertEquals(now, optionalResult.get());
    }

    private boolean containsAll(String string, String ... values) {
        for (String value: values) {
            if (!string.contains(value)) {
                return false;
            }
        }

        return true;
    }
}