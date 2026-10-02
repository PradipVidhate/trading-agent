package com.pradip.tradingbot.strategy;

import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.model.Candle;

final class FiveMinuteTrendState {

    private String currentTrend;
    private Double supportBreakLevel;
    private Double resistanceBreakLevel;
    private int consecutiveClosesBelowSupport;
    private int consecutiveClosesAboveResistance;

    FiveMinuteTrendState(String previousDayTrend) {
        this.currentTrend = previousDayTrend;
    }

    void observe(Candle candle, SupportResistanceResult levels) {
        double close = candle.getClose();

        if (close < levels.getSupport()) {
            if (supportBreakLevel != null && close < supportBreakLevel) {
                consecutiveClosesBelowSupport++;
            } else {
                supportBreakLevel = levels.getSupport();
                consecutiveClosesBelowSupport = 1;
            }
        } else {
            supportBreakLevel = null;
            consecutiveClosesBelowSupport = 0;
        }

        if (close > levels.getResistance()) {
            if (resistanceBreakLevel != null && close > resistanceBreakLevel) {
                consecutiveClosesAboveResistance++;
            } else {
                resistanceBreakLevel = levels.getResistance();
                consecutiveClosesAboveResistance = 1;
            }
        } else {
            resistanceBreakLevel = null;
            consecutiveClosesAboveResistance = 0;
        }

        if (isSustainedSupportBreak()) {
            currentTrend = "BEARISH";
        } else if (isSustainedResistanceBreak()) {
            currentTrend = "BULLISH";
        }
    }

    String getCurrentTrend() {
        return currentTrend;
    }

    boolean isSustainedSupportBreak() {
        return consecutiveClosesBelowSupport >= 2;
    }

    boolean isSustainedResistanceBreak() {
        return consecutiveClosesAboveResistance >= 2;
    }

    double getSupportBreakLevel() {
        return supportBreakLevel == null ? 0 : supportBreakLevel;
    }

    double getResistanceBreakLevel() {
        return resistanceBreakLevel == null ? 0 : resistanceBreakLevel;
    }
}