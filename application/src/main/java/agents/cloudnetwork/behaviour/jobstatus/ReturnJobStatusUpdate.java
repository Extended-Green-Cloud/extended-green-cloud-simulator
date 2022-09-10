package agents.cloudnetwork.behaviour.jobstatus;

import static messages.domain.constants.MessageProtocolConstants.FINISH_JOB_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.GREEN_POWER_JOB_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static domain.job.JobStatusEnum.IN_PROGRESS;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;
import static mapper.JsonMapper.getMapper;
import static messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static utils.JobMapUtils.getJobById;

import agents.cloudnetwork.CloudNetworkAgent;
import domain.job.Job;
import domain.job.JobInstanceIdentifier;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Behaviour responsible for returning to the client job status update
 */
public class ReturnJobStatusUpdate extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ReturnJobStatusUpdate.class);
	private static final MessageTemplate messageTemplate = and(MatchPerformative(INFORM),
			or(or(MatchProtocol(FINISH_JOB_PROTOCOL),
							MatchProtocol(STARTED_JOB_PROTOCOL)),
					MatchProtocol(POWER_SHORTAGE_FINISH_ALERT_PROTOCOL)));

	private CloudNetworkAgent myCloudNetworkAgent;

	/**
	 * Method runs at the behaviour start. It casts the abstract agent to the agent of type CloudNetworkAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
	}

	/**
	 * Method which listens for the information regarding new job status.
	 * It passes that information to the client.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(messageTemplate);

		if (Objects.nonNull(message)) {
			try {
				final JobInstanceIdentifier jobInstanceId = getMapper().readValue(message.getContent(),
						JobInstanceIdentifier.class);
				if (Objects.nonNull(getJobById(myCloudNetworkAgent.getNetworkJobs(), jobInstanceId.getJobId()))) {
					switch (message.getProtocol()) {
						case FINISH_JOB_PROTOCOL -> handleFinishJobMessage(jobInstanceId);
						case STARTED_JOB_PROTOCOL -> handleStartedJobMessage(jobInstanceId);
						case POWER_SHORTAGE_FINISH_ALERT_PROTOCOL -> handleGreenPowerJobMessage(jobInstanceId);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			block();
		}
	}

	private void handleGreenPowerJobMessage(final JobInstanceIdentifier jobInstanceId) {
		final Job job = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobInstanceId.getJobId());
		logger.info("[{}] Sending information that the job {} is executed again using green power", myAgent.getName(),
				jobInstanceId.getJobId());
		myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), GREEN_POWER_JOB_PROTOCOL));
	}

	private void handleStartedJobMessage(final JobInstanceIdentifier jobInstanceId) {
		final Job job = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobInstanceId.getJobId());
		if (!myCloudNetworkAgent.getNetworkJobs().get(job).equals(IN_PROGRESS)) {
			logger.info("[{}] Sending information that the job {} execution has started", myAgent.getName(),
					jobInstanceId.getJobId());
			myCloudNetworkAgent.getNetworkJobs()
					.replace(getJobById(myCloudNetworkAgent.getNetworkJobs(), jobInstanceId.getJobId()), IN_PROGRESS);
			myCloudNetworkAgent.manage().incrementStartedJobs(jobInstanceId.getJobId());
			myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), STARTED_JOB_PROTOCOL));
		}
	}

	private void handleFinishJobMessage(final JobInstanceIdentifier jobInstanceId) {
		final Long completedJobs = myCloudNetworkAgent.completedJob();
		logger.info("[{}] Sending information that the job {} execution is finished. So far completed {} jobs!",
				myAgent.getName(), jobInstanceId.getJobId(), completedJobs);
		final String clientId = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobInstanceId.getJobId()).getClientIdentifier();
		updateNetworkInformation(jobInstanceId.getJobId());
		myAgent.send(prepareJobStatusMessageForClient(clientId, FINISH_JOB_PROTOCOL));
	}

	private void updateNetworkInformation(final String jobId) {
		myCloudNetworkAgent.getNetworkJobs().remove(getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId));
		myCloudNetworkAgent.getServerForJobMap().remove(jobId);
		myCloudNetworkAgent.manage().incrementFinishedJobs(jobId);
	}
}
