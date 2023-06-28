package com.greencloud.application.agents.server.behaviour.jobexecution.initiator;

import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_NO_POWER_AVAILABLE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_NO_RESPONSE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_NO_SOURCES_AVAILABLE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_SELECTED_GS_LOG;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapJobToPowerJob;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareRefuseReply;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareReply;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.behaviours.initiator.AbstractCFPInitiator;
import com.greencloud.application.domain.agent.GreenSourceData;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour searches for available Green Sources that can supply new job with power
 */
public class InitiatePowerDeliveryForJob extends AbstractCFPInitiator<GreenSourceData> {

	private static final Logger logger = getLogger(InitiatePowerDeliveryForJob.class);

	private final ACLMessage cnaMessage;
	private final ServerAgent myServerAgent;
	private final ClientJob job;

	private InitiatePowerDeliveryForJob(final ServerAgent agent, final ACLMessage powerRequest,
			final ACLMessage cnaMessage, final ClientJob job) {
		super(agent, powerRequest, cnaMessage, mapToJobInstanceId(job), agent.manage().offerComparator(),
				GreenSourceData.class);

		this.cnaMessage = cnaMessage;
		this.job = job;
		this.myServerAgent = agent;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent      agent which executes the behaviour
	 * @param job        job of interest
	 * @param cnaMessage message received from Cloud Network Agent
	 * @return InitiatePowerDeliveryForJob
	 */
	public static InitiatePowerDeliveryForJob create(final ClientJob job, final ServerAgent agent,
			final ACLMessage cnaMessage) {
		final List<AID> greenSources = agent.manage().getOwnedActiveGreenSources().stream().toList();
		final ACLMessage cfp = prepareCallForProposal(mapJobToPowerJob(job), greenSources, SERVER_JOB_CFP_PROTOCOL);

		return new InitiatePowerDeliveryForJob(agent, cfp, cnaMessage, job);
	}

	/**
	 * Method adds additional check to response handler that verifies if the server has sufficient amount of capacity
	 *
	 * @param responses   retrieved responses from Green Source Agents
	 * @param acceptances vector containing accept proposal message sent back to the chosen green source (not used)
	 */
	@Override
	protected void handleAllResponses(final Vector responses, final Vector acceptances) {
		MDC.put(MDC_JOB_ID, job.getJobId());

		myServerAgent.stoppedJobProcessing();
		if (myServerAgent.manage().getAvailableCapacity(job, null, null) < job.getPower()) {
			logger.info(NEW_JOB_LOOK_FOR_GS_NO_POWER_AVAILABLE_LOG);
			refuseToExecuteJob();
		}
		super.handleAllResponses(responses, acceptances);
	}

	/**
	 * Method logs message regarding no responses and calls generic method responsible for postprocessing the job
	 * power supply rejection.
	 */
	@Override
	protected void handleNoResponses() {
		logger.info(NEW_JOB_LOOK_FOR_GS_NO_RESPONSE_LOG);
		refuseToExecuteJob();
	}

	/**
	 * Method logs message regarding no available Green Sources and calls generic method responsible
	 * for postprocessing the job power supply rejection.
	 */
	@Override
	protected void handleNoAvailableAgents() {
		logger.info(NEW_JOB_LOOK_FOR_GS_NO_SOURCES_AVAILABLE_LOG);
		refuseToExecuteJob();
	}

	/**
	 * Method initiates behaviour responsible for making a proposal to Cloud Network Agent
	 */
	@Override
	protected void handleSelectedOffer(final GreenSourceData chosenOfferData) {
		if (myServerAgent.getServerJobs().containsKey(job)) {
			final AID chosenGreenSource = bestProposal.getSender();
			final String jobId = job.getJobId();

			MDC.put(MDC_JOB_ID, jobId);
			logger.info(NEW_JOB_LOOK_FOR_GS_SELECTED_GS_LOG, jobId, chosenGreenSource.getLocalName());

			myServerAgent.getGreenSourceForJobMap().put(jobId, chosenGreenSource);
			myServerAgent.addBehaviour(InitiateExecutionOfferForJob.create(myServerAgent, bestProposal, cnaMessage));
		} else {
			myAgent.send(prepareReply(bestProposal, jobInstance, REJECT_PROPOSAL));
			refuseToExecuteJob();
		}
	}

	private void refuseToExecuteJob() {
		myServerAgent.getServerJobs().remove(job);
		myAgent.send(prepareRefuseReply(cnaMessage));
	}
}
