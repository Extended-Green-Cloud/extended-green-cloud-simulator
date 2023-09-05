package com.greencloud.application.agents.server.behaviour.jobexecution.listener;

import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SERVER_NEW_JOB_LACK_OF_RESOURCES_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SERVER_NEW_JOB_LOOK_FOR_SOURCE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SERVER_NEW_NO_SOURCES_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.templates.JobHandlingMessageTemplates.NEW_JOB_CFP_TEMPLATE;
import static com.greencloud.application.agents.server.constants.ServerAgentConstants.MAX_MESSAGE_NUMBER;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareRefuseReply;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.PROCESSING;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.jobexecution.initiator.InitiateEnergySupplyForJob;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.resources.HardwareResources;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles upcoming job's CFP from Cloud Network Agents
 */
public class ListenForNewJob extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForNewJob.class);

	private ServerAgent myServerAgent;

	/**
	 * Method casts the agent to the ServerAgent.
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method listens for the upcoming job CFP coming from the Cloud Network Agents.
	 * It validates whether the server has enough resources to handle the job.
	 * If yes, then it sends the CFP to owned green sources to find available energy supplier.
	 * If no, then it sends the refuse message to the Cloud Network Agent.
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = myAgent.receive(NEW_JOB_CFP_TEMPLATE, MAX_MESSAGE_NUMBER);

		if (Objects.nonNull(messages)) {
			messages.forEach(message -> {
				final ClientJob job = readMessageContent(message, ClientJob.class);
				final HardwareResources resources = myServerAgent.resources().getAvailableResources(job, null, null);

				MDC.put(MDC_JOB_ID, job.getJobId());

				if (resources.areSufficient(job.getEstimatedResources())
						&& !myServerAgent.getServerJobs().containsKey(job)
						&& myServerAgent.canTakeIntoProcessing()
						&& !myServerAgent.getOwnedGreenSources().isEmpty()
						&& !myServerAgent.isHasError()) {
					MDC.put(MDC_JOB_ID, job.getJobId());
					logger.info(SERVER_NEW_JOB_LOOK_FOR_SOURCE_LOG);

					myServerAgent.getServerJobs().putIfAbsent(job, PROCESSING);
					myServerAgent.tookJobIntoProcessing();
					myAgent.addBehaviour(InitiateEnergySupplyForJob.create(job, myServerAgent, message));
				} else if (myServerAgent.getOwnedGreenSources().isEmpty()) {
					logger.info(SERVER_NEW_NO_SOURCES_LOG);
					myAgent.send(prepareRefuseReply(message));
				} else {
					logger.info(SERVER_NEW_JOB_LACK_OF_RESOURCES_LOG);
					myAgent.send(prepareRefuseReply(message));
				}
			});
		} else {
			block();
		}
	}
}
