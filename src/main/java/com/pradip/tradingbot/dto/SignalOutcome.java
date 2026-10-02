package com.pradip.tradingbot.dto;

import java.time.LocalDateTime;

public class SignalOutcome {

    private TradingSignal signal;
    private String outcome;
    private String exitReason;
    private LocalDateTime exitTime;
    private Double exitPrice;
    private Double profitLossPoints;
    private Double profitLossPercent;

    public SignalOutcome(TradingSignal signal,
                         String outcome,
                         String exitReason,
                         LocalDateTime exitTime,
                         Double exitPrice,
                         Double profitLossPoints,
                         Double profitLossPercent) {
        this.signal = signal;
        this.outcome = outcome;
        this.exitReason = exitReason;
        this.exitTime = exitTime;
        this.exitPrice = exitPrice;
        this.profitLossPoints = profitLossPoints;
        this.profitLossPercent = profitLossPercent;
    }

    public TradingSignal getSignal() {
        return signal;
    }

    public void setSignal(TradingSignal signal) {
        this.signal = signal;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getExitReason() {
        return exitReason;
    }

    public void setExitReason(String exitReason) {
        this.exitReason = exitReason;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public Double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(Double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public Double getProfitLossPoints() {
        return profitLossPoints;
    }

    public void setProfitLossPoints(Double profitLossPoints) {
        this.profitLossPoints = profitLossPoints;
    }

    public Double getProfitLossPercent() {
        return profitLossPercent;
    }

    public void setProfitLossPercent(Double profitLossPercent) {
        this.profitLossPercent = profitLossPercent;
    }
}