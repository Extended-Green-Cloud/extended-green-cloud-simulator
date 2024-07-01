package org.greencloud.agentsystem.strategies.rulesets.allocation.common.resources.centralmanager;

import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PARSE_SERVER_RESOURCES_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENTS;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGES;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.agent.RegionResources;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

public class ParseServerResourcesRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	public ParseServerResourcesRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PARSE_SERVER_RESOURCES_RULE,
				"parse RMA response",
				"method parses the Servers' resources received from RMA");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ACLMessage> messages = facts.get(MESSAGES);

		final List<RegionResources> regionResources = messages.stream()
				.map(rmaResponse -> readMessageContent(rmaResponse, RegionResources.class))
				.toList();
		final List<Map<String, Object>> serversResources = agentProps.getServerResourcesFromRMAsData(regionResources);
		final Map<String, List<String>> serversPerRMA = messages.stream()
				.collect(toMap(response -> response.getSender().getName(), this::getServersPerRMA));

		facts.put(AGENTS, serversPerRMA);
		facts.put(RESULT, serversResources);
	}

	private List<String> getServersPerRMA(final ACLMessage rmaResponse) {
		return readMessageContent(rmaResponse, RegionResources.class).getServersResources().keySet().stream().toList();
	}

	@Override
	public AgentRule copy() {
		return new ParseServerResourcesRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
