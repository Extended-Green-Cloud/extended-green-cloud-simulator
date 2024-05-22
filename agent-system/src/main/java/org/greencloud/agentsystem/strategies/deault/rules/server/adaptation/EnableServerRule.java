package org.greencloud.agentsystem.strategies.deault.rules.server.adaptation;

import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.ENABLE_SERVER;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.event.EventTypeEnum.ENABLE_SERVER_EVENT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SERVER_ENABLING_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.environment.domain.ExternalEvent;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class EnableServerRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(EnableServerRule.class);

	public EnableServerRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(ADAPTATION_REQUEST_RULE,
				"enable Server",
				"performing adaptation which enables Server");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return (nonNull(facts.get(ADAPTATION_TYPE)) && facts.get(ADAPTATION_TYPE).equals(ENABLE_SERVER)) ||
				(nonNull(facts.get(EVENT)) &&
						((ExternalEvent) facts.get(EVENT)).getEventType().equals(ENABLE_SERVER_EVENT));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Enabling Server and informing RMA {}.", agentProps.getOwnerRegionalManagerAgent().getLocalName());
		agentProps.enable();
		agentProps.saveMonitoringData();

		final RuleSetFacts enablingFacts = new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get());
		enablingFacts.put(RULE_TYPE, PROCESS_SERVER_ENABLING_RULE);
		agent.addBehaviour(InitiateRequest.create(agent, enablingFacts, PROCESS_SERVER_ENABLING_RULE, controller));
	}

	@Override
	public AgentRule copy() {
		return new EnableServerRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
