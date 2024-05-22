package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.adaptation;

import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.DECREASE_GREEN_SOURCE_ERROR;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.INCREASE_GREEN_SOURCE_ERROR;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
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
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;

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
		final AdaptationActionTypeEnum actionEnum = facts.get(ADAPTATION_TYPE);
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
		final String agentName = targetAgent.split("@")[0];
		logger.info("Disconnecting Green Source from server: {}", agentName);

		facts.put(AGENT, targetAgent);
		agentProps.getGreenSourceDisconnection().setBeingDisconnected(true);
		agentProps.getGreenSourceDisconnection().setOriginalAdaptationMessage(message);
		agent.addBehaviour(InitiateRequest.create(agent, facts, PROCESS_SERVER_DEACTIVATION_RULE, controller));
	}

	@Override
	public AgentRule copy() {
		return new ChangeWeatherPredictionErrorRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}

	private void setWeatherPredictionError(double weatherPredictionError) {
		agentProps.setWeatherPredictionError(weatherPredictionError);
		if (nonNull(agentNode)) {
			agentNode.updatePredictionError(weatherPredictionError);
		}
	}
}
