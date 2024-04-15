package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static org.jrba.rulesengine.enums.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.processing.ProcessServerJobStatusUpdateConfirmedJobRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.processing.ProcessServerJobStatusUpdateFailedJobRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.processing.ProcessServerJobStatusUpdateFinishedJobRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.processing.ProcessServerJobStatusUpdateJobOnHoldRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.processing.ProcessServerJobStatusUpdateJobWithBackUpRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.processing.ProcessServerJobStatusUpdateJobWithGreenEnergyRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.processing.ProcessServerJobStatusUpdateStartedJobRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

public class ProcessServerJobStatusUpdateCombinedRule
		extends AgentCombinedRule<RegionalManagerAgentProps, RegionalManagerNode> {

	public ProcessServerJobStatusUpdateCombinedRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
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
