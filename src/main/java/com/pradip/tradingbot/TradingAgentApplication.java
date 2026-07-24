package com.pradip.tradingbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradingAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingAgentApplication.class, args);
	}

}
