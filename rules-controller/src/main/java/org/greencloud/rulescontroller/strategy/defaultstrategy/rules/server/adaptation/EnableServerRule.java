package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.adaptation;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ENABLE_SERVER;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_ENABLING_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import com.gui.agents.server.ServerNode;

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
	public boolean evaluateRule(final StrategyFacts facts) {
		return facts.get(ADAPTATION_TYPE).equals(ENABLE_SERVER);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		logger.info("Enabling Server and informing CNA {}.", agentProps.getOwnerCloudNetworkAgent().getLocalName());
		agentProps.enable();
		agentProps.saveMonitoringData();

		facts.put(RULE_TYPE, PROCESS_SERVER_ENABLING_RULE);
		controller.fire(facts);
	}
}
