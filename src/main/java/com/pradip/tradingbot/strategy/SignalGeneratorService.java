package com.pradip.tradingbot.strategy;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.dto.SignalScanResult;
import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.model.Candle;
import com.pradip.tradingbot.service.HistoricalDataService;
import com.pradip.tradingbot.service.InstrumentService;

@Service
public class SignalGeneratorService {

    private static final double DEFAULT_PROXIMITY_PERCENT = 0.30;

    private final HistoricalDataService historicalDataService;
    private final InstrumentService instrumentService;
    private final SupportResistanceService supportResistanceService;

    public SignalGeneratorService(HistoricalDataService historicalDataService,
                                  InstrumentService instrumentService,
                                  SupportResistanceService supportResistanceService) {

        this.historicalDataService = historicalDataService;
        this.instrumentService = instrumentService;
        this.supportResistanceService = supportResistanceService;
    }

    public TradingSignal generateSignal(String symbol,
                                        String interval,
                                        int days) {

        String indexSymbol = requireNiftyIndexSymbol(symbol);

        List<Candle> candles =
                historicalDataService.getHistoricalData(indexSymbol, interval, days);

        if (candles.isEmpty()) {
            throw new RuntimeException("No candles found for signal generation.");
        }

        SupportResistanceResult levels =
                supportResistanceService.calculate(candles);

        Candle latestCandle = candles.stream()
                .max(Comparator.comparing(Candle::getTime))
                .orElseThrow(() -> new RuntimeException("Latest candle could not be found."));

        double lastClose = latestCandle.getClose();
        String signal = "NO_TRADE";
        String reason = "Price is not near support or resistance.";

        if (supportResistanceService.isNearSupport(
                lastClose,
                levels.getSupport(),
                DEFAULT_PROXIMITY_PERCENT)) {

            signal = "BUY";
            reason = "Price is near calculated support.";

        } else if (supportResistanceService.isNearResistance(
                lastClose,
                levels.getResistance(),
                DEFAULT_PROXIMITY_PERCENT)) {

            signal = "SELL";
            reason = "Price is near calculated resistance.";
        }

        TradingSignal tradingSignal = new TradingSignal();

        tradingSignal.setSymbol(indexSymbol);
        tradingSignal.setInterval(interval);
        tradingSignal.setSignal(signal);
        tradingSignal.setReason(reason);
        tradingSignal.setCandleTime(latestCandle.getTime());
        tradingSignal.setLastClose(round(lastClose));
        tradingSignal.setSupport(levels.getSupport());
        tradingSignal.setResistance(levels.getResistance());
        tradingSignal.setPivot(levels.getPivot());
        tradingSignal.setProximityPercent(DEFAULT_PROXIMITY_PERCENT);

        return tradingSignal;
    }

    public SignalScanResult scanSignals(List<String> symbols,
                                        String interval,
                                        int days) {

        SignalScanResult result = new SignalScanResult();

        for (String symbol : symbols) {
            String normalizedSymbol = symbol.trim();

            if (normalizedSymbol.isBlank()) {
                continue;
            }

            try {
                TradingSignal signal =
                        generateSignal(normalizedSymbol, interval, days);

                result.getSignals().add(signal);

            } catch (RuntimeException ex) {
                result.getErrors().add(
                        normalizedSymbol.toUpperCase() + " : " + ex.getMessage());
            }
        }

        return result;
    }

    public String requireNiftyIndexSymbol(String symbol) {

        if (!instrumentService.isNiftyIndexSymbol(symbol)) {
            throw new RuntimeException(
                    "This strategy is currently enabled only for NIFTY 50 index.");
        }

        return instrumentService.getNiftyIndexSymbol();
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}
