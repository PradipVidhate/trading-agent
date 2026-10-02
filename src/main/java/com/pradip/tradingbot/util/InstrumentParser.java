package com.pradip.tradingbot.util;

import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.pradip.tradingbot.model.Instrument;

public class InstrumentParser {

    public static List<Instrument> parse(Reader reader) throws Exception {

        List<Instrument> list = new ArrayList<>();

        CSVParser parser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader);

        for (CSVRecord record : parser) {

            Instrument instrument = new Instrument();

            instrument.setInstrumentToken(
                    Long.parseLong(record.get("instrument_token")));

            instrument.setExchangeToken(
                    record.get("exchange_token"));

            instrument.setTradingSymbol(
                    record.get("tradingsymbol"));

            instrument.setName(
                    record.get("name"));

            instrument.setExchange(
                    record.get("exchange"));

            instrument.setSegment(
                    record.get("segment"));

            instrument.setExpiry(
                    parseDate(record.get("expiry")));

            instrument.setStrike(
                    parseDouble(record.get("strike")));

            instrument.setLotSize(
                    parseInt(record.get("lot_size")));

            instrument.setInstrumentType(
                    record.get("instrument_type"));

            list.add(instrument);
        }

        return list;
    }

    private static LocalDate parseDate(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value);
    }

    private static double parseDouble(String value) {

        if (value == null || value.isBlank()) {
            return 0;
        }

        return Double.parseDouble(value);
    }

    private static int parseInt(String value) {

        if (value == null || value.isBlank()) {
            return 0;
        }

        return Integer.parseInt(value);
    }
}
