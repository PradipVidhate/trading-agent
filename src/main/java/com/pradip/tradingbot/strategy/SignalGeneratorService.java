package com.pradip.tradingbot.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.dto.SignalOutcome;
import com.pradip.tradingbot.dto.SignalScanResult;
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
    private static final double MAX_STOP_DISTANCE_PERCENT = 0.25;
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

    public TradingSignal generateSignal(String symbol, String interval, int days) {
        return generateSignal(symbol, interval, days, true);
    }

    public TradingSignal generateScheduledSignal(String symbol, String interval, int days) {
        return generateSignal(symbol, interval, days, false);
    }

    private TradingSignal generateSignal(String symbol,
                                         String interval,
                                         int days,
                                         boolean record) {
        String indexSymbol = requireNiftyIndexSymbol(symbol);
        List<Candle> candles = historicalDataService.getHistoricalData(indexSymbol, interval, days)
                .stream()
                .sorted(Comparator.comparing(Candle::getTime))
                .toList();
        if (candles.isEmpty()) {
            throw new RuntimeException("No candles found for signal generation.");
        }

        LocalDate sessionDate = candles.get(candles.size() - 1).getTime().toLocalDate();
        List<Candle> sessionCandles = candles.stream()
                .filter(candle -> sessionDate.equals(candle.getTime().toLocalDate()))
                .toList();
        String previousDayTrend = supportResistanceService
                .calculate(candles, sessionCandles.get(0).getTime())
                .getPreviousDayTrend();
        List<TradingSignal> sessionSignals = analyzeSession(
                indexSymbol, interval, candles, sessionCandles, previousDayTrend);
        TradingSignal latestSignal = sessionSignals.get(sessionSignals.size() - 1);

        if (record && !"NO_TRADE".equals(latestSignal.getSignal())) {
            dailySignalHistoryService.record(latestSignal);
        }
        return latestSignal;
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

        String previousDayTrend = supportResistanceService
                .calculate(candles, dayCandles.get(0).getTime())
                .getPreviousDayTrend();
        List<TradingSignal> evaluatedSignals = analyzeSession(
                indexSymbol, INTRADAY_INTERVAL, candles, dayCandles, previousDayTrend);

        List<TradingSignal> signals = new ArrayList<>();
        Set<String> emittedSignalsForDay = new HashSet<>();
        for (TradingSignal signal : evaluatedSignals) {
            if (!"NO_TRADE".equals(signal.getSignal())
                    && emittedSignalsForDay.add(signal.getSignal())) {
                signals.add(signal);
            }
        }

        LocalDate today = LocalDate.now(MARKET_ZONE);
        boolean tradingDayComplete = date.isBefore(today)
                || (date.equals(today) && LocalTime.now(MARKET_ZONE).isAfter(LocalTime.of(15, 30)));
        return signalOutcomeService.evaluate(signals, dayCandles, tradingDayComplete);
    }

    private List<TradingSignal> analyzeSession(String indexSymbol,
                                               String interval,
                                               List<Candle> allCandles,
                                               List<Candle> sessionCandles,
                                               String previousDayTrend) {
        FiveMinuteTrendState trendState = new FiveMinuteTrendState(previousDayTrend);
        List<TradingSignal> signals = new ArrayList<>();

        for (Candle candle : sessionCandles) {
            SupportResistanceResult levels =
                    supportResistanceService.calculate(allCandles, candle.getTime());
            trendState.observe(candle, levels);
            signals.add(createSignal(
                    indexSymbol, interval, candle, levels, previousDayTrend, trendState));
        }
        return signals;
    }

    private TradingSignal createSignal(String indexSymbol,
                                      String interval,
                                      Candle candle,
                                      SupportResistanceResult levels,
                                      String previousDayTrend,
                                      FiveMinuteTrendState trendState) {
        double close = candle.getClose();
        String currentTrend = trendState.getCurrentTrend();
        String signal = "NO_TRADE";
        String reason = "Price is not near support or resistance.";

        if (trendState.isSustainedSupportBreak()) {
            signal = "BUY_PE";
            reason = "Two consecutive five-minute closes broke recent chart support.";
        } else if (trendState.isSustainedResistanceBreak()) {
            signal = "BUY_CE";
            reason = "Two consecutive five-minute closes broke recent chart resistance.";
        } else if (supportResistanceService.isNearSupport(
                candle, levels.getSupport(), DEFAULT_PROXIMITY_PERCENT)
                && close >= levels.getSupport()) {
            if (!"BEARISH".equals(currentTrend)) {
                signal = "BUY_CE";
                reason = "Price bounced from or approached recent five-minute chart support.";
            } else {
                reason = "Support bounce ignored because the current trend is bearish.";
            }
        } else if (supportResistanceService.isNearResistance(
                candle, levels.getResistance(), DEFAULT_PROXIMITY_PERCENT)
                && close <= levels.getResistance()) {
            if (!"BULLISH".equals(currentTrend)) {
                signal = "BUY_PE";
                reason = "Price was rejected from or approached recent five-minute chart resistance.";
            } else {
                reason = "Resistance rejection ignored because the current trend is bullish.";
            }
        }

        TradingSignal tradingSignal = new TradingSignal();
        tradingSignal.setSymbol(indexSymbol);
        tradingSignal.setInterval(interval);
        tradingSignal.setSignal(signal);
        tradingSignal.setReason(reason);
        tradingSignal.setPreviousDayTrend(previousDayTrend);
        tradingSignal.setCurrentTrend(currentTrend);
        tradingSignal.setTrendReversalConfirmed(!previousDayTrend.equals(currentTrend));
        tradingSignal.setCandleTime(candle.getTime());
        tradingSignal.setLastClose(round(close));
        tradingSignal.setSupport(levels.getSupport());
        tradingSignal.setResistance(levels.getResistance());
        tradingSignal.setPivot(levels.getPivot());
        applyRiskLevels(tradingSignal, trendState);
        tradingSignal.setProximityPercent(DEFAULT_PROXIMITY_PERCENT);
        return tradingSignal;
    }

    public SignalScanResult scanSignals(List<String> symbols, String interval, int days) {
        SignalScanResult result = new SignalScanResult();
        for (String symbol : symbols) {
            String normalizedSymbol = symbol.trim();
            if (normalizedSymbol.isBlank()) {
                continue;
            }

            try {
                result.getSignals().add(generateSignal(normalizedSymbol, interval, days));
            } catch (RuntimeException ex) {
                result.getErrors().add(normalizedSymbol.toUpperCase() + " : " + ex.getMessage());
            }
        }
        return result;
    }

    public String requireNiftyIndexSymbol(String symbol) {
        if (!instrumentService.isNiftyIndexSymbol(symbol)) {
            throw new RuntimeException("This strategy is currently enabled only for NIFTY 50 index.");
        }
        return instrumentService.getNiftyIndexSymbol();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void applyRiskLevels(TradingSignal signal, FiveMinuteTrendState trendState) {
        double stopLoss = 0;
        double target = 0;
        double range = signal.getResistance() - signal.getSupport();

        if ("BUY_CE".equals(signal.getSignal())) {
            if (trendState.isSustainedResistanceBreak()) {
                stopLoss = trendState.getResistanceBreakLevel()
                        * (1 - STOP_LOSS_BUFFER_PERCENT / 100);
                target = signal.getResistance() + range;
            } else {
                stopLoss = signal.getSupport() * (1 - STOP_LOSS_BUFFER_PERCENT / 100);
                target = signal.getResistance();
            }
        } else if ("BUY_PE".equals(signal.getSignal())) {
            if (trendState.isSustainedSupportBreak()) {
                stopLoss = trendState.getSupportBreakLevel()
                        * (1 + STOP_LOSS_BUFFER_PERCENT / 100);
                target = signal.getSupport() - range;
            } else {
                stopLoss = signal.getResistance() * (1 + STOP_LOSS_BUFFER_PERCENT / 100);
                target = signal.getSupport();
            }
        }

        if (!"NO_TRADE".equals(signal.getSignal())) {
            boolean call = "BUY_CE".equals(signal.getSignal());
            double entry = signal.getLastClose();
            double stopDistance = call ? entry - stopLoss : stopLoss - entry;
            boolean targetOnProfitableSide = call ? target > entry : target < entry;
            boolean stopOnProtectiveSide = stopDistance > 0;
            double maxStopDistance = entry * MAX_STOP_DISTANCE_PERCENT / 100;

            if (!stopOnProtectiveSide || !targetOnProfitableSide) {
                signal.setSignal("NO_TRADE");
                signal.setReason("Entry skipped because stop or target is on the wrong side of entry.");
                stopLoss = 0;
                target = 0;
            } else if (stopDistance > maxStopDistance) {
                signal.setSignal("NO_TRADE");
                signal.setReason("Entry skipped because stop distance exceeds 0.25% of NIFTY price.");
                stopLoss = 0;
                target = 0;
            }
        }

        signal.setStopLoss(round(stopLoss));
        signal.setTarget(round(target));
        signal.setRiskRewardRatio(calculateRiskRewardRatio(
                signal.getSignal(), signal.getLastClose(), signal.getStopLoss(), signal.getTarget()));
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
        return risk == 0 ? 0 : round(reward / risk);
    }
}
