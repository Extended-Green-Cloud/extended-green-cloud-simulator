package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;
import java.util.Optional;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.processing.ProcessUpdateFromGreenSourceJobConfirmationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.processing.ProcessUpdateFromGreenSourceJobFailureRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.processing.ProcessUpdateFromGreenSourceTransferConfirmationRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.processing.ProcessUpdateFromGreenSourceTransferFailureRule;

public class ProcessUpdateFromGreenSourceCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessUpdateFromGreenSourceCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE,
				"handles updates regarding job execution",
				"handling messages received from Green Source informing about changes in power supply");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessUpdateFromGreenSourceJobConfirmationRule(controller),
				new ProcessUpdateFromGreenSourceTransferConfirmationRule(controller),
				new ProcessUpdateFromGreenSourceJobFailureRule(controller),
				new ProcessUpdateFromGreenSourceTransferFailureRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final JobInstanceIdentifier instance = facts.get(MESSAGE_CONTENT);
		final ClientJob job = getJobByInstanceId(instance.getJobInstanceId(), agentProps.getServerJobs());
		final Optional<ClientJob> jobOptional = ofNullable(job);

		if (jobOptional.isPresent()) {
			facts.put(JOB, jobOptional.get());
			return true;
		}
		return false;
	}
}
