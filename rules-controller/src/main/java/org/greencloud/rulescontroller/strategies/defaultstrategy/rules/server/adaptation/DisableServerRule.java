package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.adaptation;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DISABLE_SERVER;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_DISABLING_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import com.gui.agents.server.ServerNode;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

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
	public boolean evaluateRule(final StrategyFacts facts) {
		return facts.get(ADAPTATION_TYPE).equals(DISABLE_SERVER);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		logger.info("Disabling Server and informing CNA {}.",agentProps.getOwnerCloudNetworkAgent().getLocalName());
		agentProps.disable();
		agentProps.saveMonitoringData();

		facts.put(RULE_TYPE, PROCESS_SERVER_DISABLING_RULE);
		controller.fire(facts);
	}
}
