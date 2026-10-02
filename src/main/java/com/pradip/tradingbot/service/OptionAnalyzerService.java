package com.pradip.tradingbot.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pradip.tradingbot.client.KiteClient;
import com.pradip.tradingbot.dto.OptionAnalysisResult;
import com.pradip.tradingbot.dto.OptionCandidate;
import com.pradip.tradingbot.dto.TradingSignal;
import com.pradip.tradingbot.model.Instrument;
import com.pradip.tradingbot.strategy.SignalGeneratorService;

@Service
public class OptionAnalyzerService {

    private static final ZoneId MARKET_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int SHORTLIST_SIZE = 7;

    private final SignalGeneratorService signalGeneratorService;
    private final InstrumentService instrumentService;
    private final KiteClient kiteClient;
    private final SessionService sessionService;

    public OptionAnalyzerService(SignalGeneratorService signalGeneratorService,
                                 InstrumentService instrumentService,
                                 KiteClient kiteClient,
                                 SessionService sessionService) {

        this.signalGeneratorService = signalGeneratorService;
        this.instrumentService = instrumentService;
        this.kiteClient = kiteClient;
        this.sessionService = sessionService;
    }

    public OptionAnalysisResult analyzeNifty(String interval,
                                             int days) {

        if (!sessionService.isLoggedIn()) {
            throw new RuntimeException("Please login first.");
        }

        TradingSignal signal =
                signalGeneratorService.generateSignal("NIFTY", interval, days);

        OptionAnalysisResult result = new OptionAnalysisResult();
        result.setSignal(signal);

        String optionType = optionTypeFor(signal.getSignal());
        if (optionType == null) {
            result.setMessage(
                    "No option candidate: the current NIFTY signal is NO_TRADE.");
            return result;
        }

        result.setOptionType(optionType);

        LocalDate today = LocalDate.now(MARKET_ZONE);
        List<Instrument> contracts =
                instrumentService.findNiftyOptions(optionType, today);

        if (contracts.isEmpty()) {
            result.setMessage("No active NIFTY " + optionType + " contracts are loaded.");
            return result;
        }

        LocalDate expiry = chooseExpiry(contracts, today);
        List<Instrument> shortlist = contracts.stream()
                .filter(contract -> expiry.equals(contract.getExpiry()))
                .sorted(Comparator.comparingDouble(contract ->
                        Math.abs(contract.getStrike() - signal.getLastClose())))
                .limit(SHORTLIST_SIZE)
                .toList();

        JsonNode quoteResponse = kiteClient.getQuotes(
                shortlist.stream().map(this::quoteKey).toList(),
                sessionService.getAccessToken());

        if (quoteResponse == null
                || !"success".equalsIgnoreCase(quoteResponse.path("status").asText())) {
            throw new RuntimeException("Kite did not return option quotes.");
        }

        JsonNode quotes = quoteResponse.path("data");
        List<OptionCandidate> candidates = new ArrayList<>(shortlist.stream()
                .map(contract -> buildCandidate(
                        contract,
                        quotes.path(quoteKey(contract)),
                        signal.getLastClose(),
                        today))
                .filter(candidate -> candidate != null)
                .toList());

        if (candidates.isEmpty()) {
            result.setMessage("Kite returned no tradable quotes for nearby NIFTY options.");
            return result;
        }

        scoreAndRank(candidates);
        result.setCandidates(candidates);
        result.setRecommendedCandidate(candidates.get(0));
        result.setMessage(
                "Candidates are ranked by moneyness, volume, open interest, and spread. "
                        + "This analysis does not place orders.");

        return result;
    }

    private String optionTypeFor(String signal) {

        if ("BUY".equals(signal)) {
            return "CE";
        }

        if ("SELL".equals(signal)) {
            return "PE";
        }

        return null;
    }

    private LocalDate chooseExpiry(List<Instrument> contracts,
                                   LocalDate today) {

        LocalDate preferredDate = today.plusDays(2);

        return contracts.stream()
                .map(Instrument::getExpiry)
                .filter(expiry -> !expiry.isBefore(preferredDate))
                .findFirst()
                .orElse(contracts.get(0).getExpiry());
    }

    private OptionCandidate buildCandidate(Instrument contract,
                                           JsonNode quote,
                                           double underlyingPrice,
                                           LocalDate today) {

        if (quote.isMissingNode() || quote.path("last_price").asDouble() <= 0) {
            return null;
        }

        OptionCandidate candidate = new OptionCandidate();
        candidate.setTradingSymbol(contract.getTradingSymbol());
        candidate.setOptionType(contract.getInstrumentType());
        candidate.setExpiry(contract.getExpiry());
        candidate.setDaysToExpiry((int) ChronoUnit.DAYS.between(today, contract.getExpiry()));
        candidate.setStrike(contract.getStrike());
        candidate.setLotSize(contract.getLotSize());
        candidate.setLastPrice(round(quote.path("last_price").asDouble()));
        candidate.setVolume(quote.path("volume").asLong());
        candidate.setOpenInterest(openInterest(quote));

        double bestBid = depthPrice(quote.path("depth").path("buy"));
        double bestAsk = depthPrice(quote.path("depth").path("sell"));
        candidate.setBestBid(round(bestBid));
        candidate.setBestAsk(round(bestAsk));
        candidate.setMoneynessPercentage(round(
                Math.abs(contract.getStrike() - underlyingPrice) / underlyingPrice * 100));
        candidate.setSpreadPercentage(round(spreadPercentage(
                bestBid,
                bestAsk,
                candidate.getLastPrice())));

        return candidate;
    }

    private void scoreAndRank(List<OptionCandidate> candidates) {

        double maxVolume = candidates.stream()
                .mapToDouble(OptionCandidate::getVolume)
                .max()
                .orElse(0);
        double maxOpenInterest = candidates.stream()
                .mapToDouble(OptionCandidate::getOpenInterest)
                .max()
                .orElse(0);

        candidates.forEach(candidate -> candidate.setScore(round(
                moneynessScore(candidate)
                        + normalizedScore(candidate.getVolume(), maxVolume, 25)
                        + normalizedScore(candidate.getOpenInterest(), maxOpenInterest, 20)
                        + spreadScore(candidate))));

        candidates.sort(Comparator.comparingDouble(OptionCandidate::getScore).reversed()
                .thenComparingDouble(OptionCandidate::getMoneynessPercentage));

        for (int index = 0; index < candidates.size(); index++) {
            candidates.get(index).setRank(index + 1);
        }
    }

    private double moneynessScore(OptionCandidate candidate) {

        return Math.max(0, 35 - candidate.getMoneynessPercentage() * 10);
    }

    private double normalizedScore(long value,
                                   double maximum,
                                   double weight) {

        if (value <= 0 || maximum <= 0) {
            return 0;
        }

        return Math.log1p(value) / Math.log1p(maximum) * weight;
    }

    private double spreadScore(OptionCandidate candidate) {

        if (candidate.getBestBid() <= 0 || candidate.getBestAsk() <= 0) {
            return 0;
        }

        return Math.max(0, 20 - candidate.getSpreadPercentage() * 10);
    }

    private long openInterest(JsonNode quote) {

        long oi = quote.path("oi").asLong();
        return oi > 0 ? oi : quote.path("open_interest").asLong();
    }

    private double depthPrice(JsonNode depth) {

        return depth.isArray() && depth.size() > 0
                ? depth.get(0).path("price").asDouble()
                : 0;
    }

    private double spreadPercentage(double bid,
                                    double ask,
                                    double lastPrice) {

        if (bid <= 0 || ask <= 0 || ask < bid || lastPrice <= 0) {
            return 100;
        }

        return (ask - bid) / lastPrice * 100;
    }

    private String quoteKey(Instrument contract) {

        return contract.getExchange() + ":" + contract.getTradingSymbol();
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}
