package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DISABLE_SERVER;
import static com.gui.event.domain.EventTypeEnum.DISABLE_SERVER_EVENT;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_DISABLING_RULE;
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

public class DisableServerRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(DisableServerRule.class);

	public DisableServerRule(final RulesController<ServerAgentProps, ServerNode> controller) {
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
				"disable Server",
				"performing adaptation which disables Server");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return (nonNull(facts.get(ADAPTATION_TYPE)) && facts.get(ADAPTATION_TYPE).equals(DISABLE_SERVER)) ||
				(nonNull(facts.get(EVENT)) &&
						((AbstractEvent) facts.get(EVENT)).getEventTypeEnum().equals(DISABLE_SERVER_EVENT));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Disabling Server and informing CNA {}.", agentProps.getOwnerCloudNetworkAgent().getLocalName());
		agentProps.disable();
		agentProps.saveMonitoringData();

		final RuleSetFacts disablingFacts = new RuleSetFacts(controller.getLatestRuleSet().get());
		disablingFacts.put(RULE_TYPE, PROCESS_SERVER_DISABLING_RULE);
		agent.addBehaviour(InitiateRequest.create(agent, disablingFacts, PROCESS_SERVER_DISABLING_RULE, controller));
	}
}
