package com.pradip.tradingbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pradip.tradingbot.dto.ApiResult;
import com.pradip.tradingbot.model.Instrument;
import com.pradip.tradingbot.service.InstrumentService;
import java.util.List;
import com.pradip.tradingbot.model.Instrument;

@RestController
@RequestMapping("/market")
public class MarketController {

    private final InstrumentService instrumentService;

    public MarketController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping("/download")
    public ApiResult<Integer> download() throws Exception {

        int total = instrumentService.downloadInstrumentMaster();

        return ApiResult.success(
                "Instrument Master Downloaded Successfully",
                total);
    }

    @GetMapping("/count")
    public ApiResult<Integer> count() {

        return ApiResult.success(
                "Total Instruments",
                instrumentService.count());
    }

    @GetMapping("/search")
    public ApiResult<Instrument> search(
            @RequestParam String symbol) {

        Instrument instrument =
                instrumentService.findByTradingSymbol(symbol);

        if (instrument == null) {
            return ApiResult.failure("Instrument Not Found");
        }

        return ApiResult.success(
                "Instrument Found",
                instrument);
    }
    
    @GetMapping("/search-all")
    public ApiResult<List<Instrument>> searchAll(@RequestParam String symbol) {

        return ApiResult.success(
                "Matching Instruments",
                instrumentService.searchBySymbol(symbol));
    }

}