package com.pradip.tradingbot.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class KiteAlertSetupResult {

    private SupportResistanceResult levels;
    private List<JsonNode> createdAlerts = new ArrayList<>();
    private List<JsonNode> updatedAlerts = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public SupportResistanceResult getLevels() {
        return levels;
    }

    public void setLevels(SupportResistanceResult levels) {
        this.levels = levels;
    }

    public List<JsonNode> getCreatedAlerts() {
        return createdAlerts;
    }

    public void setCreatedAlerts(List<JsonNode> createdAlerts) {
        this.createdAlerts = createdAlerts;
    }

    public List<JsonNode> getUpdatedAlerts() {
        return updatedAlerts;
    }

    public void setUpdatedAlerts(List<JsonNode> updatedAlerts) {
        this.updatedAlerts = updatedAlerts;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
