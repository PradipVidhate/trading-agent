package com.pradip.tradingbot.service;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.client.KiteClient;
import com.pradip.tradingbot.dto.AccessTokenData;
import com.pradip.tradingbot.dto.ApiResponse;
import com.pradip.tradingbot.dto.LoginResult;
import com.pradip.tradingbot.session.SessionManager;

@Service
public class AuthService {

    private final KiteClient kiteClient;
    private final SessionService sessionService;
    private final SessionManager sessionManager;
    private final InstrumentService instrumentService;

    public AuthService(KiteClient kiteClient,
                       SessionService sessionService,
                       SessionManager sessionManager,
                       InstrumentService instrumentService) {

        this.kiteClient = kiteClient;
        this.sessionService = sessionService;
        this.sessionManager = sessionManager;
        this.instrumentService = instrumentService;
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

        if (data == null) {
            throw new RuntimeException("Zerodha login response did not include session data.");
        }

        sessionService.saveSession(
                data.getAccessToken(),
                data.getUserId(),
                data.getUserName());
        sessionManager.setAccessToken(data.getAccessToken());

        return data;
    }

    public LoginResult login(String requestToken) {

        AccessTokenData data = generateAccessToken(requestToken);

        try {
            LoginResult result = new LoginResult();

            result.setSession(data);
            result.setInstrumentCount(
                    instrumentService.downloadInstrumentMaster());

            return result;

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Login succeeded, but instrument download failed: "
                            + ex.getMessage(),
                    ex);
        }
    }

    /**
     * Logout
     */
    public void logout() {
        sessionService.clearSession();
        sessionManager.clear();
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
