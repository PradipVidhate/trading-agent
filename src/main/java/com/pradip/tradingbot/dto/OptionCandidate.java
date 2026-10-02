package com.pradip.tradingbot.dto;

import java.time.LocalDate;

public class OptionCandidate {

    private int rank;
    private String tradingSymbol;
    private String optionType;
    private LocalDate expiry;
    private int daysToExpiry;
    private double strike;
    private int lotSize;
    private double lastPrice;
    private long volume;
    private long openInterest;
    private double bestBid;
    private double bestAsk;
    private double spreadPercentage;
    private double moneynessPercentage;
    private double score;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public String getTradingSymbol() { return tradingSymbol; }
    public void setTradingSymbol(String tradingSymbol) { this.tradingSymbol = tradingSymbol; }
    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }
    public LocalDate getExpiry() { return expiry; }
    public void setExpiry(LocalDate expiry) { this.expiry = expiry; }
    public int getDaysToExpiry() { return daysToExpiry; }
    public void setDaysToExpiry(int daysToExpiry) { this.daysToExpiry = daysToExpiry; }
    public double getStrike() { return strike; }
    public void setStrike(double strike) { this.strike = strike; }
    public int getLotSize() { return lotSize; }
    public void setLotSize(int lotSize) { this.lotSize = lotSize; }
    public double getLastPrice() { return lastPrice; }
    public void setLastPrice(double lastPrice) { this.lastPrice = lastPrice; }
    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }
    public long getOpenInterest() { return openInterest; }
    public void setOpenInterest(long openInterest) { this.openInterest = openInterest; }
    public double getBestBid() { return bestBid; }
    public void setBestBid(double bestBid) { this.bestBid = bestBid; }
    public double getBestAsk() { return bestAsk; }
    public void setBestAsk(double bestAsk) { this.bestAsk = bestAsk; }
    public double getSpreadPercentage() { return spreadPercentage; }
    public void setSpreadPercentage(double spreadPercentage) { this.spreadPercentage = spreadPercentage; }
    public double getMoneynessPercentage() { return moneynessPercentage; }
    public void setMoneynessPercentage(double moneynessPercentage) { this.moneynessPercentage = moneynessPercentage; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
