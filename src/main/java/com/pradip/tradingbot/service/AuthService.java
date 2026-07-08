package com.pradip.tradingbot.service;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.client.KiteClient;
import com.pradip.tradingbot.dto.AccessTokenData;
import com.pradip.tradingbot.dto.ApiResponse;

@Service
public class AuthService {

    private final KiteClient kiteClient;
    private final SessionService sessionService;

    public AuthService(KiteClient kiteClient,
                       SessionService sessionService) {

        this.kiteClient = kiteClient;
        this.sessionService = sessionService;
    }

    /**
     * Returns Zerodha Login URL
     */
    public String getLoginUrl() {
        return kiteClient.getLoginUrl();
    }

    /**
     * Exchanges request_token for access_token
     */
    public AccessTokenData generateAccessToken(String requestToken) {

        ApiResponse<AccessTokenData> response =
                kiteClient.generateAccessToken(requestToken);

        if (response == null) {
            throw new RuntimeException("No response received from Zerodha.");
        }

        if (!"success".equalsIgnoreCase(response.getStatus())) {
            throw new RuntimeException(response.getMessage());
        }

        AccessTokenData data = response.getData();

        sessionService.saveSession(
                data.getAccessToken(),
                data.getUserId(),
                data.getUserName());

        return data;
    }

    /**
     * Logout
     */
    public void logout() {
        sessionService.clearSession();
    }

    /**
     * Returns current logged in user
     */
    public AccessTokenData getCurrentSession() {

        if (!sessionService.isLoggedIn()) {
            return null;
        }

        AccessTokenData data = new AccessTokenData();

        data.setAccessToken(sessionService.getAccessToken());
        data.setUserId(sessionService.getUserId());
        data.setUserName(sessionService.getUserName());

        return data;
    }

    /**
     * Check login status
     */
    public boolean isLoggedIn() {
        return sessionService.isLoggedIn();
    }
}