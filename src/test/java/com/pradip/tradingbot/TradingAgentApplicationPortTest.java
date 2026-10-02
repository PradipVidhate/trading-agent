package com.pradip.tradingbot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class TradingAgentApplicationPortTest {

    @Test
    void recognizesAnAlreadyRunningTradingAgent() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] page = "<title>Trading Agent Dashboard</title>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, page.length);
            try (var response = exchange.getResponseBody()) {
                response.write(page);
            }
        });
        server.start();

        try {
            assertThat(TradingAgentApplication.isAlreadyRunning(server.getAddress().getPort())).isTrue();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void doesNotTreatAnotherPageAsTheTradingAgent() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] page = "<title>Other App</title>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, page.length);
            try (var response = exchange.getResponseBody()) {
                response.write(page);
            }
        });
        server.start();

        try {
            assertThat(TradingAgentApplication.isAlreadyRunning(server.getAddress().getPort())).isFalse();
        } finally {
            server.stop(0);
        }
    }
}