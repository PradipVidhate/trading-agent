package com.pradip.tradingbot.dto;

import java.time.LocalDate;

public class SupportResistanceResult {

    private LocalDate referenceDate;
    private double previousDayOpen;
    private double previousDayHigh;
    private double previousDayLow;
    private double previousDayClose;
    private String previousDayTrend;
    private double pivot;
    private double support;
    private double resistance;

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    public double getPreviousDayOpen() {
        return previousDayOpen;
    }

    public void setPreviousDayOpen(double previousDayOpen) {
        this.previousDayOpen = previousDayOpen;
    }

    public double getPreviousDayHigh() {
        return previousDayHigh;
    }

    public void setPreviousDayHigh(double previousDayHigh) {
        this.previousDayHigh = previousDayHigh;
    }

    public double getPreviousDayLow() {
        return previousDayLow;
    }

    public void setPreviousDayLow(double previousDayLow) {
        this.previousDayLow = previousDayLow;
    }

    public double getPreviousDayClose() {
        return previousDayClose;
    }

    public void setPreviousDayClose(double previousDayClose) {
        this.previousDayClose = previousDayClose;
    }

    public String getPreviousDayTrend() {
        return previousDayTrend;
    }

    public void setPreviousDayTrend(String previousDayTrend) {
        this.previousDayTrend = previousDayTrend;
    }

    public double getPivot() {
        return pivot;
    }

    public void setPivot(double pivot) {
        this.pivot = pivot;
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
}
