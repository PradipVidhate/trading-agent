package com.pradip.tradingbot.strategy;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.dto.SignalScanResult;
import com.pradip.tradingbot.dto.SignalOutcome;
import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.model.Candle;
import com.pradip.tradingbot.service.DailySignalHistoryService;
import com.pradip.tradingbot.service.HistoricalDataService;
import com.pradip.tradingbot.service.InstrumentService;

@Service
public class SignalGeneratorService {

    private static final double DEFAULT_PROXIMITY_PERCENT = 0.30;
    private static final double STOP_LOSS_BUFFER_PERCENT = 0.10;
    private static final String INTRADAY_INTERVAL = "5minute";
    private static final int PREVIOUS_SESSION_LOOKBACK_DAYS = 10;
    private static final ZoneId MARKET_ZONE = ZoneId.of("Asia/Kolkata");

    private final HistoricalDataService historicalDataService;
    private final InstrumentService instrumentService;
    private final SupportResistanceService supportResistanceService;
    private final SignalOutcomeService signalOutcomeService;
    private final DailySignalHistoryService dailySignalHistoryService;

    public SignalGeneratorService(HistoricalDataService historicalDataService,
                                  InstrumentService instrumentService,
                                  SupportResistanceService supportResistanceService,
                                  SignalOutcomeService signalOutcomeService,
                                  DailySignalHistoryService dailySignalHistoryService) {

        this.historicalDataService = historicalDataService;
        this.instrumentService = instrumentService;
        this.supportResistanceService = supportResistanceService;
        this.signalOutcomeService = signalOutcomeService;
        this.dailySignalHistoryService = dailySignalHistoryService;
    }

    public TradingSignal generateSignal(String symbol,
                                        String interval,
                                        int days) {

        return generateSignal(symbol, interval, days, true);
    }

    public TradingSignal generateScheduledSignal(String symbol,
                                                 String interval,
                                                 int days) {

        return generateSignal(symbol, interval, days, false);
    }

    private TradingSignal generateSignal(String symbol,
                                         String interval,
                                         int days,
                                         boolean record) {

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

        TradingSignal tradingSignal = createSignal(indexSymbol, interval, latestCandle, levels);
        if (record && !"NO_TRADE".equals(tradingSignal.getSignal())) {
            dailySignalHistoryService.record(tradingSignal);
        }

        return tradingSignal;
    }

    public List<SignalOutcome> generateSignalsForDay(String symbol, LocalDate date) {

        if (date == null) {
            throw new RuntimeException("A trading date is required.");
        }
        if (date.isAfter(LocalDate.now(MARKET_ZONE))) {
            throw new RuntimeException("Trading date cannot be in the future.");
        }

        String indexSymbol = requireNiftyIndexSymbol(symbol);
        LocalDateTime from = date.minusDays(PREVIOUS_SESSION_LOOKBACK_DAYS).atStartOfDay();
        LocalDateTime to = date.atTime(23, 59, 59);
        List<Candle> candles = historicalDataService.getHistoricalData(
                indexSymbol, INTRADAY_INTERVAL, from, to);

        List<Candle> dayCandles = candles.stream()
                .filter(candle -> date.equals(candle.getTime().toLocalDate()))
                .sorted(Comparator.comparing(Candle::getTime))
                .toList();
        if (dayCandles.isEmpty()) {
            throw new RuntimeException("No NIFTY 50 5-minute candles found for " + date + ".");
        }

        SupportResistanceResult levels = supportResistanceService.calculate(candles);
        List<TradingSignal> signals = new ArrayList<>();
        String lastActionableSignal = "NO_TRADE";
        for (Candle candle : dayCandles) {
            TradingSignal signal = createSignal(indexSymbol, INTRADAY_INTERVAL, candle, levels);
            if ("NO_TRADE".equals(signal.getSignal())) {
                lastActionableSignal = "NO_TRADE";
                continue;
            }

            if (!signal.getSignal().equals(lastActionableSignal)) {
                signals.add(signal);
            }
            lastActionableSignal = signal.getSignal();
        }

        LocalDate today = LocalDate.now(MARKET_ZONE);
        boolean tradingDayComplete = date.isBefore(today)
            || (date.equals(today) && LocalTime.now(MARKET_ZONE).isAfter(LocalTime.of(15, 30)));
        return signalOutcomeService.evaluate(signals, dayCandles, tradingDayComplete);
    }

    private TradingSignal createSignal(String indexSymbol,
                                      String interval,
                                      Candle candle,
                                      SupportResistanceResult levels) {

        double lastClose = candle.getClose();
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
        tradingSignal.setCandleTime(candle.getTime());
        tradingSignal.setLastClose(round(lastClose));
        tradingSignal.setSupport(levels.getSupport());
        tradingSignal.setResistance(levels.getResistance());
        tradingSignal.setPivot(levels.getPivot());
        applyRiskLevels(tradingSignal);
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

    private void applyRiskLevels(TradingSignal tradingSignal) {

        double stopLoss = 0;
        double target = 0;

        if ("BUY".equals(tradingSignal.getSignal())) {
            stopLoss = tradingSignal.getSupport()
                    * (1 - STOP_LOSS_BUFFER_PERCENT / 100);
            target = tradingSignal.getResistance();

        } else if ("SELL".equals(tradingSignal.getSignal())) {
            stopLoss = tradingSignal.getResistance()
                    * (1 + STOP_LOSS_BUFFER_PERCENT / 100);
            target = tradingSignal.getSupport();
        }

        tradingSignal.setStopLoss(round(stopLoss));
        tradingSignal.setTarget(round(target));
        tradingSignal.setRiskRewardRatio(
                calculateRiskRewardRatio(
                        tradingSignal.getSignal(),
                        tradingSignal.getLastClose(),
                        tradingSignal.getStopLoss(),
                        tradingSignal.getTarget()));
    }

    private double calculateRiskRewardRatio(String signal,
                                            double entry,
                                            double stopLoss,
                                            double target) {

        if ("NO_TRADE".equals(signal) || stopLoss == 0 || target == 0) {
            return 0;
        }

        double risk = Math.abs(entry - stopLoss);
        double reward = Math.abs(target - entry);

        if (risk == 0) {
            return 0;
        }

        return round(reward / risk);
    }
}
