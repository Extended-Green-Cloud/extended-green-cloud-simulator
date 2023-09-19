package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.greenenergy.adaptation;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CONNECT_GREEN_SOURCE;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_PARAMS;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.AGENT;
import static org.greencloud.commons.enums.rules.RuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_CONNECTION_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.args.adaptation.singleagent.ChangeGreenSourceConnectionParameters;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ConnectGreenSourceRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ConnectGreenSourceRule.class);

	public ConnectGreenSourceRule(final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(ADAPTATION_REQUEST_RULE,
				"connect new Server",
				"connect Green Source with new Server");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return facts.get(ADAPTATION_TYPE).equals(CONNECT_GREEN_SOURCE);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final String targetAgent = ((ChangeGreenSourceConnectionParameters) facts.get(
				ADAPTATION_PARAMS)).getServerName();
		logger.info("Connecting Green Source with new server: {}", targetAgent.split("@")[0]);

		facts.put(AGENT, targetAgent);
		agent.addBehaviour(InitiateRequest.create(agent, facts, PROCESS_SERVER_CONNECTION_RULE, controller));
	}
}
