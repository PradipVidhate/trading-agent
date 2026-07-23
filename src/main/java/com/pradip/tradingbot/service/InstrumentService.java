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

    private static final String NIFTY_ALIAS = "NIFTY";
    private static final String NIFTY_50_ALIAS = "NIFTY50";
    private static final String NIFTY_INDEX_SYMBOL = "NIFTY 50";

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
    public Instrument findByTradingSymbol(String symbol) {

        return instruments.stream()
                .filter(i -> i.getTradingSymbol() != null)
                .filter(i -> i.getTradingSymbol()
                        .equalsIgnoreCase(symbol))
                .findFirst()
                .orElse(null);
    }

    public Instrument findByTradingSymbol(String symbol,
                                          String exchange,
                                          String segment) {

        return instruments.stream()
                .filter(i -> i.getTradingSymbol() != null)
                .filter(i -> i.getTradingSymbol().equalsIgnoreCase(symbol))
                .filter(i -> exchange == null
                        || exchange.equalsIgnoreCase(i.getExchange()))
                .filter(i -> segment == null
                        || segment.equalsIgnoreCase(i.getSegment()))
                .findFirst()
                .orElse(null);
    }

    public Instrument findPreferredHistoricalInstrument(String symbol) {

        if (isNiftyIndexSymbol(symbol)) {
            Instrument niftyIndex =
                    findByTradingSymbol(NIFTY_INDEX_SYMBOL, "NSE", "INDICES");

            if (niftyIndex != null) {
                return niftyIndex;
            }

            return findByTradingSymbol(NIFTY_INDEX_SYMBOL);
        }

        Instrument nseEquity =
                findByTradingSymbol(symbol, "NSE", "NSE");

        if (nseEquity != null) {
            return nseEquity;
        }

        Instrument bseEquity =
                findByTradingSymbol(symbol, "BSE", "BSE");

        if (bseEquity != null) {
            return bseEquity;
        }

        return findByTradingSymbol(symbol);
    }

    public boolean isNiftyIndexSymbol(String symbol) {

        if (symbol == null) {
            return false;
        }

        String normalized = symbol.trim().replace(" ", "").toUpperCase();

        return NIFTY_ALIAS.equals(normalized)
                || NIFTY_50_ALIAS.equals(normalized);
    }

    public String getNiftyIndexSymbol() {

        return NIFTY_INDEX_SYMBOL;
    }

    /**
     * Return all instruments
     */
    public List<Instrument> getAll() {
        return instruments;
    }

    public List<Instrument> searchBySymbol(String keyword) {

        return instruments.stream()
                .filter(i -> i.getTradingSymbol() != null
                        && i.getTradingSymbol().toUpperCase().contains(keyword.toUpperCase()))
                .limit(20)
                .toList();
    }
}
