package com.pradip.tradingbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pradip.tradingbot.service.InstrumentService;

@RestController
public class MarketController {

    private final InstrumentService instrumentService;

    public MarketController(InstrumentService instrumentService) {

        this.instrumentService = instrumentService;
    }

    @GetMapping("/market/count")
    public String count() {

        return "Total Instruments : "
                + instrumentService.count();
    }

}