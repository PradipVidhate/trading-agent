package com.pradip.tradingbot.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.dto.SignalOutcome;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.model.Candle;

@Service
public class SignalOutcomeService {

    public List<SignalOutcome> evaluate(List<TradingSignal> signals,
                                       List<Candle> dayCandles,
                                       boolean tradingDayComplete) {
        return signals.stream()
                .map(signal -> evaluate(signal, dayCandles, tradingDayComplete))
                .toList();
    }

    private SignalOutcome evaluate(TradingSignal signal,
                                   List<Candle> dayCandles,
                                   boolean tradingDayComplete) {
        LocalDateTime signalTime = signal.getCandleTime();
        LocalDate signalDate = signalTime.toLocalDate();
        boolean isBuy = "BUY".equals(signal.getSignal());

        List<Candle> futureCandles = dayCandles.stream()
                .filter(candle -> candle.getTime().toLocalDate().equals(signalDate))
                .filter(candle -> candle.getTime().isAfter(signalTime))
                .sorted(Comparator.comparing(Candle::getTime))
                .toList();

        for (Candle candle : futureCandles) {
            boolean stopTouched = isBuy
                    ? candle.getLow() <= signal.getStopLoss()
                    : candle.getHigh() >= signal.getStopLoss();
            boolean targetTouched = isBuy
                    ? candle.getHigh() >= signal.getTarget()
                    : candle.getLow() <= signal.getTarget();

            if (stopTouched) {
                return outcomeAtPrice(signal, candle, signal.getStopLoss(), "STOP_LOSS", true);
            }
            if (targetTouched) {
                return outcomeAtPrice(signal, candle, signal.getTarget(), "TARGET", true);
            }
        }

        if (futureCandles.isEmpty()) {
            return new SignalOutcome(signal, "UNRESOLVED", "NO_FOLLOW_UP_CANDLE",
                    null, null, null, null);
        }

        Candle finalCandle = futureCandles.get(futureCandles.size() - 1);
        return tradingDayComplete
            ? outcomeAtPrice(signal, finalCandle, finalCandle.getClose(), "DAY_CLOSE", true)
            : outcomeAtPrice(signal, finalCandle, finalCandle.getClose(), "AS_OF_LAST_CANDLE", false);
    }

        private SignalOutcome outcomeAtPrice(TradingSignal signal,
                         Candle exitCandle,
                         double exitPrice,
                         String exitReason,
                         boolean realized) {
        boolean isBuy = "BUY".equals(signal.getSignal());
        double rawPoints = isBuy
                ? exitPrice - signal.getLastClose()
                : signal.getLastClose() - exitPrice;
        double points = round(rawPoints);
        double percent = round(rawPoints / signal.getLastClose() * 100);
        String outcome = realized
            ? points > 0 ? "PROFIT" : points < 0 ? "LOSS" : "BREAKEVEN"
            : "OPEN";

        return new SignalOutcome(signal, outcome, exitReason, exitCandle.getTime(),
                round(exitPrice), points, percent);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}