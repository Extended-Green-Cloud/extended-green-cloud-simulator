package org.greencloud.rulescontroller.rest;

import static java.lang.String.format;
import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_CLOUD_RULE_SET;
import static org.greencloud.commons.enums.rules.RuleSetType.DEFAULT_RULE_SET;
import static org.greencloud.commons.mapper.JsonMapper.getMapper;
import static org.greencloud.commons.utils.filereader.FileReader.readAllFiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.exception.InvalidScenarioException;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.rest.domain.RuleSetRest;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.greencloud.rulescontroller.ruleset.defaultruleset.DefaultCloudRuleSet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class RuleSetRestApi {

	protected static final String RULE_SETS_PATH = "rulesets";
	protected static Map<String, RuleSet> availableRuleSets;
	protected static List<AgentNode> agentNodes;

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
		agentNodes = new ArrayList<>();
		availableRuleSets.put(DEFAULT_CLOUD_RULE_SET, new DefaultCloudRuleSet());

		final List<File> ruleSetFiles = readAllFiles(RULE_SETS_PATH);
		ruleSetFiles.forEach(file -> {
			final RuleSetRest ruleSet = parseRuleSetStructure(file);
			final RuleSet newRuleSet = new RuleSet(ruleSet);
			getAvailableRuleSets().put(newRuleSet.getName(), newRuleSet);
		});
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
		agentNodes = new ArrayList<>();
	}

	public static void addAgentNode(final AgentNode newNode) {
		agentNodes.add(newNode);
	}

	public static Map<String, RuleSet> getAvailableRuleSets() {
		return availableRuleSets;
	}

	public static List<AgentNode> getAgentNodes() {
		return agentNodes;
	}

	private static RuleSetRest parseRuleSetStructure(final File ruleSetFile) {
		try {
			return getMapper().readValue(ruleSetFile, RuleSetRest.class);
		} catch (IOException e) {
			throw new InvalidScenarioException(
					format("Failed to parse rule set file \"%s\"", ruleSetFile), e);
		}
	}

}
