package agents.server.behaviour.jobexecution.listener;

import static agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SERVER_NEW_JOB_LACK_OF_POWER_LOG;
import static agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SERVER_NEW_JOB_LOOK_FOR_SOURCE_LOG;
import static agents.server.behaviour.jobexecution.listener.templates.JobHandlingMessageTemplates.NEW_JOB_CFP_TEMPLATE;
import static messages.MessagingUtils.readMessageContent;
import static messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static messages.domain.factory.CallForProposalMessageFactory.createCallForProposal;
import static messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;
import static utils.GUIUtils.displayMessageArrow;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import agents.server.behaviour.jobexecution.initiator.InitiatePowerDeliveryForJob;
import mapper.JobMapper;
import domain.job.ClientJob;
import domain.job.JobStatusEnum;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles upcoming job's CFP from cloud network agents
 */
public class ListenForNewJob extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForNewJob.class);

	private ServerAgent myServerAgent;
	private String guid;

	/**
	 * Method casts the agent to the ServerAgent.
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
		this.guid = myServerAgent.getName();
	}

	/**
	 * Method listens for the upcoming job CFP coming from the Cloud Network Agents.
	 * It validates whether the server has enough power to handle the job.
	 * If yes, then it sends the CFP to owned green sources to find available power sources.
	 * If no, then it sends the refuse message to the Cloud Network Agent.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(NEW_JOB_CFP_TEMPLATE);

		if (Objects.nonNull(message)) {
			final ClientJob job = readMessageContent(message, ClientJob.class);
			final int availableCapacity = myServerAgent.manage()
					.getAvailableCapacity(job.getStartTime(), job.getEndTime(), null, null);
			final boolean validJobConditions = job.getPower() <= availableCapacity &&
					!myServerAgent.getServerJobs().containsKey(job) &&
					myServerAgent.canTakeIntoProcessing();

			if (validJobConditions) {
				initiateNegotiationWithPowerSources(job, message);
			} else {
				logger.info(SERVER_NEW_JOB_LACK_OF_POWER_LOG, guid);
				displayMessageArrow(myServerAgent, message.getSender());
				myAgent.send(prepareRefuseReply(message.createReply()));
			}
		} else {
			block();
		}
	}

	private void initiateNegotiationWithPowerSources(final ClientJob job, final ACLMessage cnaMessage) {
		logger.info(SERVER_NEW_JOB_LOOK_FOR_SOURCE_LOG, guid);
		myServerAgent.getServerJobs().putIfAbsent(job, JobStatusEnum.PROCESSING);
		myServerAgent.tookJobIntoProcessing();

		final ACLMessage cfp = createCallForProposal(JobMapper.mapJobToPowerJob(job),
				myServerAgent.getOwnedGreenSources(), SERVER_JOB_CFP_PROTOCOL);

		displayMessageArrow(myServerAgent, myServerAgent.getOwnedGreenSources());
		myAgent.addBehaviour(new InitiatePowerDeliveryForJob(myAgent, cfp, cnaMessage.createReply(), job));
	}
}
