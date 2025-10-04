package org.example.data;

import java.time.LocalDateTime;
import java.util.Map;

public class ExchangeRateList {

    private LocalDateTime createdAt;
    private Map<String, Double> rates;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }
}
