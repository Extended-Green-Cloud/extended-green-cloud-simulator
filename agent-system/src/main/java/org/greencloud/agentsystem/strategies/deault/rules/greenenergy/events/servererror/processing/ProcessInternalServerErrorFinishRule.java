package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.servererror.processing;

import static java.util.Objects.nonNull;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_SERVER_ERROR_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_SERVER_ERROR_HANDLE_FINISH_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceIdAndServer;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.NETWORK_ERROR_FINISH_ALERT_PROTOCOL;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessInternalServerErrorFinishRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessInternalServerErrorFinishRule.class);

	private ACLMessage message;

	public ProcessInternalServerErrorFinishRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_SERVER_ERROR_HANDLER_RULE,
				LISTEN_FOR_SERVER_ERROR_HANDLE_FINISH_RULE,
				"handling information about Server error finish",
				"handling different types of information regarding possible Server errors");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		message = facts.get(MESSAGE);
		return message.getProtocol().equals(NETWORK_ERROR_FINISH_ALERT_PROTOCOL);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(message, JobInstanceIdentifier.class);
		final ServerJob job = getJobByInstanceIdAndServer(jobInstanceId.getJobInstanceId(), message.getSender(),
				agentProps.getServerJobs());

		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		if (nonNull(job)) {
			logger.info("Power shortage in server finished. Changing the status of the server job {}",
					jobInstanceId.getJobId());
			final boolean hasStarted = isJobStarted(job, agentProps.getServerJobs());
			final JobExecutionStatusEnum previousStatus = agentProps.getServerJobs().get(job);
			final JobExecutionStatusEnum newStatus = EXECUTING_ON_GREEN.getStatus(hasStarted);

			agentProps.getJobsExecutionTime()
					.updateJobExecutionDuration(job, previousStatus, newStatus, getCurrentTime());
			agentProps.getServerJobs().replace(job, newStatus);
			agentProps.updateGUI();
		} else {
			logger.info("Job {} to supply with green power was not found", jobInstanceId.getJobId());
		}
	}
}
