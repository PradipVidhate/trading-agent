package com.pradip.tradingbot.dto;

import java.time.LocalDateTime;

public class DailySignalEntry {

    private LocalDateTime generatedAt;
    private TradingSignal tradingSignal;

    public DailySignalEntry(LocalDateTime generatedAt, TradingSignal tradingSignal) {
        this.generatedAt = generatedAt;
        this.tradingSignal = tradingSignal;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public TradingSignal getTradingSignal() {
        return tradingSignal;
    }

    public void setTradingSignal(TradingSignal tradingSignal) {
        this.tradingSignal = tradingSignal;
    }
}