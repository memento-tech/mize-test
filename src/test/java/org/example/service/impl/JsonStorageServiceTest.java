package org.example.service.impl;

import org.example.data.ExchangeRateList;
import org.example.handler.CachedObjectHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    CachedObjectHandler<ExchangeRateList> objectHandler;

    int ttl = 14400;

    JsonStorageService<ExchangeRateList> jsonStorageService;

    @BeforeEach
    void before() throws IOException {
        var file = tempDir.resolve("rates.json");
        Files.writeString(file, "{\"dummy\":123}");

        jsonStorageService = new JsonStorageService<>(objectHandler, "rates.json", ttl) {
            @Override
            protected Path ensureOutputFolder() {
                return tempDir;
            }
        };
    }

    @Test
    void testRead_nonExistingFile() throws IOException {
        var service = new JsonStorageService<>(objectHandler, "non_existing.json", ttl) {
            @Override
            protected Path ensureOutputFolder() {
                return tempDir;
            }
        };

        assertNull(service.read());
    }

    @Test
    void testRead_validFile() throws IOException {
        var expected = new ExchangeRateList();
        when(objectHandler.jsonToObject(any())).thenReturn(expected);

        ExchangeRateList actual = jsonStorageService.read();

        assertEquals(expected, actual);
    }

    @Test
    void testIsReadOnly() {
        assertFalse(jsonStorageService.isReadOnly());
    }

    @Test
    void testIsExpired_nullUpdated() {
        var service = new JsonStorageService<>(objectHandler, "non_existing.json", ttl) {
            @Override
            protected Path ensureOutputFolder() {
                return tempDir;
            }
        };

        assertTrue(service.isExpired());
    }

    @Test
    void testIsExpired_expiredUpdateValue() {
        var expected = LocalDateTime.now().minusHours(5);
        var obj = new ExchangeRateList();
        obj.setCreatedAt(expected);
        when(objectHandler.jsonToObject(any())).thenReturn(obj);
        when(objectHandler.getCreationDate(obj)).thenReturn(Optional.of(expected));

        assertTrue(jsonStorageService.isExpired());
    }

    @Test
    void testIsExpired_notExpiredUpdateValue() {
        var expected = LocalDateTime.now().minusHours(1);
        var obj = new ExchangeRateList();
        obj.setCreatedAt(expected);
        when(objectHandler.jsonToObject(any())).thenReturn(obj);
        when(objectHandler.getCreationDate(obj)).thenReturn(Optional.of(expected));

        assertFalse(jsonStorageService.isExpired());
    }

    @Test
    void testWrite_nullObject() throws IOException {
        jsonStorageService.write(null);

        verify(objectHandler, times(0)).objectToJSON(any());
    }

    @Test
    void testWrite_validObject() throws IOException {
        when(objectHandler.objectToJSON(any())).thenReturn("test");

        var obj = new ExchangeRateList();
        obj.setCreatedAt(LocalDateTime.now());

        jsonStorageService.write(obj);

        verify(objectHandler, times(1)).objectToJSON(any());
    }
}
