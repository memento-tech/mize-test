package org.example;

import org.example.data.ExchangeRateList;
import org.example.handler.impl.ExchangeRateHandler;
import org.example.service.impl.DefaultChainResourceService;
import org.example.service.impl.JsonStorageService;
import org.example.service.impl.MemoryStorageService;
import org.example.service.impl.WebStorageService;

import java.io.IOException;
import java.util.List;

public class Main {

    private static final String OPEN_EXCHANGE_RATES_APP_ID = "70993b5c8e56434b9a1beae168ec46fb";
    private static final String OPEN_EXCHANGE_RATES_URL = "https://openexchangerates.org/" +
                                                          "api/latest.json" +
                                                          "?app_id=" + OPEN_EXCHANGE_RATES_APP_ID;

    private static final int MEMORY_STORAGE_TTL_IN_SECONDS = 3600;
    private static final int JSON_STORAGE_TTL_IN_SECONDS = 14400;
    private static final String FILE_STORAGE_NAME = "test.json";

    public static void main(String[] args) throws IOException {
        var exchangeRateMapper = new ExchangeRateHandler();
        var memoryStorage = new MemoryStorageService<ExchangeRateList>(MEMORY_STORAGE_TTL_IN_SECONDS);
        var jsonStorage = new JsonStorageService<>(exchangeRateMapper, FILE_STORAGE_NAME, JSON_STORAGE_TTL_IN_SECONDS);
        var webStorage = new WebStorageService<>(OPEN_EXCHANGE_RATES_URL, exchangeRateMapper);

        var chainResource = new DefaultChainResourceService<>(List.of(memoryStorage, jsonStorage, webStorage));

        var s = chainResource.read();
    }

//    public static void main(String[] args) throws IOException {
//        var exchangeRateMapper = new ExchangeRateHandler();
//        var memoryStorage = new MemoryStorageService<ExchangeRateList>(1);
//        var jsonStorage = new JsonStorageService<>(exchangeRateMapper, FILE_STORAGE_NAME, 3);
//        var webStorage = new WebStorageService<>(OPEN_EXCHANGE_RATES_URL, exchangeRateMapper);
//
//        var chainResource = new DefaultChainResourceService<>(List.of(memoryStorage, jsonStorage, webStorage));
//
//        int threadCount = 10; // number of concurrent threads
//        var executor = Executors.newFixedThreadPool(threadCount);
//
//        for (int i = 0; i < threadCount; i++) {
//            executor.submit(() -> {
//                try {
//                    for (int j = 0; j < 10; j++) { // repeat access 10 times
//                        var result = chainResource.read();
//                        Thread.sleep(500); // 0.5 second between reads
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        executor.shutdown();
//    }
}