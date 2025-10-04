package org.example.service.impl;

import org.example.service.StorageService;

import java.time.LocalDateTime;
import java.util.Objects;

public class MemoryStorageService<T> implements StorageService<T> {

    private final int ttl;

    private volatile LocalDateTime updated;
    private T data;

    public MemoryStorageService(int ttl) {
        this.ttl = ttl;
    }

    @Override
    public T read() {
        return data;
    }

    @Override
    public void write(T value) {
        updated = LocalDateTime.now();
        this.data = value;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean isExpired() {
        return Objects.isNull(updated) || updated.plusSeconds(ttl).isBefore(LocalDateTime.now());
    }
}
