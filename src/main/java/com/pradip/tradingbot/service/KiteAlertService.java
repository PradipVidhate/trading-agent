package com.pradip.tradingbot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.client.KiteClient;
import com.pradip.tradingbot.dto.ApiResponse;
import com.pradip.tradingbot.dto.KiteAlertSetupResult;
import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.model.Candle;
import com.pradip.tradingbot.strategy.SignalGeneratorService;
import com.pradip.tradingbot.strategy.SupportResistanceService;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class KiteAlertService {

    private static final String BUY_ALERT_NAME =
            "TRADING_AGENT_NIFTY_BUY_SUPPORT";
    private static final String SELL_ALERT_NAME =
            "TRADING_AGENT_NIFTY_SELL_RESISTANCE";
    private static final String LEGACY_BUY_ALERT_PREFIX =
            "NIFTY BUY zone <=";
    private static final String LEGACY_SELL_ALERT_PREFIX =
            "NIFTY SELL zone >=";

    private final KiteClient kiteClient;
    private final SessionService sessionService;
    private final HistoricalDataService historicalDataService;
    private final SupportResistanceService supportResistanceService;
    private final SignalGeneratorService signalGeneratorService;

    public KiteAlertService(KiteClient kiteClient,
                            SessionService sessionService,
                            HistoricalDataService historicalDataService,
                            SupportResistanceService supportResistanceService,
                            SignalGeneratorService signalGeneratorService) {

        this.kiteClient = kiteClient;
        this.sessionService = sessionService;
        this.historicalDataService = historicalDataService;
        this.supportResistanceService = supportResistanceService;
        this.signalGeneratorService = signalGeneratorService;
    }

    public KiteAlertSetupResult createNiftySupportResistanceAlerts(String interval,
                                                                   int days) {

        if (!sessionService.isLoggedIn()) {
            throw new RuntimeException("Please login first.");
        }

        String symbol = signalGeneratorService.requireNiftyIndexSymbol("NIFTY");

        List<Candle> candles =
                historicalDataService.getHistoricalData(symbol, interval, days);

        SupportResistanceResult levels =
                supportResistanceService.calculate(candles);

        KiteAlertSetupResult result = new KiteAlertSetupResult();

        result.setLevels(levels);

        JsonNode existingAlerts = getExistingAlerts();

        upsertAlertSafely(
                result,
                existingAlerts,
                BUY_ALERT_NAME,
                LEGACY_BUY_ALERT_PREFIX,
                "<=",
                levels.getSupport());

        upsertAlertSafely(
                result,
                existingAlerts,
                SELL_ALERT_NAME,
                LEGACY_SELL_ALERT_PREFIX,
                ">=",
                levels.getResistance());

        return result;
    }

    private void upsertAlertSafely(KiteAlertSetupResult result,
                                   JsonNode existingAlerts,
                                   String name,
                                   String legacyNamePrefix,
                                   String operator,
                                   double level) {

        try {
            JsonNode existingAlert =
                    findExistingAlert(existingAlerts, name, legacyNamePrefix);

            if (existingAlert == null) {
                result.getCreatedAlerts().add(
                        createAlert(name, operator, level));
                return;
            }

            String uuid = existingAlert.path("uuid").asText();

            result.getUpdatedAlerts().add(
                    updateAlert(uuid, name, operator, level));

        } catch (RuntimeException ex) {
            result.getErrors().add(name + " : " + ex.getMessage());
        }
    }

    private JsonNode getExistingAlerts() {

        ApiResponse<JsonNode> response =
                kiteClient.getAlerts(sessionService.getAccessToken());

        if (response == null) {
            throw new RuntimeException("No response received from Kite alerts API.");
        }

        if (!"success".equalsIgnoreCase(response.getStatus())) {
            throw new RuntimeException(response.getMessage());
        }

        return response.getData();
    }

    private JsonNode findExistingAlert(JsonNode alerts,
                                       String name,
                                       String legacyNamePrefix) {

        if (alerts == null || !alerts.isArray()) {
            return null;
        }

        for (JsonNode alert : alerts) {
            String alertName = alert.path("name").asText();
            String status = alert.path("status").asText();

            if ("deleted".equalsIgnoreCase(status)) {
                continue;
            }

            if (name.equals(alertName)
                    || alertName.startsWith(legacyNamePrefix)) {
                return alert;
            }
        }

        return null;
    }

    private JsonNode createAlert(String name,
                                 String operator,
                                 double level) {

        ApiResponse<JsonNode> response =
                kiteClient.createSimpleAlert(
                        sessionService.getAccessToken(),
                        name,
                        operator,
                        level);

        if (response == null) {
            throw new RuntimeException("No response received from Kite alerts API.");
        }

        if (!"success".equalsIgnoreCase(response.getStatus())) {
            throw new RuntimeException(response.getMessage());
        }

        return response.getData();
    }

    private JsonNode updateAlert(String uuid,
                                 String name,
                                 String operator,
                                 double level) {

        ApiResponse<JsonNode> response =
                kiteClient.updateSimpleAlert(
                        sessionService.getAccessToken(),
                        uuid,
                        name,
                        operator,
                        level);

        if (response == null) {
            throw new RuntimeException("No response received from Kite alerts API.");
        }

        if (!"success".equalsIgnoreCase(response.getStatus())) {
            throw new RuntimeException(response.getMessage());
        }

        return response.getData();
    }
}
