package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror;

import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_SERVER_ERROR_HANDLER_RULE;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror.processing.ProcessInternalServerErrorAlertRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror.processing.ProcessInternalServerErrorFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror.processing.ProcessPutJobOnHoldRule;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;

import com.gui.agents.greenenergy.GreenEnergyNode;

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
}
