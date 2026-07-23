package com.pradip.tradingbot.dto;

import java.util.ArrayList;
import java.util.List;

public class SignalScanResult {

    private List<TradingSignal> signals = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public List<TradingSignal> getSignals() {
        return signals;
    }

    public void setSignals(List<TradingSignal> signals) {
        this.signals = signals;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
