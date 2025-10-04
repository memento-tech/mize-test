package org.example.service.impl;

import org.example.handler.CachedObjectHandler;
import org.example.service.StorageService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WebStorageService<T> implements StorageService<T> {

    private static final String REQUEST_METHOD = "GET";

    private final String webStorageUrl;
    private final CachedObjectHandler<T> objectMapper;

    public WebStorageService(String webStorageUrl, CachedObjectHandler<T> objectMapper) {
        this.webStorageUrl = webStorageUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public T read() throws IOException {
        var url = new URL(webStorageUrl);

        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(REQUEST_METHOD);

        connection.connect();

        var responseCode = connection.getResponseCode();

        if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        } else {
            var builder = new StringBuilder();
            var scanner = new Scanner(url.openStream());

            while (scanner.hasNext()) {
                builder.append(scanner.nextLine());
            }

            return objectMapper.jsonToObject(builder.toString());
        }
    }

    @Override
    public void write(T value) {
        throw new UnsupportedOperationException("Write operation is not supported for WebStorageService");
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean isExpired() {
        return false;
    }
}
