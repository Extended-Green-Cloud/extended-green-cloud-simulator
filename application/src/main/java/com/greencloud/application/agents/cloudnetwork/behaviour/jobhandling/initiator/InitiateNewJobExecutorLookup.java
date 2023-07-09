package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.CHOSEN_SERVER_FOR_JOB_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.NO_SERVER_AVAILABLE_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.NO_SERVER_RESPONSES_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.CNA_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Vector;

import org.slf4j.Logger;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.behaviours.initiator.AbstractCFPInitiator;
import com.greencloud.application.domain.agent.ServerData;
import com.greencloud.commons.domain.job.ClientJob;

import jade.lang.acl.ACLMessage;

/**
 * Behaviour initiates lookup for Server which will execute the Client's job
 */
public class InitiateNewJobExecutorLookup extends AbstractCFPInitiator<ServerData> {

	private static final Logger logger = getLogger(InitiateNewJobExecutorLookup.class);

	private final ClientJob job;
	private final CloudNetworkAgent myCloudNetworkAgent;

	protected InitiateNewJobExecutorLookup(final CloudNetworkAgent agent, final ACLMessage cfp,
			final ACLMessage schedulerMessage, final ClientJob job) {
		super(agent, cfp, schedulerMessage, mapToJobInstanceId(job), agent.manage().offerComparator(),
				ServerData.class);

		this.job = job;
		this.myCloudNetworkAgent = agent;
	}

	/**
	 * Method creates the behaviour
	 *
	 * @param agent            agent which is executing the behaviour
	 * @param schedulerMessage CFP message received from the scheduler
	 * @param job              job of interest
	 * @return InitiateNewJobExecutorLookup
	 */
	public static InitiateNewJobExecutorLookup create(final CloudNetworkAgent agent, final ACLMessage schedulerMessage,
			final ClientJob job) {
		final ACLMessage cfp = prepareCallForProposal(job, agent.manage().getOwnedActiveServers(),
				CNA_JOB_CFP_PROTOCOL);
		return new InitiateNewJobExecutorLookup(agent, cfp, schedulerMessage, job);
	}

	/**
	 * Method  overrides generic all response handler by an additional check which verifies if the given job exists in
	 * Cloud Network.
	 */
	@Override
	protected void handleAllResponses(final Vector responses, final Vector acceptances) {
		if (!myCloudNetworkAgent.getNetworkJobs().containsKey(job)) {
			myAgent.send(prepareRefuseReply(originalMessage));
			return;
		}
		super.handleAllResponses(responses, acceptances);
	}

	/**
	 * Method logs message regarding no responses and calls generic method responsible for postprocessing the job
	 * execution rejection.
	 */
	@Override
	protected void handleNoResponses() {
		logger.info(NO_SERVER_RESPONSES_LOG);
		handleRejectedJob();
	}

	/**
	 * Method logs message regarding no available servers and calls generic method responsible for postprocessing the
	 * job execution rejection.
	 */
	@Override
	protected void handleNoAvailableAgents() {
		logger.info(NO_SERVER_AVAILABLE_LOG);
		handleRejectedJob();
	}

	/**
	 * Method computes power available at the given moment in Cloud Network and based on that constructs the proposal
	 * that is to be sent to the Scheduler. It initiates the job execution proposal.
	 */
	@Override
	protected void handleSelectedOffer(final ServerData serverData) {
		logger.info(CHOSEN_SERVER_FOR_JOB_LOG, job.getJobId(), bestProposal.getSender().getName());

		myCloudNetworkAgent.getServerForJobMap().put(job.getJobId(), bestProposal.getSender());
		myCloudNetworkAgent.addBehaviour(
				InitiateNewJobOffer.create(myCloudNetworkAgent, serverData, bestProposal, originalMessage));
	}

	private void handleRejectedJob() {
		myCloudNetworkAgent.getNetworkJobs().remove(job);
		myCloudNetworkAgent.manage().updateGUI();
		myAgent.send(prepareRefuseReply(originalMessage));
	}
}
