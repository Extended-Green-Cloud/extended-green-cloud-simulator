package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_IS_PRESENT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate.processing.ProcessServerJobStatusUpdateConfirmedJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate.processing.ProcessServerJobStatusUpdateFailedJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate.processing.ProcessServerJobStatusUpdateFinishedJobRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate.processing.ProcessServerJobStatusUpdateJobOnHoldRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate.processing.ProcessServerJobStatusUpdateJobWithBackUpRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate.processing.ProcessServerJobStatusUpdateJobWithGreenEnergyRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.listening.jobupdate.processing.ProcessServerJobStatusUpdateStartedJobRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

public class ProcessServerJobStatusUpdateCombinedRule
		extends AgentCombinedRule<RegionalManagerAgentProps, RMANode> {

	public ProcessServerJobStatusUpdateCombinedRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
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
		final ACLMessage message = facts.get(MESSAGE);
		final String messageType = message.getProtocol().equals(FAILED_JOB_PROTOCOL) ? FAILED_JOB_ID :
				message.getConversationId();

		if (!messageType.equals(STARTED_JOB_ID)) {
			final JobWithStatus jobStatusUpdate = readMessageContent(message, JobWithStatus.class);
			final ClientJob job = getJobById(jobStatusUpdate.getJobId(), agentProps.getNetworkJobs());
			final boolean isJobPresent = nonNull(job);

			ofNullable(job).ifPresent(processedJob -> facts.put(JOB, processedJob));
			facts.put(MESSAGE_CONTENT, jobStatusUpdate);
			facts.put(JOB_IS_PRESENT, isJobPresent);
		}
		facts.put(MESSAGE_TYPE, messageType);
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerJobStatusUpdateCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
