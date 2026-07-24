package com.pradip.tradingbot.dto;

import java.time.LocalDateTime;

public class ScheduledSignalStatus {

    private boolean enabled;
    private LocalDateTime lastRunAt;
    private TradingSignal lastSignal;
    private String lastError;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(LocalDateTime lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public TradingSignal getLastSignal() {
        return lastSignal;
    }

    public void setLastSignal(TradingSignal lastSignal) {
        this.lastSignal = lastSignal;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
