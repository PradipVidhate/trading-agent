package com.pradip.tradingbot.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.dto.DailySignalEntry;
import com.pradip.tradingbot.dto.TradingSignal;

@Service
public class DailySignalHistoryService {

    private static final ZoneId MARKET_ZONE = ZoneId.of("Asia/Kolkata");

    private final Clock clock;
    private final List<DailySignalEntry> entries = new ArrayList<>();

    public DailySignalHistoryService() {
        this(Clock.system(MARKET_ZONE));
    }

    DailySignalHistoryService(Clock clock) {
        this.clock = clock;
    }

    public synchronized void record(TradingSignal tradingSignal) {
        LocalDate today = LocalDate.now(clock);
        entries.removeIf(entry -> !entry.getGeneratedAt().toLocalDate().equals(today));
        entries.add(0, new DailySignalEntry(LocalDateTime.now(clock), tradingSignal));
    }

    public synchronized List<DailySignalEntry> getToday() {
        LocalDate today = LocalDate.now(clock);
        entries.removeIf(entry -> !entry.getGeneratedAt().toLocalDate().equals(today));
        return List.copyOf(entries);
    }
}