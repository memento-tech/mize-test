package org.example.service.impl;
import org.apache.commons.collections4.CollectionUtils;
import org.example.service.ChainResourceService;
import org.example.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultChainResourceService<T> implements ChainResourceService<T> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultChainResourceService.class);

    private final List<StorageService<T>> storages;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public DefaultChainResourceService(List<StorageService<T>> storages) {
        this.storages = CollectionUtils.emptyIfNull(storages)
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public T read() throws IOException {
        if (storages.isEmpty()) {
            return null;
        }

        var visitedStorages = new ArrayList<StorageService<T>>();
        T result = null;

        var threadName = Thread.currentThread().getName();

        lock.readLock().lock();
        try {
            LOG.debug("{} acquired READ lock.", threadName);

            for (StorageService<T> storage : storages) {
                // In this step do not check read-only storages since we will not write anything yet
                if (storage.isReadOnly()) {
                    continue;
                }
                if (!storage.isExpired()) {
                    result = storage.read();
                } else {
                    visitedStorages.add(storage);
                }
            }
        } finally {
            lock.readLock().unlock();
            LOG.debug("{} released READ lock.", threadName);
        }

        // If visited storages list is empty and result is null, this means that there are only 'read-only' storages available
        if (visitedStorages.isEmpty() && Objects.nonNull(result)) {
            return result;
        }

        lock.writeLock().lock();
        try {
            LOG.debug("{} acquired WRITE lock before double check.", threadName);

            // Double check if  some other thread haven't updated storages (non-read-only only)
            if (Objects.isNull(result)) {
                for (StorageService<T> storage: storages) {
                    if (!storage.isExpired() && !storage.isReadOnly()) {
                        return storage.read();
                    }
                }
            }

            LOG.debug("{} >>> refreshing cache.", threadName);

            // Get first good storage based on size of visited storages (arrays start from 0, size from 1)
            var goodStorage = storages.get(visitedStorages.size());
            result = goodStorage.read();

            // Finally refresh storages that are visited and contain expired data
            for (StorageService<T> visitedStorage: visitedStorages) {
                if (!visitedStorage.isReadOnly()) {
                    LOG.debug("{} writing refreshed data into.", visitedStorage.getClass().getSimpleName());
                    visitedStorage.write(result);
                }
            }
        } finally {
            lock.writeLock().unlock();
            LOG.debug("{} released WRITE lock.", threadName);
        }

        return result;
    }

}
