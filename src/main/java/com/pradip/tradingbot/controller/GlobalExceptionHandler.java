package com.pradip.tradingbot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import com.pradip.tradingbot.dto.ApiResult;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResult<Void>> handleRuntimeException(RuntimeException ex) {

        HttpStatus status = "Please login first.".equals(ex.getMessage())
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(status)
                .body(ApiResult.failure(ex.getMessage()));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiResult<Void>> handleRestClientResponseException(
            RestClientResponseException ex) {

        String message = ex.getResponseBodyAsString();

        if (message == null || message.isBlank()) {
            message = ex.getMessage();
        }

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResult.failure(message));
    }
}
