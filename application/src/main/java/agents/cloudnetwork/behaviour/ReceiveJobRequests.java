package agents.cloudnetwork.behaviour;

import static utils.GUIUtils.displayMessageArrow;
import static messages.domain.constants.MessageProtocolConstants.CLIENT_JOB_CFP_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.CNA_JOB_CFP_PROTOCOL;
import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static mapper.JsonMapper.getMapper;
import static utils.JobMapUtils.getJobById;

import agents.cloudnetwork.CloudNetworkAgent;
import domain.job.Job;
import domain.job.JobStatusEnum;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Objects;

import messages.domain.factory.CallForProposalMessageFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behaviour which is responsible for handling upcoming call for proposals from clients
 */
public class ReceiveJobRequests extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ReceiveJobRequests.class);
	private static final MessageTemplate messageTemplate = and(MatchPerformative(CFP),
			MatchProtocol(CLIENT_JOB_CFP_PROTOCOL));

	private CloudNetworkAgent myCloudNetworkAgent;

	/**
	 * Method runs at the behaviour start. It casts the abstract agent to the agent of type CloudNetworkAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
	}

	/**
	 * Method listens for the upcoming job call for proposals from the Client Agents. It announces the job call for proposal
	 * to the network by sending call for proposal with job characteristics to owned Server Agents.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(messageTemplate);

		if (Objects.nonNull(message)) {
			try {
				final Job job = getMapper().readValue(message.getContent(), Job.class);
				final String jobId = job.getJobId();
				if (myCloudNetworkAgent.getJobRequestRetries().containsKey(jobId)) {
					logger.info(
							"[{}] Sending call for proposal to Server Agents for a job request with jobId {}, {} retry.",
							myAgent.getName(), jobId, myCloudNetworkAgent.getJobRequestRetries().get(jobId));
				} else {
					myCloudNetworkAgent.getJobRequestRetries().put(jobId, 0);
					final Job previousInstance = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId);
					if (Objects.nonNull(previousInstance)) {
						myCloudNetworkAgent.getNetworkJobs().remove(previousInstance);
					}
					logger.info("[{}] Sending call for proposal to Server Agents for a job request with jobId {}!",
							myAgent.getName(), jobId);
				}

				final ACLMessage cfp = CallForProposalMessageFactory
						.createCallForProposal(job, myCloudNetworkAgent.getOwnedServers(), CNA_JOB_CFP_PROTOCOL);

				displayMessageArrow(myCloudNetworkAgent, myCloudNetworkAgent.getOwnedServers());
				myCloudNetworkAgent.getNetworkJobs().put(job, JobStatusEnum.PROCESSING);
				myAgent.addBehaviour(new AnnounceNewJobRequest(myAgent, cfp, message, jobId));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			block(75);
		}
	}
}
