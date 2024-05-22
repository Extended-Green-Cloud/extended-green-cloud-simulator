package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.servererror;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_SERVER_ERROR_HANDLER_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.servererror.processing.ProcessInternalServerErrorAlertRule;
import org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.servererror.processing.ProcessInternalServerErrorFinishRule;
import org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.servererror.processing.ProcessPutJobOnHoldRule;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class ProcessServerErrorInformationRule extends AgentCombinedRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessServerErrorInformationRule(final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_SERVER_ERROR_HANDLER_RULE,
				"handling information about Server error",
				"handling different types of information regarding possible Server errors");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessInternalServerErrorAlertRule(controller),
				new ProcessInternalServerErrorFinishRule(controller),
				new ProcessPutJobOnHoldRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerErrorInformationRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
