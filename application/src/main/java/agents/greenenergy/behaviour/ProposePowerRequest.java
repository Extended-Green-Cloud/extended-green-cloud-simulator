package agents.greenenergy.behaviour;

import static agents.greenenergy.domain.GreenEnergyAgentConstants.MAX_ERROR_IN_JOB_FINISH;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Objects.isNull;
import static mapper.JsonMapper.getMapper;
import static messages.domain.factory.ReplyMessageFactory.prepareReply;
import static utils.GUIUtils.displayMessageArrow;
import static utils.JobMapUtils.getJobById;
import static utils.JobMapUtils.getJobByIdAndStartDate;
import static utils.TimeUtils.getCurrentTime;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import agents.greenenergy.GreenEnergyAgent;
import common.mapper.JobMapper;
import domain.job.JobInstanceIdentifier;
import domain.job.JobStatusEnum;
import domain.job.JobWithProtocol;
import domain.job.PowerJob;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

/**
 * Behaviour which is responsible for sending the proposal with power request to Server Agent and
 * handling the retrieved responses.
 */
public class ProposePowerRequest extends ProposeInitiator {

	private static final Logger logger = LoggerFactory.getLogger(ProposePowerRequest.class);
	private final String guid;
	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent agent which is executing the behaviour
	 * @param msg   proposal message that is sent to the Server Agent
	 */
	public ProposePowerRequest(final Agent agent, final ACLMessage msg) {
		super(agent, msg);
		this.myGreenEnergyAgent = (GreenEnergyAgent) agent;
		this.guid = myGreenEnergyAgent.getName();
	}

	/**
	 * Method handles the accept proposal response from server. It updates the state of the job in
	 * green source data and replies with the message containing correct protocol and the information
	 * that the execution of the given job can be started.
	 *
	 * @param accept_proposal accept proposal response retrieved from the Server Agent
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage accept_proposal) {
		final JobWithProtocol jobWithProtocol = readMessage(accept_proposal);
		if (Objects.nonNull(jobWithProtocol)) {
			PowerJob job = getJobByIdAndStartDate(myGreenEnergyAgent.getPowerJobs(), jobWithProtocol.getJobInstanceIdentifier());
			if (isNull(job)) {
				job = getJobById(myGreenEnergyAgent.getPowerJobs(), jobWithProtocol.getJobInstanceIdentifier().getJobId());
			}
			logger.info("[{}] Sending information regarding job {} back to server agent.", guid, job.getJobId());
			myGreenEnergyAgent.getPowerJobs().replace(job, JobStatusEnum.ACCEPTED);
			myAgent.addBehaviour(new FinishJobManually(myGreenEnergyAgent, calculateExpectedJobEndTime(job),
					JobMapper.mapToJobInstanceId(job)));
			displayMessageArrow(myGreenEnergyAgent, accept_proposal.getSender());
			sendResponseToServer(accept_proposal, jobWithProtocol);
		}
	}

	/**
	 * Method handles the reject proposal response from server. It logs the information to the console.
	 *
	 * @param reject_proposal reject proposal response retrieved from the Server Agent
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage reject_proposal) {
		try {
			logger.info("[{}] Server rejected the job proposal", guid);
			final JobInstanceIdentifier jobInstanceId = getMapper().readValue(reject_proposal.getContent(),
					JobInstanceIdentifier.class);
			final PowerJob powerJob = getJobByIdAndStartDate(myGreenEnergyAgent.getPowerJobs(), jobInstanceId);
			if (Objects.nonNull(powerJob)) {
				myGreenEnergyAgent.getPowerJobs().remove(powerJob);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendResponseToServer(final ACLMessage acceptProposal, final JobWithProtocol jobWithProtocol) {
		final ACLMessage response = prepareReply(acceptProposal.createReply(),
				jobWithProtocol.getJobInstanceIdentifier(), INFORM);
		response.setProtocol(jobWithProtocol.getReplyProtocol());
		myAgent.send(response);
	}

	private JobWithProtocol readMessage(ACLMessage message) {
		try {
			return getMapper().readValue(message.getContent(), JobWithProtocol.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Date calculateExpectedJobEndTime(final PowerJob job) {
		final Instant endDate = getCurrentTime().isAfter(job.getEndTime()) ? getCurrentTime() : job.getEndTime();
		return Date.from(endDate.plus(MAX_ERROR_IN_JOB_FINISH, ChronoUnit.MILLIS));
	}
}
