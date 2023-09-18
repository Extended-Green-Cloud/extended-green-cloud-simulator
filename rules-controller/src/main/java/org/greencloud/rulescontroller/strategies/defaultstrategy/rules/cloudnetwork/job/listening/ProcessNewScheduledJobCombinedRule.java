package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.cloudnetwork.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.cloudnetwork.job.listening.processing.ProcessNewScheduledJobNoServersRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.cloudnetwork.job.listening.processing.ProcessNewScheduledJobRule;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

public class ProcessNewScheduledJobCombinedRule extends AgentCombinedRule<CloudNetworkAgentProps, CloudNetworkNode> {
	public ProcessNewScheduledJobCombinedRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller, AgentCombinedRuleType.EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handles new scheduled jobs",
				"rule run when CNA processes new job received from CBA");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNewScheduledJobNoServersRule(controller),
				new ProcessNewScheduledJobRule(controller)
		);
	}
}
