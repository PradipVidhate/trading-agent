package com.pradip.tradingbot.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pradip.tradingbot.model.Candle;
import com.pradip.tradingbot.service.HistoricalDataService;
import com.pradip.tradingbot.dto.ApiResponse;

@RestController
public class HistoryController {

    private final HistoricalDataService historicalDataService;

    public HistoryController(HistoricalDataService historicalDataService) {
        this.historicalDataService = historicalDataService;
    }

    @GetMapping("/market/history")
    public ApiResponse<List<Candle>> history(

            @RequestParam String symbol,

            @RequestParam(defaultValue = "5minute")
            String interval,

            @RequestParam(defaultValue = "5")
            int days) {

        List<Candle> candles =
                historicalDataService.getHistoricalData(
                        symbol,
                        interval,
                        days);

        return ApiResponse.success(
                "Historical Data",
                candles);
    }
}