package com.pradip.tradingbot.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pradip.tradingbot.dto.ApiResponse;
import com.pradip.tradingbot.dto.ScheduledSignalStatus;
import com.pradip.tradingbot.dto.SignalScanResult;
import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.model.Candle;
import com.pradip.tradingbot.scheduler.NiftySignalScheduler;
import com.pradip.tradingbot.service.HistoricalDataService;
import com.pradip.tradingbot.strategy.SignalGeneratorService;
import com.pradip.tradingbot.strategy.SupportResistanceService;

@RestController
@RequestMapping("/strategy")
public class StrategyController {

    private final HistoricalDataService historicalDataService;
    private final SupportResistanceService supportResistanceService;
    private final SignalGeneratorService signalGeneratorService;
    private final NiftySignalScheduler niftySignalScheduler;

    public StrategyController(HistoricalDataService historicalDataService,
                              SupportResistanceService supportResistanceService,
                              SignalGeneratorService signalGeneratorService,
                              NiftySignalScheduler niftySignalScheduler) {

        this.historicalDataService = historicalDataService;
        this.supportResistanceService = supportResistanceService;
        this.signalGeneratorService = signalGeneratorService;
        this.niftySignalScheduler = niftySignalScheduler;
    }

    @GetMapping("/levels")
    public ApiResponse<SupportResistanceResult> levels(
            @RequestParam(defaultValue = "NIFTY") String symbol,
            @RequestParam(defaultValue = "5minute") String interval,
            @RequestParam(defaultValue = "5") int days) {

        String indexSymbol =
                signalGeneratorService.requireNiftyIndexSymbol(symbol);

        List<Candle> candles =
                historicalDataService.getHistoricalData(indexSymbol, interval, days);

        SupportResistanceResult result =
                supportResistanceService.calculate(candles);

        return ApiResponse.success("Support and resistance levels", result);
    }

    @GetMapping("/signal")
    public ApiResponse<TradingSignal> signal(
            @RequestParam(defaultValue = "NIFTY") String symbol,
            @RequestParam(defaultValue = "5minute") String interval,
            @RequestParam(defaultValue = "5") int days) {

        TradingSignal signal =
                signalGeneratorService.generateSignal(symbol, interval, days);

        return ApiResponse.success("Trading signal", signal);
    }

    @GetMapping("/signals")
    public ApiResponse<SignalScanResult> signals(
            @RequestParam(defaultValue = "NIFTY") String symbols,
            @RequestParam(defaultValue = "5minute") String interval,
            @RequestParam(defaultValue = "5") int days) {

        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(symbol -> !symbol.isBlank())
                .distinct()
                .toList();

        if (symbolList.isEmpty()) {
            throw new RuntimeException("At least one symbol is required.");
        }

        SignalScanResult result =
                signalGeneratorService.scanSignals(symbolList, interval, days);

        return ApiResponse.success("Trading signals", result);
    }

    @GetMapping("/scheduled-signal")
    public ApiResponse<ScheduledSignalStatus> scheduledSignal() {

        return ApiResponse.success(
                "Latest scheduled NIFTY signal",
                niftySignalScheduler.getStatus());
    }
}
