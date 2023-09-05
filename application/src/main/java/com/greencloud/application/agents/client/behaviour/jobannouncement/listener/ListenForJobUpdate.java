package com.greencloud.application.agents.client.behaviour.jobannouncement.listener;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_UNKNOWN_UPDATE_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.templates.JobAnnouncementMessageTemplates.CLIENT_JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.messages.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.RE_SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.STARTED_JOB_ID;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Objects;

import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.behaviour.jobannouncement.handler.AbstractJobUpdateHandler;
import com.greencloud.application.agents.client.behaviour.jobannouncement.handler.HandleGenericJobStatusUpdate;
import com.greencloud.application.agents.client.behaviour.jobannouncement.handler.HandleJobFailedUpdate;
import com.greencloud.application.agents.client.behaviour.jobannouncement.handler.HandleJobFinishUpdate;
import com.greencloud.application.agents.client.behaviour.jobannouncement.handler.HandleJobStartUpdate;
import com.greencloud.application.agents.client.behaviour.jobannouncement.handler.HandlePostponeJobUpdate;
import com.greencloud.application.agents.client.behaviour.jobannouncement.handler.HandleRescheduleJobUpdate;
import com.greencloud.application.agents.client.domain.enums.ClientJobUpdateEnum;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles the information that the job status has been updated
 */
public class ListenForJobUpdate extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForJobUpdate.class);

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
	 * Method waits for messages informing about changes in the job's status and executes corresponding handler
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE);

		if (Objects.nonNull(message)) {
			try {
				final ClientJobUpdateEnum updateEnum = ClientJobUpdateEnum.valueOf(message.getConversationId());
				final AbstractJobUpdateHandler updateHandler = getUpdateHandler(message, updateEnum);

				if (isNull(updateHandler)) {
					logger.info(CLIENT_UNKNOWN_UPDATE_LOG, message.getConversationId());
					return;
				}
				if (nonNull(updateEnum.getLogMessage())) {
					logger.info(updateEnum.getLogMessage());
				}
				myClientAgent.addBehaviour(updateHandler);

			} catch (IllegalArgumentException e) {
				logger.info(CLIENT_UNKNOWN_UPDATE_LOG, message.getConversationId());
			}
		} else {
			block();
		}
	}

	@VisibleForTesting
	protected AbstractJobUpdateHandler getUpdateHandler(final ACLMessage message,
			final ClientJobUpdateEnum updateEnum) {
		return switch (message.getConversationId()) {
			case SCHEDULED_JOB_ID,
					PROCESSING_JOB_ID,
					DELAYED_JOB_ID,
					BACK_UP_POWER_JOB_ID,
					GREEN_POWER_JOB_ID,
					ON_HOLD_JOB_ID -> new HandleGenericJobStatusUpdate(message, myClientAgent, updateEnum);
			case STARTED_JOB_ID -> new HandleJobStartUpdate(message, myClientAgent, updateEnum);
			case FINISH_JOB_ID -> new HandleJobFinishUpdate(message, myClientAgent, updateEnum);
			case FAILED_JOB_ID -> new HandleJobFailedUpdate(message, myClientAgent, updateEnum);
			case POSTPONED_JOB_ID -> new HandlePostponeJobUpdate(message, myClientAgent, updateEnum);
			case RE_SCHEDULED_JOB_ID -> new HandleRescheduleJobUpdate(message, myClientAgent, updateEnum);
			default -> null;
		};
	}
}
