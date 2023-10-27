package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ENABLE_SERVER;
import static com.gui.event.domain.EventTypeEnum.ENABLE_SERVER_EVENT;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_ENABLING_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import com.gui.agents.server.ServerNode;
import com.gui.event.AbstractEvent;

public class EnableServerRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(EnableServerRule.class);

	public EnableServerRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize default rule metadata
	 *
	 * @return rule description
	 */
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
						((AbstractEvent) facts.get(EVENT)).getEventTypeEnum().equals(ENABLE_SERVER_EVENT));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Enabling Server and informing CNA {}.", agentProps.getOwnerCloudNetworkAgent().getLocalName());
		agentProps.enable();
		agentProps.saveMonitoringData();

		final RuleSetFacts enablingFacts = new RuleSetFacts(controller.getLatestRuleSet().get());
		enablingFacts.put(RULE_TYPE, PROCESS_SERVER_ENABLING_RULE);
		agent.addBehaviour(InitiateRequest.create(agent, enablingFacts, PROCESS_SERVER_ENABLING_RULE, controller));
	}
}
