package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.maintenance;

import static org.greencloud.commons.constants.FactTypeConstants.EVENT;
import static org.greencloud.commons.constants.FactTypeConstants.RESOURCES;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_MAINTENANCE_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import com.gui.agents.server.ServerNode;
import com.gui.event.ServerMaintenanceEvent;

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

		logger.info("Received information about server maintenance - informing CNA about changes in server resources!");
		agentNode.confirmMaintenanceInServer();

		final RuleSetFacts maintenanceFacts = new RuleSetFacts(controller.getLatestRuleSet().get());
		maintenanceFacts.put(RESOURCES, serverMaintenanceEvent.getNewResources());
		agent.addBehaviour(
				InitiateRequest.create(agent, maintenanceFacts, "SERVER_MAINTENANCE_REQUEST_RULE", controller));
	}

}
