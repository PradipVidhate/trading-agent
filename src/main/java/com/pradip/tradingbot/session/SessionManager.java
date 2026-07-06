package com.pradip.tradingbot.session;

import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    private String accessToken;
    private String publicToken;

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

    public boolean isLoggedIn() {
        return accessToken != null && !accessToken.isBlank();
    }

    public void clear() {
        accessToken = null;
        publicToken = null;
    }
}