package com.pradip.tradingbot.scheduler;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pradip.tradingbot.dto.ScheduledSignalStatus;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.service.DailySignalHistoryService;
import com.pradip.tradingbot.service.KiteAlertService;
import com.pradip.tradingbot.strategy.SignalGeneratorService;

@Component
public class NiftySignalScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(NiftySignalScheduler.class);

    private static final String SYMBOL = "NIFTY";
    private static final String INTERVAL = "5minute";
    private static final int DAYS = 5;
    private static final ZoneId MARKET_ZONE = ZoneId.of("Asia/Kolkata");

    private final SignalGeneratorService signalGeneratorService;
    private final KiteAlertService kiteAlertService;
    private final DailySignalHistoryService dailySignalHistoryService;
    private final ScheduledSignalStatus status = new ScheduledSignalStatus();
    private String lastPublishedSignal;
    private LocalDate lastAlertSetupDate;

    public NiftySignalScheduler(SignalGeneratorService signalGeneratorService,
                                KiteAlertService kiteAlertService,
                                DailySignalHistoryService dailySignalHistoryService) {

        this.signalGeneratorService = signalGeneratorService;
        this.kiteAlertService = kiteAlertService;
        this.dailySignalHistoryService = dailySignalHistoryService;
        this.status.setEnabled(true);
    }

    @Scheduled(cron = "0 15,20,25,30,35,40,45,50,55 9 * * MON-FRI", zone = "Asia/Kolkata")
    @Scheduled(cron = "0 0,5,10,15,20,25,30,35,40,45,50,55 10-14 * * MON-FRI", zone = "Asia/Kolkata")
    @Scheduled(cron = "0 0,5,10,15,20,25,30 15 * * MON-FRI", zone = "Asia/Kolkata")
    public void generateNiftySignal() {

        LocalDateTime runAt = LocalDateTime.now();

        try {
            TradingSignal signal =
                    signalGeneratorService.generateScheduledSignal(SYMBOL, INTERVAL, DAYS);

            status.setLastRunAt(runAt);
            status.setLastError(null);
            refreshAlertsOncePerDay();

                if ("NO_TRADE".equals(signal.getSignal())) {
                lastPublishedSignal = null;
                return;
                }

                if (signal.getSignal().equals(lastPublishedSignal)) {
                log.debug("Suppressing repeated NIFTY {} signal at close {}",
                    signal.getSignal(), signal.getLastClose());
                return;
                }

                lastPublishedSignal = signal.getSignal();
                status.setLastSignal(signal);
                dailySignalHistoryService.record(signal);
                log.info("Scheduled NIFTY {} signal at close {}",
                    signal.getSignal(), signal.getLastClose());

        } catch (RuntimeException ex) {
            status.setLastRunAt(runAt);
            status.setLastError(ex.getMessage());

            log.warn("Scheduled NIFTY signal generation skipped: {}", ex.getMessage());
        }
    }

    private void refreshAlertsOncePerDay() {
        LocalDate today = LocalDate.now(MARKET_ZONE);
        if (today.equals(lastAlertSetupDate)) {
            return;
        }

        try {
            kiteAlertService.createNiftySupportResistanceAlerts(INTERVAL, DAYS);
            lastAlertSetupDate = today;
        } catch (RuntimeException ex) {
            log.warn("Scheduled NIFTY alert setup failed: {}", ex.getMessage());
        }
    }

    public ScheduledSignalStatus getStatus() {

        return status;
    }
}
