package com.pradip.tradingbot.dto;

public class TokenRequest {

    private String requestToken;

    public TokenRequest() {
    }

    public TokenRequest(String requestToken) {
        this.requestToken = requestToken;
    }

    public String getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }
}