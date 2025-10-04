package org.example.service;

import java.io.IOException;

public interface ChainResourceService<T> {

    T read() throws IOException;
}
