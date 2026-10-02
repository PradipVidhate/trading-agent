package com.pradip.tradingbot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pradip.tradingbot.client.KiteClient;
import com.pradip.tradingbot.model.Candle;
import com.pradip.tradingbot.model.Instrument;

@Service
public class HistoricalDataService {

    private final InstrumentService instrumentService;
    private final KiteClient kiteClient;
    private final SessionService sessionService;

    public HistoricalDataService(InstrumentService instrumentService,
                                 KiteClient kiteClient,
                                 SessionService sessionService) {

        this.instrumentService = instrumentService;
        this.kiteClient = kiteClient;
        this.sessionService = sessionService;
    }

    public List<Candle> getHistoricalData(String symbol,
                                          String interval,
                                          int days) {

        LocalDateTime to = LocalDateTime.now();
        return getHistoricalData(symbol, interval, to.minusDays(days), to);
    }

    public List<Candle> getHistoricalData(String symbol,
                                          String interval,
                                          LocalDateTime from,
                                          LocalDateTime to) {

        if (!sessionService.isLoggedIn()) {
            throw new RuntimeException("Please login first.");
        }

        if (from.isAfter(to)) {
            throw new RuntimeException("Historical data start time must be before end time.");
        }

        Instrument instrument =
                instrumentService.findPreferredHistoricalInstrument(symbol);

        if (instrument == null) {
            throw new RuntimeException("Instrument not found : " + symbol);
        }

        JsonNode response =
                kiteClient.getHistoricalData(
                        instrument.getInstrumentToken(),
                        interval,
                        from,
                        to,
                        sessionService.getAccessToken());

        if (response == null) {
            throw new RuntimeException("No historical data response received from Zerodha.");
        }

        String status = response.path("status").asText();

        if (!"success".equalsIgnoreCase(status)) {
            String message = response.path("message").asText("Historical data request failed.");
            throw new RuntimeException(message);
        }

        JsonNode candles =
                response.path("data").path("candles");

        if (!candles.isArray()) {
            throw new RuntimeException("Historical data response did not contain candles.");
        }

        List<Candle> result = new ArrayList<>();

        for (JsonNode candle : candles) {

            Candle c = new Candle();

            c.setTime(parseCandleTime(candle.get(0).asText()));

            c.setOpen(candle.get(1).asDouble());

            c.setHigh(candle.get(2).asDouble());

            c.setLow(candle.get(3).asDouble());

            c.setClose(candle.get(4).asDouble());

            if (candle.size() > 5) {
                c.setVolume(candle.get(5).asLong());
            }

            result.add(c);
        }

        return result;
    }

    private LocalDateTime parseCandleTime(String time) {

        String dateTime = time.substring(0, 19);

        if (dateTime.contains("T")) {
            return LocalDateTime.parse(dateTime);
        }

        return LocalDateTime.parse(
                dateTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
