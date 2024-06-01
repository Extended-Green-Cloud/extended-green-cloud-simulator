package org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.adaptation;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.DISCONNECT_GREEN_SOURCE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SERVER_DEACTIVATION_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.ADAPTATION_PARAMS;
import static org.jrba.rulesengine.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.slf4j.LoggerFactory.getLogger;

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

import jade.lang.acl.ACLMessage;

public class DisconnectGreenSourceRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(DisconnectGreenSourceRule.class);

	public DisconnectGreenSourceRule(final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(ADAPTATION_REQUEST_RULE,
				"disconnect new Server",
				"disconnect Green Source with new Server");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return facts.get(ADAPTATION_TYPE).equals(DISCONNECT_GREEN_SOURCE);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
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
		return new DisconnectGreenSourceRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
