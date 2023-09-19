package org.greencloud.rulescontroller.rest;

import java.util.HashMap;
import java.util.Map;

import org.greencloud.rulescontroller.strategy.Strategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class StrategyRestApi {

	protected static Map<String, Strategy> availableStrategies;

	public static void main(String[] args) {
		SpringApplication.run(StrategyRestApi.class, args);
	}

	/**
	 * Method starts REST controller that listens for new strategies
	 */
	public static void startRulesControllerRest() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(StrategyRestApi.class);
		builder.headless(false);
		builder.run();
		availableStrategies = new HashMap<>();
	}

	public static Map<String, Strategy> getAvailableStrategies() {
		return availableStrategies;
	}

}
