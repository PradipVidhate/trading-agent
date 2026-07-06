package com.pradip.tradingbot.service;

import org.springframework.stereotype.Service;

import com.pradip.tradingbot.client.KiteClient;
import com.pradip.tradingbot.dto.AccessTokenData;
import com.pradip.tradingbot.dto.ApiResponse;
import com.pradip.tradingbot.dto.UserProfile;
import com.pradip.tradingbot.session.SessionManager;

@Service
public class AuthService {

    private final KiteClient kiteClient;
    private final SessionManager sessionManager;

    public AuthService(KiteClient kiteClient,
                       SessionManager sessionManager) {
        this.kiteClient = kiteClient;
        this.sessionManager = sessionManager;
    }

    public String getLoginUrl() {
        return kiteClient.getLoginUrl();
    }

    public String authenticate(String requestToken) {

        ApiResponse<AccessTokenData> response =
                kiteClient.generateAccessToken(requestToken);

        AccessTokenData data = response.getData();

        sessionManager.setAccessToken(data.getAccessToken());
        sessionManager.setPublicToken(data.getPublicToken());

        return """
        		Login Successful

        		Welcome %s

        		Authentication completed successfully.
        		""".formatted(data.getUserName());
    }
    
    public UserProfile getProfile() {

        return kiteClient.getProfile().getData();

    }
    
}