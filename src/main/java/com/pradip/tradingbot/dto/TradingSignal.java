package com.pradip.tradingbot.dto;

import java.time.LocalDateTime;

public class TradingSignal {

    private String symbol;
    private String interval;
    private String signal;
    private String reason;
    private String previousDayTrend;
    private String currentTrend;
    private boolean trendReversalConfirmed;
    private LocalDateTime candleTime;
    private double lastClose;
    private double support;
    private double resistance;
    private double pivot;
    private double stopLoss;
    private double target;
    private double riskRewardRatio;
    private double proximityPercent;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPreviousDayTrend() {
        return previousDayTrend;
    }

    public void setPreviousDayTrend(String previousDayTrend) {
        this.previousDayTrend = previousDayTrend;
    }

    public String getCurrentTrend() {
        return currentTrend;
    }

    public void setCurrentTrend(String currentTrend) {
        this.currentTrend = currentTrend;
    }

    public boolean isTrendReversalConfirmed() {
        return trendReversalConfirmed;
    }

    public void setTrendReversalConfirmed(boolean trendReversalConfirmed) {
        this.trendReversalConfirmed = trendReversalConfirmed;
    }

    public LocalDateTime getCandleTime() {
        return candleTime;
    }

    public void setCandleTime(LocalDateTime candleTime) {
        this.candleTime = candleTime;
    }

    public double getLastClose() {
        return lastClose;
    }

    public void setLastClose(double lastClose) {
        this.lastClose = lastClose;
    }

    public double getSupport() {
        return support;
    }

    public void setSupport(double support) {
        this.support = support;
    }

    public double getResistance() {
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }

    public double getPivot() {
        return pivot;
    }

    public void setPivot(double pivot) {
        this.pivot = pivot;
    }

    public double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public double getRiskRewardRatio() {
        return riskRewardRatio;
    }

    public void setRiskRewardRatio(double riskRewardRatio) {
        this.riskRewardRatio = riskRewardRatio;
    }

    public double getProximityPercent() {
        return proximityPercent;
    }

    public void setProximityPercent(double proximityPercent) {
        this.proximityPercent = proximityPercent;
    }
}
