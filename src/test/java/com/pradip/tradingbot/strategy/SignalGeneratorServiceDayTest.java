package com.pradip.tradingbot.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pradip.tradingbot.dto.SignalOutcome;
import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.model.Candle;
import com.pradip.tradingbot.service.DailySignalHistoryService;
import com.pradip.tradingbot.service.HistoricalDataService;
import com.pradip.tradingbot.service.InstrumentService;

class SignalGeneratorServiceDayTest {

    @Test
        void emitsEachOptionSideOnlyOnceForTheSelectedDay() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        HistoricalDataService historicalDataService = mock(HistoricalDataService.class);
        InstrumentService instrumentService = mock(InstrumentService.class);
        SupportResistanceService supportResistanceService = spy(new SupportResistanceService());
        when(instrumentService.isNiftyIndexSymbol("NIFTY")).thenReturn(true);
        when(instrumentService.getNiftyIndexSymbol()).thenReturn("NIFTY 50");

        SupportResistanceResult levels = levels(90, 110, "SIDEWAYS", 200, 50);
        List<Candle> candles = List.of(
                candle(date.minusDays(1).atTime(10, 0), 100, 110, 90),
                candle(date.minusDays(1).atTime(15, 25), 100, 105, 95),
                candle(date.atTime(9, 15), 100, 101, 89),
                candle(date.atTime(9, 20), 90.1, 92, 90),
                candle(date.atTime(9, 25), 100, 101, 99),
                candle(date.atTime(9, 30), 90, 91, 89.9),
                candle(date.atTime(9, 35), 100, 111, 99),
                candle(date.atTime(9, 40), 109.9, 110, 109.5),
                candle(date.atTime(9, 45), 100, 101, 99),
                candle(date.atTime(9, 50), 110, 111, 109));
        when(historicalDataService.getHistoricalData(
                "NIFTY 50",
                "5minute",
                date.minusDays(10).atStartOfDay(),
                date.atTime(23, 59, 59)))
                .thenReturn(candles);
        doReturn(levels).when(supportResistanceService)
                .calculate(eq(candles), any(LocalDateTime.class));

        SignalGeneratorService service = new SignalGeneratorService(
                historicalDataService,
                instrumentService,
                supportResistanceService,
                new SignalOutcomeService(),
                new DailySignalHistoryService());

        List<SignalOutcome> signals = service.generateSignalsForDay("NIFTY", date);

        assertThat(signals).hasSize(2)
                .extracting(signal -> signal.getSignal().getSignal())
                .containsExactly("BUY_CE", "BUY_PE");
        assertThat(signals).extracting(signal -> signal.getSignal().getCandleTime())
                .containsExactly(date.atTime(9, 20), date.atTime(9, 40));
        verify(historicalDataService).getHistoricalData(
                "NIFTY 50",
                "5minute",
                date.minusDays(10).atStartOfDay(),
                date.atTime(23, 59, 59));
    }

    @Test
    void allowsCounterTrendCallOnlyAfterSustainedResistanceBreak() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        HistoricalDataService historicalDataService = mock(HistoricalDataService.class);
        InstrumentService instrumentService = mock(InstrumentService.class);
        SupportResistanceService supportResistanceService = spy(new SupportResistanceService());
        when(instrumentService.isNiftyIndexSymbol("NIFTY")).thenReturn(true);
        when(instrumentService.getNiftyIndexSymbol()).thenReturn("NIFTY 50");

        SupportResistanceResult levels = levels(99.5, 100.5, "BEARISH", 120, 80);
        List<Candle> candles = List.of(
                candle(date.minusDays(1).atTime(10, 0), 100, 120, 80),
                candleWithOpen(date.minusDays(1).atTime(15, 25), 100, 95, 110, 90),
                candle(date.atTime(9, 15), 100.5, 100.5, 100.4),
                candle(date.atTime(9, 20), 100.6, 100.65, 100.55),
                candle(date.atTime(9, 25), 100.6, 100.65, 100.55));
        when(historicalDataService.getHistoricalData(
                "NIFTY 50",
                "5minute",
                date.minusDays(10).atStartOfDay(),
                date.atTime(23, 59, 59)))
                .thenReturn(candles);
        doReturn(levels).when(supportResistanceService)
                .calculate(eq(candles), any(LocalDateTime.class));

        SignalGeneratorService service = new SignalGeneratorService(
                historicalDataService,
                instrumentService,
                supportResistanceService,
                new SignalOutcomeService(),
                new DailySignalHistoryService());

        List<SignalOutcome> signals = service.generateSignalsForDay("NIFTY", date);

        assertThat(signals).hasSize(2)
                .extracting(signal -> signal.getSignal().getSignal())
                .containsExactly("BUY_PE", "BUY_CE");
        assertThat(signals).extracting(signal -> signal.getSignal().getCandleTime())
                .containsExactly(date.atTime(9, 15), date.atTime(9, 25));
        assertThat(signals.get(0).getSignal().getPreviousDayTrend()).isEqualTo("BEARISH");
        assertThat(signals.get(0).getSignal().isTrendReversalConfirmed()).isFalse();
        assertThat(signals.get(1).getSignal().getCurrentTrend()).isEqualTo("BULLISH");
        assertThat(signals.get(1).getSignal().isTrendReversalConfirmed()).isTrue();
        verify(historicalDataService).getHistoricalData(
                "NIFTY 50",
                "5minute",
                date.minusDays(10).atStartOfDay(),
                date.atTime(23, 59, 59));
    }

    private Candle candle(LocalDateTime time, double close, double high, double low) {
        Candle candle = new Candle();
        candle.setTime(time);
        candle.setOpen(close);
        candle.setClose(close);
        candle.setHigh(high);
        candle.setLow(low);
        return candle;
    }

        private SupportResistanceResult levels(double support,
                                                                                   double resistance,
                                                                                   String trend,
                                                                                   double previousHigh,
                                                                                   double previousLow) {
                SupportResistanceResult levels = new SupportResistanceResult();
                levels.setSupport(support);
                levels.setResistance(resistance);
                levels.setPreviousDayTrend(trend);
                levels.setPreviousDayHigh(previousHigh);
                levels.setPreviousDayLow(previousLow);
                return levels;
        }

        private Candle candleWithOpen(LocalDateTime time,
                                                                  double open,
                                                                  double close,
                                                                  double high,
                                                                  double low) {
                Candle candle = candle(time, close, high, low);
                candle.setOpen(open);
                return candle;
        }
}