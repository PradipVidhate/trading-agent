package com.pradip.tradingbot.client;

import org.springframework.http.MediaType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.pradip.tradingbot.config.KiteProperties;
import com.pradip.tradingbot.dto.AccessTokenData;
import com.pradip.tradingbot.dto.ApiResponse;
import com.pradip.tradingbot.util.HashUtil;
import com.pradip.tradingbot.constant.ApiConstants;

import org.springframework.http.HttpHeaders;
import com.pradip.tradingbot.dto.UserProfile;
import com.pradip.tradingbot.session.SessionManager;

@Component
public class KiteClient {

    private final KiteProperties kiteProperties;
    private final RestClient restClient;
    private final SessionManager sessionManager;

    public KiteClient(KiteProperties kiteProperties,
                      RestClient restClient,
                      SessionManager sessionManager) {

        this.kiteProperties = kiteProperties;
        this.restClient = restClient;
        this.sessionManager = sessionManager;
    }
    
    public String getLoginUrl() {

        return UriComponentsBuilder
                .fromHttpUrl("https://kite.zerodha.com/connect/login")
                .queryParam("v", "3")
                .queryParam("api_key", kiteProperties.getApiKey())
                .build()
                .toUriString();
    }

    public ApiResponse<AccessTokenData> generateAccessToken(String requestToken) {

        String checksum = HashUtil.sha256(
                kiteProperties.getApiKey()
                + requestToken
                + kiteProperties.getApiSecret());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("api_key", kiteProperties.getApiKey());
        body.add("request_token", requestToken);
        body.add("checksum", checksum);

        return restClient.post()
        		.uri(ApiConstants.BASE_URL + ApiConstants.SESSION_TOKEN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<ApiResponse<AccessTokenData>>() {});
    }
    
    public ApiResponse<UserProfile> getProfile() {

        String authorization = "token "
                + kiteProperties.getApiKey()
                + ":"
                + sessionManager.getAccessToken();

        return restClient.get()
                .uri(ApiConstants.BASE_URL + ApiConstants.PROFILE)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<ApiResponse<UserProfile>>() {});
    }
    
    public String downloadInstrumentCsv(String accessToken) {

        return restClient.get()
                .uri("https://api.kite.trade/instruments")
                .header("Authorization", "token "
                        + kiteProperties.getApiKey()
                        + ":" + accessToken)
                .retrieve()
                .body(String.class);
    }
    
    public JsonNode getHistoricalData(long instrumentToken,
            String interval,
            LocalDateTime from,
            LocalDateTime to,
            String accessToken) {

DateTimeFormatter formatter =
DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

return restClient.get()

.uri(uriBuilder -> uriBuilder

.scheme("https")
.host("api.kite.trade")
.path("/instruments/historical/{token}/{interval}")

.queryParam("from", from.format(formatter))
.queryParam("to", to.format(formatter))
.queryParam("continuous", 0)
.queryParam("oi", 0)

.build(instrumentToken, interval))

.header(HttpHeaders.AUTHORIZATION,
"token "
      + kiteProperties.getApiKey()
      + ":"
      + accessToken)

.header("X-Kite-Version", "3")

.retrieve()

.body(JsonNode.class);
}
    
}