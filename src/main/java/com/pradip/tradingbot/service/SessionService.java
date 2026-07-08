package com.pradip.tradingbot.service;

import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private String accessToken;
    private String userId;
    private String userName;

    public void saveSession(String accessToken,
                            String userId,
                            String userName) {

        this.accessToken = accessToken;
        this.userId = userId;
        this.userName = userName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isLoggedIn() {
        return accessToken != null && !accessToken.isBlank();
    }

    public void clearSession() {
        accessToken = null;
        userId = null;
        userName = null;
    }
}