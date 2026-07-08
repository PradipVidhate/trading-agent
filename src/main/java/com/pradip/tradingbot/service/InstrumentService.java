package com.pradip.tradingbot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.model.Instrument;

@Service
public class InstrumentService {

    private final List<Instrument> instruments = new ArrayList<>();

    public void save(List<Instrument> list) {

        instruments.clear();

        instruments.addAll(list);
    }

    public List<Instrument> getAll() {
        return instruments;
    }

    public Instrument findByTradingSymbol(String tradingSymbol) {

        return instruments.stream()
                .filter(i -> i.getTradingSymbol()
                        .equalsIgnoreCase(tradingSymbol))
                .findFirst()
                .orElse(null);
    }

    public int count() {
        return instruments.size();
    }

}