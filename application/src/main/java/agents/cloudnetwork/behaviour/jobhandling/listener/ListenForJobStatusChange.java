package agents.cloudnetwork.behaviour.jobhandling.listener;

import static agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_GREEN_POWER_STATUS_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_JOB_FINISH_STATUS_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_JOB_START_STATUS_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.listener.templates.JobHandlingMessageTemplates.JOB_STATUS_CHANGE_TEMPLATE;
import static domain.job.JobStatusEnum.IN_PROGRESS;
import static messages.MessagingUtils.readMessageContent;
import static messages.domain.constants.MessageProtocolConstants.FINISH_JOB_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.GREEN_POWER_JOB_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.cloudnetwork.CloudNetworkAgent;
import domain.job.ClientJob;
import domain.job.JobInstanceIdentifier;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour returns to the client job status update
 */
public class ListenForJobStatusChange extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForJobStatusChange.class);

	private CloudNetworkAgent myCloudNetworkAgent;
	private String guid;

	/**
	 * Method casts the abstract agent to the agent of type CloudNetworkAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.guid = myCloudNetworkAgent.getName();
	}

	/**
	 * Method listens for the information regarding new job status.
	 * It passes that information to the client.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(JOB_STATUS_CHANGE_TEMPLATE);

		if (Objects.nonNull(message)) {
			final JobInstanceIdentifier jobInstanceId = readMessageContent(message, JobInstanceIdentifier.class);
			final String jobId = jobInstanceId.getJobId();

			if (Objects.nonNull(myCloudNetworkAgent.manage().getJobById(jobId))) {
				switch (message.getProtocol()) {
					case FINISH_JOB_PROTOCOL -> handleFinishJobMessage(jobId);
					case STARTED_JOB_PROTOCOL -> handleStartedJobMessage(jobId);
					case POWER_SHORTAGE_FINISH_ALERT_PROTOCOL -> handleGreenPowerJobMessage(jobId);
				}
			}
		} else {
			block();
		}
	}

	private void handleGreenPowerJobMessage(final String jobId) {
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);
		logger.info(SEND_GREEN_POWER_STATUS_LOG, guid, jobId);
		myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), GREEN_POWER_JOB_PROTOCOL));
	}

	private void handleStartedJobMessage(final String jobId) {
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);

		if (!myCloudNetworkAgent.getNetworkJobs().get(job).equals(IN_PROGRESS)) {
			logger.info(SEND_JOB_START_STATUS_LOG, guid, jobId);
			myCloudNetworkAgent.getNetworkJobs().replace(myCloudNetworkAgent.manage().getJobById(jobId), IN_PROGRESS);
			myCloudNetworkAgent.manage().incrementStartedJobs(jobId);
			myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), STARTED_JOB_PROTOCOL));
		}
	}

	private void handleFinishJobMessage(final String jobId) {
		final Long completedJobs = myCloudNetworkAgent.completedJob();
		logger.info(SEND_JOB_FINISH_STATUS_LOG, guid, jobId, completedJobs);
		final String clientId = myCloudNetworkAgent.manage().getJobById(jobId).getClientIdentifier();
		updateNetworkInformation(jobId);
		myAgent.send(prepareJobStatusMessageForClient(clientId, FINISH_JOB_PROTOCOL));
	}

	private void updateNetworkInformation(final String jobId) {
		myCloudNetworkAgent.getNetworkJobs().remove(myCloudNetworkAgent.manage().getJobById(jobId));
		myCloudNetworkAgent.getServerForJobMap().remove(jobId);
		myCloudNetworkAgent.manage().incrementFinishedJobs(jobId);
	}
}
