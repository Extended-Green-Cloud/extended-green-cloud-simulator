package agents.server.behaviour.jobexecution.initiator;

import static agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_NO_POWER_AVAILABLE_LOG;
import static agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_NO_RESPONSE_LOG;
import static agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_NO_SOURCES_AVAILABLE_LOG;
import static agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_SELECTED_GS_LOG;
import static messages.MessagingUtils.readMessageContent;
import static messages.MessagingUtils.rejectJobOffers;
import static messages.MessagingUtils.retrieveProposals;
import static messages.MessagingUtils.retrieveValidMessages;
import static messages.domain.factory.OfferMessageFactory.makeServerJobOffer;
import static messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import domain.job.ClientJob;
import mapper.JobMapper;
import domain.GreenSourceData;
import domain.job.JobStatusEnum;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Behaviour searches for available green source that can supply new job with power
 */
public class InitiatePowerDeliveryForJob extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiatePowerDeliveryForJob.class);

	private final ACLMessage replyMessage;
	private final ServerAgent myServerAgent;
	private final String guid;
	private final ClientJob job;

	/**
	 * Behaviour constructor
	 *
	 * @param agent        agent which executed the behaviour
	 * @param powerRequest call for proposal containing the details regarding power needed to execute the job
	 * @param replyMessage reply message sent to cloud network after receiving the green sources' responses
	 */
	public InitiatePowerDeliveryForJob(final Agent agent, final ACLMessage powerRequest, final ACLMessage replyMessage,
			final ClientJob job) {
		super(agent, powerRequest);
		this.replyMessage = replyMessage;
		this.job = job;
		this.myServerAgent = (ServerAgent) myAgent;
		this.guid = myServerAgent.getName();
	}

	/**
	 * Method waits for all Green Source Agent responses.
	 * It analyzes received proposals, selects the Green Source Agent which will be the power supplier for new job
	 * and rejects the remaining ones.
	 *
	 * @param responses   retrieved responses from Green Source Agents
	 * @param acceptances vector containing accept proposal message sent back to the chosen green source (not used)
	 */
	@Override
	protected void handleAllResponses(Vector responses, Vector acceptances) {
		final List<ACLMessage> proposals = retrieveProposals(responses);

		myServerAgent.stoppedJobProcessing();
		if (responses.isEmpty()) {
			logger.info(NEW_JOB_LOOK_FOR_GS_NO_RESPONSE_LOG, guid);
			refuseToExecuteJob(proposals);
		} else if (proposals.isEmpty()) {
			logger.info(NEW_JOB_LOOK_FOR_GS_NO_SOURCES_AVAILABLE_LOG, guid);
			refuseToExecuteJob(proposals);
		} else if (myServerAgent.manage().getAvailableCapacity(job.getStartTime(), job.getEndTime(), null, null)
				<= job.getPower()) {
			logger.info(NEW_JOB_LOOK_FOR_GS_NO_POWER_AVAILABLE_LOG, guid);
			refuseToExecuteJob(proposals);
		} else {
			final List<ACLMessage> validProposals = retrieveValidMessages(proposals, GreenSourceData.class);
			final boolean isJobStillProcessed = myServerAgent.getServerJobs()
					.replace(myServerAgent.manage().getJobByIdAndStartDate(job.getJobId(), job.getStartTime()),
							JobStatusEnum.PROCESSING, JobStatusEnum.ACCEPTED);

			if (!validProposals.isEmpty() && isJobStillProcessed) {
				final ACLMessage chosenGreenSourceOffer = myServerAgent.chooseGreenSourceToExecuteJob(validProposals);
				proposeServerOffer(chosenGreenSourceOffer);
				rejectJobOffers(myServerAgent, JobMapper.mapToJobInstanceId(job), chosenGreenSourceOffer, proposals);
			} else {
				handleInvalidProposals(proposals);
			}
		}
		myServerAgent.removeBehaviour(this);
	}

	private void proposeServerOffer(final ACLMessage chosenOffer) {
		final AID chosenGreenSource = chosenOffer.getSender();
		final GreenSourceData offerData = readMessageContent(chosenOffer, GreenSourceData.class);
		final String jobId = offerData.getJobId();

		logger.info(NEW_JOB_LOOK_FOR_GS_SELECTED_GS_LOG, guid, jobId, chosenGreenSource.getLocalName());

		final double servicePrice = myServerAgent.manage().calculateServicePrice(offerData);
		final ACLMessage proposalMessage = makeServerJobOffer(myServerAgent, servicePrice, jobId, replyMessage);
		myServerAgent.getGreenSourceForJobMap().put(jobId, chosenGreenSource);
		myServerAgent.addBehaviour(
				new InitiateExecutionOfferForJob(myAgent, proposalMessage, chosenOffer.createReply()));
	}

	private void handleInvalidProposals(final List<ACLMessage> proposals) {
		rejectJobOffers(myServerAgent, JobMapper.mapToJobInstanceId(job), null, proposals);
		refuseToExecuteJob(proposals);
	}

	private void refuseToExecuteJob(final List<ACLMessage> proposals) {
		myServerAgent.getServerJobs().remove(job);
		myAgent.send(prepareRefuseReply(replyMessage));
		rejectJobOffers(myServerAgent, JobMapper.mapToJobInstanceId(job), null, proposals);
	}
}
