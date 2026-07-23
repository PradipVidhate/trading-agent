package com.pradip.tradingbot.strategy;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.model.Candle;

@Service
public class SupportResistanceService {

    public SupportResistanceResult calculate(List<Candle> candles) {

        if (candles == null || candles.isEmpty()) {
            throw new RuntimeException("No candles available for support/resistance calculation.");
        }

        LocalDate latestDate = candles.stream()
                .map(candle -> candle.getTime().toLocalDate())
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new RuntimeException("No candle date available."));

        LocalDate referenceDate = candles.stream()
                .map(candle -> candle.getTime().toLocalDate())
                .filter(date -> date.isBefore(latestDate))
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new RuntimeException("Previous trading day candles are required."));

        List<Candle> previousDayCandles = candles.stream()
                .filter(candle -> referenceDate.equals(candle.getTime().toLocalDate()))
                .toList();

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

        double pivot = (high + low + close) / 3;
        double support = (2 * pivot) - high;
        double resistance = (2 * pivot) - low;

        SupportResistanceResult result = new SupportResistanceResult();

        result.setReferenceDate(referenceDate);
        result.setPreviousDayHigh(round(high));
        result.setPreviousDayLow(round(low));
        result.setPreviousDayClose(round(close));
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

    public boolean isNearResistance(double price,
                                    double resistance,
                                    double proximityPercent) {

        return percentageDistance(price, resistance) <= proximityPercent;
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
