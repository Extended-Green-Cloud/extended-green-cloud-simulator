package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.adaptation;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DECREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static java.util.Objects.nonNull;
import static org.jrba.rulesengine.constants.FactTypeConstants.ADAPTATION_PARAMS;
import static org.jrba.rulesengine.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SERVER_DEACTIVATION_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.adaptation.singleagent.AdjustGreenSourceErrorParameters;
import org.greencloud.commons.args.adaptation.singleagent.ChangeGreenSourceConnectionParameters;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import com.database.knowledge.domain.action.AdaptationActionEnum;

import jade.lang.acl.ACLMessage;

public class ChangeWeatherPredictionErrorRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ChangeWeatherPredictionErrorRule.class);

	public ChangeWeatherPredictionErrorRule(final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(ADAPTATION_REQUEST_RULE,
				"change weather prediction error",
				"method adjusts current weather prediction error of the Green Source");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final AdaptationActionEnum actionEnum = facts.get(ADAPTATION_TYPE);
		return List.of(INCREASE_GREEN_SOURCE_ERROR, DECREASE_GREEN_SOURCE_ERROR).contains(actionEnum);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final AdjustGreenSourceErrorParameters params = facts.get(ADAPTATION_PARAMS);
		final double currentError = agentProps.getWeatherPredictionError();
		final double newError = currentError + params.getPercentageChange();
		final String log = params.getPercentageChange() > 0 ? "Increasing" : "Decreasing";

		logger.info("{} value of weather prediction error from {} to {}", log, currentError, newError);

		setWeatherPredictionError(newError);
		agentProps.updateGUI();
		final ACLMessage message = facts.get(MESSAGE);
		final String targetAgent = ((ChangeGreenSourceConnectionParameters) facts.get(
				ADAPTATION_PARAMS)).getServerName();
		logger.info("Disconnecting Green Source from server: {}", targetAgent.split("@")[0]);

		facts.put(AGENT, targetAgent);
		agentProps.getGreenSourceDisconnection().setBeingDisconnected(true);
		agentProps.getGreenSourceDisconnection().setOriginalAdaptationMessage(message);
		agent.addBehaviour(InitiateRequest.create(agent, facts, PROCESS_SERVER_DEACTIVATION_RULE, controller));
	}

	private void setWeatherPredictionError(double weatherPredictionError) {
		agentProps.setWeatherPredictionError(weatherPredictionError);
		if (nonNull(agentNode)) {
			agentNode.updatePredictionError(weatherPredictionError);
		}
	}
}
