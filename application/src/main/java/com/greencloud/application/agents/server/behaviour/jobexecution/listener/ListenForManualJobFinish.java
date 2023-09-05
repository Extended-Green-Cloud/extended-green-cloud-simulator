package com.greencloud.application.agents.server.behaviour.jobexecution.listener;

import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_FINISHED_MANUALLY_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.templates.JobHandlingMessageTemplates.MANUAL_FINISH_TEMPLATE;
import static com.greencloud.application.agents.server.constants.ServerAgentConstants.MAX_MESSAGE_NUMBER;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceId;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.IN_PROGRESS;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for energy supply message informing that the energy supply has finished manually
 */
public class ListenForManualJobFinish extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForManualJobFinish.class);

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
	 * Method listens for the messages informing that the job execution was finished manually as
	 * the message about job finish did not come to Green Source on time.
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = myAgent.receive(MANUAL_FINISH_TEMPLATE, MAX_MESSAGE_NUMBER);

		if (nonNull(messages)) {
			messages.forEach(message -> {
				final JobInstanceIdentifier identifier = readMessageContent(message, JobInstanceIdentifier.class);
				final ClientJob job = getJobByInstanceId(identifier.getJobInstanceId(), myServerAgent.getServerJobs());

				if (nonNull(job) && myServerAgent.getServerJobs().containsKey(job)) {
					final JobExecutionStatusEnum statusEnum = myServerAgent.getServerJobs().get(job);
					final String clientName = job.getClientIdentifier();

					if (statusEnum.equals(IN_PROGRESS)) {
						MDC.put(MDC_JOB_ID, job.getJobId());
						logger.debug(SUPPLY_FINISHED_MANUALLY_LOG, clientName, clientName);
						final boolean informCNA = myServerAgent.getServerJobs().keySet().stream()
								.filter(clientJob -> clientJob.getJobId().equals(job.getJobId())).count() == 1;
						myServerAgent.manage().finishJobExecution(job, informCNA);
					} else {
						myServerAgent.getServerJobs().remove(job);
						myServerAgent.manage().updateGUI();
					}
				}
			});
		} else {
			block();
		}
	}

}
