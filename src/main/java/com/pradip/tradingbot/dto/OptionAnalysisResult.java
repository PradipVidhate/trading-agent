package com.pradip.tradingbot.dto;

import java.util.ArrayList;
import java.util.List;

public class OptionAnalysisResult {

    private TradingSignal signal;
    private String optionType;
    private String message;
    private OptionCandidate recommendedCandidate;
    private List<OptionCandidate> candidates = new ArrayList<>();

    public TradingSignal getSignal() { return signal; }
    public void setSignal(TradingSignal signal) { this.signal = signal; }
    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public OptionCandidate getRecommendedCandidate() { return recommendedCandidate; }
    public void setRecommendedCandidate(OptionCandidate recommendedCandidate) {
        this.recommendedCandidate = recommendedCandidate;
    }
    public List<OptionCandidate> getCandidates() { return candidates; }
    public void setCandidates(List<OptionCandidate> candidates) { this.candidates = candidates; }
}
