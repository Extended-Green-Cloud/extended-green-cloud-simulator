package com.greencloud.application.agents.client.behaviour.jobannouncement.listener;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_BACK_UP_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FAILED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FINISH_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FINISH_ON_TIME_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_GREEN_POWER_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_START_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_START_ON_TIME_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.templates.JobAnnouncementMessageTemplates.CLIENT_JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.BACK_UP_POWER_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DELAYED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FINISH_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.GREEN_POWER_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.ClientAgentConstants;
import com.gui.agents.ClientAgentNode;
import com.gui.agents.domain.JobStatusEnum;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles the information that the job status has been updated
 */
public class ListenForJobUpdate extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForJobUpdate.class);

	private final ClientAgent myClientAgent;

	/**
	 * Behaviours constructor.
	 *
	 * @param clientAgent agent executing the behaviour
	 */
	public ListenForJobUpdate(final ClientAgent clientAgent) {
		super(clientAgent);
		this.myClientAgent = clientAgent;
	}

	/**
	 * Method which waits for messages informing about changes in the job's status
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE);

		if (Objects.nonNull(message)) {
			switch (message.getProtocol()) {
				case STARTED_JOB_PROTOCOL -> {
					checkIfJobStartedOnTime();
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.IN_PROGRESS);
				}
				case FINISH_JOB_PROTOCOL -> {
					checkIfJobFinishedOnTime();
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.FINISHED);
					myClientAgent.getGuiController().updateClientsCountByValue(-1);
					myClientAgent.doDelete();
				}
				case DELAYED_JOB_PROTOCOL -> {
					logger.info(CLIENT_JOB_DELAY_LOG);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.DELAYED);
				}
				case BACK_UP_POWER_JOB_PROTOCOL -> {
					logger.info(CLIENT_JOB_BACK_UP_LOG);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.ON_BACK_UP);
				}
				case GREEN_POWER_JOB_PROTOCOL -> {
					logger.info(CLIENT_JOB_GREEN_POWER_LOG);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.IN_PROGRESS);
				}
				case FAILED_JOB_PROTOCOL -> {
					logger.info(CLIENT_JOB_FAILED_LOG, myClientAgent.getName());
					myClientAgent.getGuiController().updateClientsCountByValue(-1);
					myClientAgent.doDelete();
				}
			}
		} else {
			block();
		}
	}

	private void checkIfJobStartedOnTime() {
		final Instant startTime = getCurrentTime();
		final long timeDifference = ChronoUnit.MILLIS.between(myClientAgent.getSimulatedJobStart(), startTime);
		if (ClientAgentConstants.MAX_TIME_DIFFERENCE.isValidValue(timeDifference)) {
			logger.info(CLIENT_JOB_START_ON_TIME_LOG);
		} else {
			logger.info(CLIENT_JOB_START_DELAY_LOG, timeDifference);
		}
	}

	private void checkIfJobFinishedOnTime() {
		final Instant endTime = getCurrentTime();
		final long timeDifference = ChronoUnit.MILLIS.between(endTime, myClientAgent.getSimulatedJobEnd());
		if (ClientAgentConstants.MAX_TIME_DIFFERENCE.isValidValue(timeDifference)) {
			logger.info(CLIENT_JOB_FINISH_ON_TIME_LOG);
		} else {
			logger.info(CLIENT_JOB_FINISH_DELAY_LOG, timeDifference);
		}
	}
}
