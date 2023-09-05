package com.greencloud.application.agents.server.behaviour.errorhandling.listener;

import static com.greencloud.application.agents.server.behaviour.errorhandling.listener.logs.ErrorHandlingListenerLog.GS_SHORTAGE_FINISH_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.listener.templates.ErrorHandlingMessageTemplates.SOURCE_POWER_SHORTAGE_FINISH_TEMPLATE;
import static com.greencloud.application.agents.server.constants.ServerAgentConstants.MAX_MESSAGE_NUMBER;
import static com.greencloud.application.messages.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceId;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.POWER_SHORTAGE_SOURCE_STATUSES;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.commons.constants.LoggingConstant;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the information that the power shortage in the given green source has finished
 */
public class ListenForSourcePowerShortageFinish extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForSourcePowerShortageFinish.class);

	private ServerAgent myServerAgent;

	/**
	 * Method casts the abstract agent to agent of type Server Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method listens for the message coming from the Green Source informing that the power
	 * shortage has finished and that given power job can be supplied again using green source power.
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = myAgent.receive(SOURCE_POWER_SHORTAGE_FINISH_TEMPLATE, MAX_MESSAGE_NUMBER);

		if (nonNull(messages)) {
			messages.forEach(message -> {
				final JobInstanceIdentifier jobInstance = readMessageContent(message, JobInstanceIdentifier.class);
				final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), myServerAgent.getServerJobs());

				if (nonNull(job) && POWER_SHORTAGE_SOURCE_STATUSES.contains(myServerAgent.getServerJobs().get(job))) {
					MDC.put(LoggingConstant.MDC_JOB_ID, job.getJobId());
					logger.info(GS_SHORTAGE_FINISH_LOG, job.getJobId());
					final boolean hasStarted = isJobStarted(job, myServerAgent.getServerJobs());

					myServerAgent.getServerJobs().replace(job, EXECUTING_ON_GREEN.getStatus(hasStarted));
					myServerAgent.manage().updateGUI();
					myServerAgent.message().informCNAAboutStatusChange(jobInstance, GREEN_POWER_JOB_ID);
				}
			});
		} else {
			block();
		}
	}
}
