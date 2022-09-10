package agents.server.behaviour.jobexecution.listener;

import static agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_JOB_ANNOUNCEMENT_LOG;
import static agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_JOB_FINISHED_LOG;
import static agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_JOB_SCHEDULING_LOG;
import static agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_FINISHED_MANUALLY_LOG;
import static agents.server.behaviour.jobexecution.listener.templates.JobHandlingMessageTemplates.POWER_SUPPLY_UPDATE_TEMPLATE;
import static java.util.Objects.nonNull;
import static messages.MessagingUtils.readMessageContent;
import static messages.domain.constants.MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static utils.GUIUtils.announceBookedJob;
import static utils.JobMapUtils.getJobById;
import static utils.JobMapUtils.getJobByIdAndStartDate;
import static utils.TimeUtils.getCurrentTime;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import agents.server.behaviour.jobexecution.handler.HandleJobStart;
import domain.job.Job;
import domain.job.JobInstanceIdentifier;
import domain.job.JobStatusEnum;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for power supply update messages coming from Green Source
 */
public class ListenForPowerSupplyUpdate extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForPowerSupplyUpdate.class);

	private ServerAgent myServerAgent;
	private String guid;

	/**
	 * Method casts the abstract agent to agent of type Server Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
		this.guid = myServerAgent.getName();
	}

	/**
	 * Method listens for the messages coming from Green Source with updates regarding power supply.
	 * It handles two types of messages:
	 *
	 * - messages confirming that given green source will provide power necessary to supply the job (either new job
	 * or transferred one)
	 * - messages informing that the job execution was finished manually as the message about job finish did not come
	 * on time
	 */
	@Override
	public void action() {
		final ACLMessage inform = myAgent.receive(POWER_SUPPLY_UPDATE_TEMPLATE);

		if (Objects.nonNull(inform)) {
			switch (inform.getProtocol()) {
				case MANUAL_JOB_FINISH_PROTOCOL -> handlePowerSupplyManualFinishMessage(inform);
				default -> handlePowerConfirmationMessage(inform);
			}
		} else {
			block();
		}
	}

	private void handlePowerSupplyManualFinishMessage(final ACLMessage inform) {
		final Job job = retrieveJobFromMessage(inform);
		final JobStatusEnum statusEnum = myServerAgent.getServerJobs().getOrDefault(job, null);

		if (nonNull(statusEnum) && statusEnum.equals(JobStatusEnum.IN_PROGRESS)) {
			logger.debug(SUPPLY_FINISHED_MANUALLY_LOG, guid, job.getClientIdentifier(), job.getClientIdentifier());
			myServerAgent.manage().finishJobExecution(job, true);
		}
	}

	private void handlePowerConfirmationMessage(final ACLMessage inform) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(inform, JobInstanceIdentifier.class);
		final String messageType = inform.getProtocol();
		final String jobId = jobInstanceId.getJobId();

		if (messageType.equals(SERVER_JOB_CFP_PROTOCOL)) {
			logger.info(SUPPLY_CONFIRMATION_JOB_ANNOUNCEMENT_LOG, guid, jobId);
			announceBookedJob(myServerAgent);
		}
		scheduleJobExecution(jobInstanceId, messageType);
	}

	private void scheduleJobExecution(final JobInstanceIdentifier jobInstanceId, final String messageType) {
		final Job job = getJobByIdAndStartDate(myServerAgent.getServerJobs(), jobInstanceId);

		if (nonNull(job)) {
			logger.info(SUPPLY_CONFIRMATION_JOB_SCHEDULING_LOG, guid, jobInstanceId.getJobId());
			final boolean informCNAStart = messageType.equals(SERVER_JOB_CFP_PROTOCOL) || jobInstanceId.getStartTime()
					.isAfter(getCurrentTime());
			myAgent.addBehaviour(HandleJobStart.createFor(myServerAgent, job, informCNAStart, true));
		} else {
			logger.info(SUPPLY_CONFIRMATION_JOB_FINISHED_LOG, guid, jobInstanceId.getJobId());
		}
	}

	private Job retrieveJobFromMessage(final ACLMessage inform) {
		try {
			final String jobId = readMessageContent(inform, String.class);
			return getJobById(myServerAgent.getServerJobs(), jobId);
		} catch (Exception e) {
			final JobInstanceIdentifier identifier = readMessageContent(inform, JobInstanceIdentifier.class);
			return getJobByIdAndStartDate(myServerAgent.getServerJobs(), identifier);
		}
	}

}
