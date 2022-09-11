package agents.cloudnetwork.behaviour.jobhandling.listener;

import static agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_CFP_NEW_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_CFP_RETRY_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.listener.templates.JobHandlingMessageTemplates.NEW_JOB_REQUEST_TEMPLATE;
import static messages.MessagingUtils.readMessageContent;
import static messages.domain.constants.MessageProtocolConstants.CNA_JOB_CFP_PROTOCOL;
import static messages.domain.factory.CallForProposalMessageFactory.createCallForProposal;
import static utils.GUIUtils.displayMessageArrow;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.cloudnetwork.CloudNetworkAgent;
import agents.cloudnetwork.behaviour.jobhandling.initiator.InitiateNewJobExecutorLookup;
import domain.job.ClientJob;
import domain.job.JobStatusEnum;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles upcoming call for proposals from clients
 */
public class ListenForClientsJob extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForClientsJob.class);

	private CloudNetworkAgent myCloudNetworkAgent;
	private String guid;

	/**
	 * Method casts the abstract agent to the agent of type CloudNetworkAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.guid = myCloudNetworkAgent.getName();
	}

	/**
	 * Method listens for the upcoming job call for proposals from the Client Agents.
	 * It announces the job to the network by sending call for proposal with job characteristics to owned Server Agents.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(NEW_JOB_REQUEST_TEMPLATE);

		if (Objects.nonNull(message)) {
			final ClientJob job = readMessageContent(message, ClientJob.class);
			final String jobId = job.getJobId();
			handleRetryProcess(jobId);
			sendCFPToServers(job, message);
		} else {
			block();
		}
	}

	private void handleRetryProcess(final String jobId) {
		if (myCloudNetworkAgent.getJobRequestRetries().containsKey(jobId)) {
			logger.info(SEND_CFP_RETRY_LOG, guid, jobId, myCloudNetworkAgent.getJobRequestRetries().get(jobId));
		} else {
			myCloudNetworkAgent.getJobRequestRetries().put(jobId, 0);
			final ClientJob previousInstance = myCloudNetworkAgent.manage().getJobById(jobId);
			if (Objects.nonNull(previousInstance)) {
				myCloudNetworkAgent.getNetworkJobs().remove(previousInstance);
			}
			logger.info(SEND_CFP_NEW_LOG, guid, jobId);
		}
	}

	private void sendCFPToServers(final ClientJob job, final ACLMessage message) {
		final ACLMessage cfp = createCallForProposal(job, myCloudNetworkAgent.getOwnedServers(),
				CNA_JOB_CFP_PROTOCOL);

		displayMessageArrow(myCloudNetworkAgent, myCloudNetworkAgent.getOwnedServers());
		myCloudNetworkAgent.getNetworkJobs().put(job, JobStatusEnum.PROCESSING);
		myAgent.addBehaviour(new InitiateNewJobExecutorLookup(myAgent, cfp, message, job.getJobId()));
	}
}
