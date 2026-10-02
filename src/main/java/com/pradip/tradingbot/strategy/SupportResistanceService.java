package com.pradip.tradingbot.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.model.Candle;

@Service
public class SupportResistanceService {

    private static final int LEVEL_LOOKBACK_CANDLES = 12;

    public SupportResistanceResult calculate(List<Candle> candles) {

        if (candles == null || candles.isEmpty()) {
            throw new RuntimeException("No candles available for support/resistance calculation.");
        }

        LocalDateTime latestCandleTime = candles.stream()
                .map(Candle::getTime)
                .max(LocalDateTime::compareTo)
                .orElseThrow(() -> new RuntimeException("No candle time available."));

        return calculate(candles, latestCandleTime);
    }

    public SupportResistanceResult calculate(List<Candle> candles, LocalDateTime beforeTime) {

        if (candles == null || candles.isEmpty()) {
            throw new RuntimeException("No candles available for support/resistance calculation.");
        }

        LocalDate analysisDate = beforeTime.toLocalDate();

        LocalDate referenceDate = candles.stream()
                .map(candle -> candle.getTime().toLocalDate())
                .filter(date -> date.isBefore(analysisDate))
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new RuntimeException("Previous trading day candles are required."));

        List<Candle> previousDayCandles = candles.stream()
                .filter(candle -> referenceDate.equals(candle.getTime().toLocalDate()))
                .toList();

        double open = previousDayCandles.stream()
            .min(Comparator.comparing(Candle::getTime))
            .map(Candle::getOpen)
            .orElseThrow(() -> new RuntimeException("Previous day open could not be calculated."));
        double high = previousDayCandles.stream()
                .mapToDouble(Candle::getHigh)
                .max()
                .orElseThrow(() -> new RuntimeException("Previous day high could not be calculated."));

        double low = previousDayCandles.stream()
                .mapToDouble(Candle::getLow)
                .min()
                .orElseThrow(() -> new RuntimeException("Previous day low could not be calculated."));

        double close = previousDayCandles.stream()
                .max(Comparator.comparing(Candle::getTime))
                .map(Candle::getClose)
                .orElseThrow(() -> new RuntimeException("Previous day close could not be calculated."));

        List<Candle> recentCandles = candles.stream()
            .filter(candle -> candle.getTime().isBefore(beforeTime))
            .sorted(Comparator.comparing(Candle::getTime).reversed())
            .limit(LEVEL_LOOKBACK_CANDLES)
            .toList();
        if (recentCandles.isEmpty()) {
            throw new RuntimeException("No completed five-minute candles precede the analysis candle.");
        }

        double support = recentCandles.stream()
            .mapToDouble(Candle::getLow)
            .min()
            .orElseThrow(() -> new RuntimeException("Recent chart support could not be calculated."));
        double resistance = recentCandles.stream()
            .mapToDouble(Candle::getHigh)
            .max()
            .orElseThrow(() -> new RuntimeException("Recent chart resistance could not be calculated."));
        double pivot = (support + resistance) / 2;

        SupportResistanceResult result = new SupportResistanceResult();

        result.setReferenceDate(referenceDate);
        result.setPreviousDayOpen(round(open));
        result.setPreviousDayHigh(round(high));
        result.setPreviousDayLow(round(low));
        result.setPreviousDayClose(round(close));
        result.setPreviousDayTrend(close > open ? "BULLISH" : close < open ? "BEARISH" : "SIDEWAYS");
        result.setPivot(round(pivot));
        result.setSupport(round(support));
        result.setResistance(round(resistance));

        return result;
    }

    public boolean isNearSupport(double price,
                                 double support,
                                 double proximityPercent) {

        return percentageDistance(price, support) <= proximityPercent;
    }

    public boolean isNearSupport(Candle candle,
                                 double support,
                                 double proximityPercent) {

        return candleTouchesOrApproaches(candle, support, proximityPercent);
    }

    public boolean isNearResistance(double price,
                                    double resistance,
                                    double proximityPercent) {

        return percentageDistance(price, resistance) <= proximityPercent;
    }

    public boolean isNearResistance(Candle candle,
                                    double resistance,
                                    double proximityPercent) {

        return candleTouchesOrApproaches(candle, resistance, proximityPercent);
    }

    private boolean candleTouchesOrApproaches(Candle candle,
                                              double level,
                                              double proximityPercent) {
        if (candle.getLow() <= level && candle.getHigh() >= level) {
            return true;
        }

        double nearestPrice = candle.getHigh() < level ? candle.getHigh() : candle.getLow();
        return percentageDistance(nearestPrice, level) <= proximityPercent;
    }

    private double percentageDistance(double price, double level) {

        if (level == 0) {
            return Double.MAX_VALUE;
        }

        return Math.abs(price - level) / level * 100;
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }

}
