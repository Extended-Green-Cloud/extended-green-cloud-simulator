package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing.ProcessServerJobStatusUpdateConfirmedJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing.ProcessServerJobStatusUpdateFailedJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing.ProcessServerJobStatusUpdateFinishedJobRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing.ProcessServerJobStatusUpdateJobOnHoldRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing.ProcessServerJobStatusUpdateJobWithBackUpRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing.ProcessServerJobStatusUpdateJobWithGreenEnergyRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing.ProcessServerJobStatusUpdateStartedJobRule;

import jade.lang.acl.ACLMessage;

public class ProcessServerJobStatusUpdateCombinedRule
		extends AgentCombinedRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ProcessServerJobStatusUpdateCombinedRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE,
				"handles updates regarding execution of the job in server",
				"rule run when Server sends update regarding job execution status");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessServerJobStatusUpdateFailedJobRule(controller),
				new ProcessServerJobStatusUpdateFinishedJobRule(controller),
				new ProcessServerJobStatusUpdateStartedJobRule(controller),
				new ProcessServerJobStatusUpdateJobOnHoldRule(controller),
				new ProcessServerJobStatusUpdateJobWithBackUpRule(controller),
				new ProcessServerJobStatusUpdateJobWithGreenEnergyRule(controller),
				new ProcessServerJobStatusUpdateConfirmedJobRule(controller));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);
		final ACLMessage message = facts.get(MESSAGE);
		final String messageType =
				message.getProtocol().equals(FAILED_JOB_PROTOCOL) ? FAILED_JOB_ID : message.getConversationId();

		final String jobId = jobStatusUpdate.getJobInstance().getJobId();
		final ClientJob job = getJobById(jobId, agentProps.getNetworkJobs());

		facts.put(MESSAGE_TYPE, messageType);
		facts.put(JOB, ofNullable(job));
	}

}
