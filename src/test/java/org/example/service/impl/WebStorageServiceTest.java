package org.example.service.impl;

import org.example.data.ExchangeRateList;
import org.example.handler.CachedObjectHandler;
import org.example.handler.impl.ExchangeRateHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WebStorageServiceTest {

    private static final String OPEN_EXCHANGE_RATES_URL = "https://openexchangerates.org/" +
                                                          "api/latest.json" +
                                                          "?app_id=70993b5c8e56434b9a1beae168ec46fb";

    CachedObjectHandler<ExchangeRateList> cachedObjectHandler = new ExchangeRateHandler();

    @InjectMocks
    WebStorageService<ExchangeRateList> webStorageService = new WebStorageService<>(OPEN_EXCHANGE_RATES_URL, cachedObjectHandler);

    @Test
    void testRead() throws IOException {
        var result = webStorageService.read();

        assertNotNull(result);
    }

    @Test
    void testIsExpired() {
        assertFalse(webStorageService.isExpired());
    }

    @Test
    void testIsReadOnly() {
        assertTrue(webStorageService.isReadOnly());
    }

    @Test
    void testWrite() {
        assertThrows(UnsupportedOperationException.class, () -> webStorageService.write(new ExchangeRateList()));
    }

}