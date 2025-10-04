package org.example.service.impl;

import org.example.data.ExchangeRateList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MemoryStorageServiceTest {

    @InjectMocks
    MemoryStorageService<ExchangeRateList> memoryStorageService = new MemoryStorageService<>(3600);

    @Test
    void testRead_nullData() {
        assertNull(memoryStorageService.read());
    }

    @Test
    void testRead_existingData() {
        var expected = new ExchangeRateList();
        expected.setCreatedAt(LocalDateTime.now());
        memoryStorageService.write(expected);

        var result = memoryStorageService.read();

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void testIsReadOnly() {
        assertFalse(memoryStorageService.isReadOnly());
    }

    @Test
    void testIsExpired_nonExistingValue() {
        assertTrue(memoryStorageService.isExpired());
    }

    @Test
    void testIsExpired_validValue_nonExpired() {
        var expected = new ExchangeRateList();
        expected.setCreatedAt(LocalDateTime.now());
        memoryStorageService.write(expected);

        assertFalse(memoryStorageService.isExpired());
    }

    @Test
    void testIsExpired_validValue_expired() {
        memoryStorageService = new MemoryStorageService<>(1) {
            private volatile LocalDateTime updated;

            @Override
            public void write(ExchangeRateList value) {
                updated = LocalDateTime.now().minusHours(1).minusMinutes(15);
                super.write(value);
            }

            @Override
            public boolean isExpired() {
                return Objects.isNull(updated) || updated.plusHours(1).isBefore(LocalDateTime.now());
            }
        };


        assertTrue(memoryStorageService.isExpired());
    }
}