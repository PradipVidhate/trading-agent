package com.pradip.tradingbot.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.client.KiteClient;
import com.pradip.tradingbot.model.Instrument;
import com.pradip.tradingbot.util.InstrumentParser;

@Service
public class InstrumentService {

    private final KiteClient kiteClient;
    private final SessionService sessionService;

    // In-memory cache
    private final List<Instrument> instruments = new ArrayList<>();

    public InstrumentService(KiteClient kiteClient,
                             SessionService sessionService) {

        this.kiteClient = kiteClient;
        this.sessionService = sessionService;
    }

    /**
     * Download instrument master from Zerodha
     */
    public int downloadInstrumentMaster() throws Exception {

        if (!sessionService.isLoggedIn()) {
            throw new RuntimeException("Please login first.");
        }

        String csv = kiteClient.downloadInstrumentCsv(
                sessionService.getAccessToken());

        List<Instrument> downloaded =
                InstrumentParser.parse(new StringReader(csv));

        instruments.clear();
        instruments.addAll(downloaded);

        return instruments.size();
    }

    /**
     * Total instruments loaded
     */
    public int count() {
        return instruments.size();
    }

    /**
     * Search by trading symbol
     */
    public Instrument findByTradingSymbol(String tradingSymbol) {

        return instruments.stream()
                .filter(i -> i.getTradingSymbol()
                        .equalsIgnoreCase(tradingSymbol))
                .findFirst()
                .orElse(null);
    }

    /**
     * Return all instruments
     */
    public List<Instrument> getAll() {
        return instruments;
    }

}