package com.pradip.tradingbot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradingAgentApplication {

	public static void main(String[] args) {
		int port = configuredPort(args);
		if (isAlreadyRunning(port)) {
			System.out.println("Trading Agent is already running at http://localhost:" + port
					+ "; keeping the existing instance.");
			return;
		}

		SpringApplication.run(TradingAgentApplication.class, args);
	}

	static boolean isAlreadyRunning(int port) {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) URI.create("http://127.0.0.1:" + port + "/")
					.toURL().openConnection();
			connection.setConnectTimeout(500);
			connection.setReadTimeout(500);
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return false;
			}

			try (var response = connection.getInputStream()) {
				String page = new String(response.readAllBytes(), StandardCharsets.UTF_8);
				return page.contains("<title>Trading Agent Dashboard</title>");
			}
		} catch (IOException | IllegalArgumentException ex) {
			return false;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private static int configuredPort(String[] args) {
		int port = 8081;
		String systemPort = System.getProperty("server.port");
		String environmentPort = System.getenv("SERVER_PORT");
		String configuredPort = systemPort != null ? systemPort : environmentPort;
		if (configuredPort != null) {
			port = parsePort(configuredPort, port);
		}

		for (String argument : args) {
			if (argument.startsWith("--server.port=")) {
				port = parsePort(argument.substring("--server.port=".length()), port);
			}
		}
		return port;
	}

	private static int parsePort(String value, int fallback) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			return fallback;
		}
	}

}
