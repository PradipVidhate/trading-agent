package com.pradip.tradingbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessTokenData {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("public_token")
    private String publicToken;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_id")
    private String userId;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPublicToken() {
        return publicToken;
    }

    public void setPublicToken(String publicToken) {
        this.publicToken = publicToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}