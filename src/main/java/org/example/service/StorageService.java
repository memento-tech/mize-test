package org.example.service;

import java.io.IOException;

public interface StorageService<T> {

    T read() throws IOException;

    void write(T value) throws IOException;

    boolean isReadOnly();

    boolean isExpired();

}
