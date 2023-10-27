package org.greencloud.rulescontroller.rest;

import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_CLOUD_RULE_SET;
import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_RULE_SET;

import java.util.HashMap;
import java.util.Map;

import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.greencloud.rulescontroller.ruleset.defaultruleset.DefaultCloudRuleSet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class RuleSetRestApi {

	protected static Map<String, RuleSet> availableRuleSets;

	public static void main(String[] args) {
		SpringApplication.run(RuleSetRestApi.class, args);
	}

	/**
	 * Method starts REST controller that listens for new rule sets
	 */
	public static void startRulesControllerRest() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(RuleSetRestApi.class);
		builder.headless(false);
		builder.run();

		availableRuleSets = new HashMap<>();
		availableRuleSets.put(DEFAULT_CLOUD_RULE_SET, new DefaultCloudRuleSet());
	}

	/**
	 * Method starts REST controller that listens for new rule sets and adds its default rule set
	 */
	public static void startRulesControllerRest(final RuleSet ruleSet) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(RuleSetRestApi.class);
		builder.headless(false);
		builder.run();

		availableRuleSets = new HashMap<>();
		availableRuleSets.put(DEFAULT_RULE_SET, ruleSet);
	}

	public static Map<String, RuleSet> getAvailableRuleSets() {
		return availableRuleSets;
	}

}
