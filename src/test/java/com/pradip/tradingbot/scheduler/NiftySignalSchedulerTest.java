package com.pradip.tradingbot.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.service.DailySignalHistoryService;
import com.pradip.tradingbot.service.KiteAlertService;
import com.pradip.tradingbot.strategy.SignalGeneratorService;

class NiftySignalSchedulerTest {

    @Test
    void recordsActionableSignalOncePerVisitToSupportOrResistanceZone() {
        SignalGeneratorService signalGeneratorService = mock(SignalGeneratorService.class);
        KiteAlertService kiteAlertService = mock(KiteAlertService.class);
        DailySignalHistoryService historyService = new DailySignalHistoryService();
        TradingSignal buy = signal("BUY");
        TradingSignal noTrade = signal("NO_TRADE");
        when(signalGeneratorService.generateScheduledSignal("NIFTY", "5minute", 5))
                .thenReturn(buy, buy, noTrade, buy);

        NiftySignalScheduler scheduler = new NiftySignalScheduler(
                signalGeneratorService, kiteAlertService, historyService);

        scheduler.generateNiftySignal();
        scheduler.generateNiftySignal();
        scheduler.generateNiftySignal();
        scheduler.generateNiftySignal();

        assertThat(historyService.getToday())
                .extracting(entry -> entry.getTradingSignal().getSignal())
                .containsExactly("BUY", "BUY");
        assertThat(scheduler.getStatus().getLastSignal()).isSameAs(buy);
        verify(signalGeneratorService, org.mockito.Mockito.times(4))
                .generateScheduledSignal("NIFTY", "5minute", 5);
        verify(kiteAlertService, org.mockito.Mockito.times(1))
                .createNiftySupportResistanceAlerts("5minute", 5);
    }

    private TradingSignal signal(String direction) {
        TradingSignal signal = new TradingSignal();
        signal.setSignal(direction);
        signal.setLastClose(100);
        return signal;
    }
}