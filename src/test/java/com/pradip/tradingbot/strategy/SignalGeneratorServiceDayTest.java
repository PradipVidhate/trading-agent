package com.pradip.tradingbot.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pradip.tradingbot.dto.SignalOutcome;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.model.Candle;
import com.pradip.tradingbot.service.DailySignalHistoryService;
import com.pradip.tradingbot.service.HistoricalDataService;
import com.pradip.tradingbot.service.InstrumentService;

class SignalGeneratorServiceDayTest {

    @Test
        void emitsOneSignalPerContinuousSupportOrResistanceVisit() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        HistoricalDataService historicalDataService = mock(HistoricalDataService.class);
        InstrumentService instrumentService = mock(InstrumentService.class);
        when(instrumentService.isNiftyIndexSymbol("NIFTY")).thenReturn(true);
        when(instrumentService.getNiftyIndexSymbol()).thenReturn("NIFTY 50");

        List<Candle> candles = List.of(
                candle(date.minusDays(1).atTime(10, 0), 100, 110, 90),
                candle(date.minusDays(1).atTime(15, 25), 100, 105, 95),
                candle(date.atTime(9, 15), 90, 92, 89),
                candle(date.atTime(9, 20), 90.1, 92, 90),
                candle(date.atTime(9, 25), 100, 101, 99),
                candle(date.atTime(9, 30), 90, 91, 89.9),
                candle(date.atTime(9, 35), 110, 111, 108),
                candle(date.atTime(9, 40), 109.9, 110, 109.5),
                candle(date.atTime(9, 45), 100, 101, 99),
                candle(date.atTime(9, 50), 110, 111, 109));
        when(historicalDataService.getHistoricalData(
                "NIFTY 50",
                "5minute",
                date.minusDays(10).atStartOfDay(),
                date.atTime(23, 59, 59)))
                .thenReturn(candles);

        SignalGeneratorService service = new SignalGeneratorService(
                historicalDataService,
                instrumentService,
                new SupportResistanceService(),
                new SignalOutcomeService(),
                new DailySignalHistoryService());

        List<SignalOutcome> signals = service.generateSignalsForDay("NIFTY", date);

        assertThat(signals).hasSize(4)
                .extracting(signal -> signal.getSignal().getSignal())
                .containsExactly("BUY", "BUY", "SELL", "SELL");
        assertThat(signals).extracting(signal -> signal.getSignal().getCandleTime())
                .containsExactly(date.atTime(9, 15), date.atTime(9, 30),
                        date.atTime(9, 35), date.atTime(9, 50));
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
}