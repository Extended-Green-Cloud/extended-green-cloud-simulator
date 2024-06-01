package org.greencloud.agentsystem.strategies.deafult.rules.server.events.maintenance.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESOURCES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_MAINTENANCE_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.gui.event.ServerMaintenanceEvent;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.slf4j.Logger;

public class ProcessServerMaintenanceRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessServerMaintenanceRule.class);

	public ProcessServerMaintenanceRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_MAINTENANCE_RULE,
				"rule changes configuration of the given server",
				"performing changes in configuration of the given Server");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ServerMaintenanceEvent serverMaintenanceEvent = facts.get(EVENT);

		logger.info("Received information about server maintenance - informing RMA about changes in server resources!");
		agentNode.confirmMaintenanceInServer();

		final RuleSetFacts maintenanceFacts = new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get());
		maintenanceFacts.put(RESOURCES, serverMaintenanceEvent.getNewResources());
		agent.addBehaviour(
				InitiateRequest.create(agent, maintenanceFacts, "SERVER_MAINTENANCE_REQUEST_RULE", controller));
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerMaintenanceRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
