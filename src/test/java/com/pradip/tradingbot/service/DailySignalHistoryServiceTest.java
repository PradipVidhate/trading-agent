package com.pradip.tradingbot.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.pradip.tradingbot.dto.TradingSignal;

class DailySignalHistoryServiceTest {

    @Test
    void recordsSignalsWithMarketLocalTimestamp() {
        ZoneId marketZone = ZoneId.of("Asia/Kolkata");
        Clock clock = Clock.fixed(Instant.parse("2026-10-01T09:00:00Z"), marketZone);
        DailySignalHistoryService historyService = new DailySignalHistoryService(clock);
        TradingSignal signal = new TradingSignal();
        signal.setSymbol("NIFTY 50");

        historyService.record(signal);

        assertThat(historyService.getToday()).hasSize(1);
        assertThat(historyService.getToday().get(0).getGeneratedAt())
                .isEqualTo(LocalDateTime.of(2026, 10, 1, 14, 30));
        assertThat(historyService.getToday().get(0).getTradingSignal()).isSameAs(signal);
    }

    @Test
    void doesNotRecordNoTradeResults() {
        DailySignalHistoryService historyService = new DailySignalHistoryService();
        TradingSignal signal = new TradingSignal();
        signal.setSignal("NO_TRADE");

        historyService.record(signal);

        assertThat(historyService.getToday()).isEmpty();
    }
}