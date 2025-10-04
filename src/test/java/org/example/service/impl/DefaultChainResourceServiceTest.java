package org.example.service.impl;

import org.example.service.StorageService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class DefaultChainResourceServiceTest {

    @Test
    void testRead_noStorages_returnsNull() throws IOException {
        var service = new DefaultChainResourceService<String>(List.of());

        assertNull(service.read());
    }

    @Test
    void testRead_oneNonExpiredStorage_returnsValue() throws IOException {
        StorageService<String> storage = mock(StorageService.class);
        when(storage.isReadOnly()).thenReturn(false);
        when(storage.isExpired()).thenReturn(false);
        when(storage.read()).thenReturn("value");

        var service = new DefaultChainResourceService<>(List.of(storage));

        assertEquals("value", service.read());
        verify(storage).read();
    }

    @Test
    void testRead_expiredStorage_refreshesFromGoodStorage() throws IOException {
        StorageService<String> expired = mock(StorageService.class);
        StorageService<String> good = mock(StorageService.class);

        when(expired.isReadOnly()).thenReturn(false);
        when(expired.isExpired()).thenReturn(true);

        when(good.isReadOnly()).thenReturn(false);
        when(good.isExpired()).thenReturn(false);
        when(good.read()).thenReturn("fresh");

        var service = new DefaultChainResourceService<>(List.of(expired, good));

        String result = service.read();

        assertEquals("fresh", result);

        verify(good, atLeastOnce()).read();
        verify(expired).write("fresh");
    }

    @Test
    void testRead_readOnlyStorage_skipped() throws IOException {
        StorageService<String> readOnly = mock(StorageService.class);
        when(readOnly.isReadOnly()).thenReturn(true);

        var service = new DefaultChainResourceService<>(List.of(readOnly));

        assertNull(service.read());

        verify(readOnly).read();
        verify(readOnly, never()).write(any());
    }

    @Test
    void testRead_multipleStorages_orderMatters() throws IOException {
        StorageService<String> expired = mock(StorageService.class);
        StorageService<String> good = mock(StorageService.class);

        when(expired.isReadOnly()).thenReturn(false);
        when(expired.isExpired()).thenReturn(true);

        when(good.isReadOnly()).thenReturn(false);
        when(good.isExpired()).thenReturn(false);
        when(good.read()).thenReturn("fresh");

        var service = new DefaultChainResourceService<>(List.of(expired, good));

        String result = service.read();

        assertEquals("fresh", result);

        InOrder inOrder = inOrder(good, expired);
        inOrder.verify(good, atLeastOnce()).read();
        inOrder.verify(expired).write("fresh");
    }
}