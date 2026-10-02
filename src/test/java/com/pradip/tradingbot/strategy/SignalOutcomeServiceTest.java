package com.pradip.tradingbot.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pradip.tradingbot.dto.SignalOutcome;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.model.Candle;

class SignalOutcomeServiceTest {

    private final SignalOutcomeService service = new SignalOutcomeService();

    @Test
    void marksTargetAsProfitUsingOnlyCandlesAfterSignal() {
        TradingSignal signal = signal("BUY", 100, 95, 105, 9, 15);
        List<Candle> candles = List.of(
                candle(9, 15, 100, 100, 100),
                candle(9, 20, 102, 99, 101),
                candle(9, 25, 106, 100, 105));

        SignalOutcome outcome = service.evaluate(List.of(signal), candles, true).get(0);

        assertThat(outcome.getOutcome()).isEqualTo("PROFIT");
        assertThat(outcome.getExitReason()).isEqualTo("TARGET");
        assertThat(outcome.getExitTime()).isEqualTo(LocalDateTime.of(2026, 10, 2, 9, 25));
        assertThat(outcome.getProfitLossPoints()).isEqualTo(5.0);
    }

    @Test
    void treatsStopAsFirstWhenOneCandleTouchesStopAndTarget() {
        TradingSignal signal = signal("BUY", 100, 95, 105, 9, 15);
        List<Candle> candles = List.of(
                candle(9, 15, 100, 100, 100),
                candle(9, 20, 106, 94, 100));

        SignalOutcome outcome = service.evaluate(List.of(signal), candles, true).get(0);

        assertThat(outcome.getOutcome()).isEqualTo("LOSS");
        assertThat(outcome.getExitReason()).isEqualTo("STOP_LOSS");
        assertThat(outcome.getProfitLossPoints()).isEqualTo(-5.0);
    }

    @Test
    void marksSignalWithoutFutureCandleUnresolved() {
        TradingSignal signal = signal("SELL", 100, 105, 95, 15, 25);

        SignalOutcome outcome = service.evaluate(
                List.of(signal), List.of(candle(15, 25, 100, 100, 100)), true).get(0);

        assertThat(outcome.getOutcome()).isEqualTo("UNRESOLVED");
        assertThat(outcome.getProfitLossPoints()).isNull();
    }

    @Test
    void marksCurrentDaySignalOpenAtLatestAvailableCandle() {
        TradingSignal signal = signal("BUY", 100, 95, 105, 9, 15);

        SignalOutcome outcome = service.evaluate(List.of(signal), List.of(
                candle(9, 15, 100, 100, 100),
                candle(9, 20, 103, 99, 102)), false).get(0);

        assertThat(outcome.getOutcome()).isEqualTo("OPEN");
        assertThat(outcome.getExitReason()).isEqualTo("AS_OF_LAST_CANDLE");
        assertThat(outcome.getProfitLossPoints()).isEqualTo(2.0);
    }

    @Test
    void closesAnUnhitSignalAtTheLastCandleOnCompletedDay() {
        TradingSignal signal = signal("SELL", 100, 105, 95, 9, 15);

        SignalOutcome outcome = service.evaluate(List.of(signal), List.of(
                candle(9, 15, 100, 100, 100),
                candle(15, 25, 102, 98, 99)), true).get(0);

        assertThat(outcome.getOutcome()).isEqualTo("PROFIT");
        assertThat(outcome.getExitReason()).isEqualTo("DAY_CLOSE");
        assertThat(outcome.getProfitLossPoints()).isEqualTo(1.0);
    }

    private TradingSignal signal(String direction,
                                 double entry,
                                 double stopLoss,
                                 double target,
                                 int hour,
                                 int minute) {
        TradingSignal signal = new TradingSignal();
        signal.setSignal(direction);
        signal.setLastClose(entry);
        signal.setStopLoss(stopLoss);
        signal.setTarget(target);
        signal.setCandleTime(LocalDateTime.of(2026, 10, 2, hour, minute));
        return signal;
    }

    private Candle candle(int hour,
                          int minute,
                          double high,
                          double low,
                          double close) {
        Candle candle = new Candle();
        candle.setTime(LocalDateTime.of(2026, 10, 2, hour, minute));
        candle.setHigh(high);
        candle.setLow(low);
        candle.setClose(close);
        return candle;
    }
}