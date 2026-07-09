package com.pradip.tradingbot.util;

import java.io.Reader;
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

            list.add(instrument);
        }

        return list;
    }
}