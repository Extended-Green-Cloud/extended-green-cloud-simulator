package org.greencloud.rulescontroller.rest;

import static org.greencloud.commons.enums.strategy.StrategyType.DEFAULT_CLOUD_STRATEGY;
import static org.greencloud.commons.enums.strategy.StrategyType.DEFAULT_STRATEGY;

import java.util.HashMap;
import java.util.Map;

import org.greencloud.rulescontroller.strategy.Strategy;
import org.greencloud.rulescontroller.strategy.defaultstrategy.DefaultCloudStrategy;
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
		availableStrategies.put(DEFAULT_CLOUD_STRATEGY.name(), new DefaultCloudStrategy());
	}

	/**
	 * Method starts REST controller that listens for new strategies and adds its default strategy
	 */
	public static void startRulesControllerRest(final Strategy strategy) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(StrategyRestApi.class);
		builder.headless(false);
		builder.run();

		availableStrategies = new HashMap<>();
		availableStrategies.put(DEFAULT_STRATEGY.name(), strategy);
	}

	public static Map<String, Strategy> getAvailableStrategies() {
		return availableStrategies;
	}

}
