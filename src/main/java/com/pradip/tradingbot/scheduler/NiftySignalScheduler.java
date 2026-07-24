package com.pradip.tradingbot.scheduler;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pradip.tradingbot.dto.ScheduledSignalStatus;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.service.KiteAlertService;
import com.pradip.tradingbot.strategy.SignalGeneratorService;

@Component
public class NiftySignalScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(NiftySignalScheduler.class);

    private static final String SYMBOL = "NIFTY";
    private static final String INTERVAL = "5minute";
    private static final int DAYS = 5;

    private final SignalGeneratorService signalGeneratorService;
    private final KiteAlertService kiteAlertService;
    private final ScheduledSignalStatus status = new ScheduledSignalStatus();

    public NiftySignalScheduler(SignalGeneratorService signalGeneratorService,
                                KiteAlertService kiteAlertService) {

        this.signalGeneratorService = signalGeneratorService;
        this.kiteAlertService = kiteAlertService;
        this.status.setEnabled(true);
    }

    @Scheduled(cron = "0 15,20,25,30,35,40,45,50,55 9 * * MON-FRI", zone = "Asia/Kolkata")
    @Scheduled(cron = "0 0,5,10,15,20,25,30,35,40,45,50,55 10-14 * * MON-FRI", zone = "Asia/Kolkata")
    @Scheduled(cron = "0 0,5,10,15,20,25,30 15 * * MON-FRI", zone = "Asia/Kolkata")
    public void generateNiftySignal() {

        LocalDateTime runAt = LocalDateTime.now();

        try {
            TradingSignal signal =
                    signalGeneratorService.generateSignal(SYMBOL, INTERVAL, DAYS);

            status.setLastRunAt(runAt);
            status.setLastSignal(signal);
            status.setLastError(null);

            log.info("Scheduled NIFTY signal generated: {} at close {}",
                    signal.getSignal(),
                    signal.getLastClose());

            kiteAlertService.createNiftySupportResistanceAlerts(INTERVAL, DAYS);

        } catch (RuntimeException ex) {
            status.setLastRunAt(runAt);
            status.setLastError(ex.getMessage());

            log.warn("Scheduled NIFTY signal generation skipped: {}", ex.getMessage());
        }
    }

    public ScheduledSignalStatus getStatus() {

        return status;
    }
}
