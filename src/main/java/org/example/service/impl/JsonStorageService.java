package org.example.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.example.handler.CachedObjectHandler;
import org.example.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class JsonStorageService<T> implements StorageService<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultChainResourceService.class);
    private static final String OUTPUT_DIR = "out";

    private final CachedObjectHandler<T> objectHandler;
    private final String fileName;
    private final int ttl;

    private volatile LocalDateTime updated;

    public JsonStorageService(CachedObjectHandler<T> objectHandler, String fileName, int ttl) {
        this.objectHandler = objectHandler;
        this.fileName = fileName;
        this.ttl = ttl;
    }

    @Override
    public T read() throws IOException {
        var folder = ensureOutputFolder();
        var filePath = folder.resolve(fileName);

        if (!Files.exists(filePath)) {
            return null;
        }

        var value = Files.readString(filePath, StandardCharsets.UTF_8);

        return objectHandler.jsonToObject(value);
    }

    @Override
    public void write(T value) throws IOException {
        var folder = ensureOutputFolder();
        var filePath = folder.resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                                                             StandardCharsets.UTF_8,
                                                             StandardOpenOption.CREATE,
                                                             StandardOpenOption.TRUNCATE_EXISTING)) {
            var toWrite = Optional.ofNullable(value)
                    .map(objectHandler::objectToJSON)
                    .orElse(StringUtils.EMPTY);
            writer.write(toWrite);
        }
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean isExpired() {
        if (Objects.isNull(updated)) {
            try {
                Optional.ofNullable(read())
                        .flatMap(objectHandler::getCreationDate)
                        .ifPresent(createdAt -> updated = createdAt);
            } catch (IOException e) {
                return true;
            }
        }

        if (Objects.isNull(updated)) {
            return true;
        }

        return updated.plusSeconds(ttl).isBefore(LocalDateTime.now());
    }


    protected Path ensureOutputFolder() throws IOException {
        var outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            LOG.info("Creating required output folder 'out'.");
            Files.createDirectories(outputPath);
        }

        return outputPath;
    }

}
