package com.greencloud.application.agents.greenenergy.behaviour.adaptation;

import static com.greencloud.application.agents.greenenergy.behaviour.adaptation.logs.AdaptationSourceLog.DEACTIVATION_FAILED_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.adaptation.logs.AdaptationSourceLog.DEACTIVATION_FINISH_REMAIN_JOBS_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.adaptation.logs.AdaptationSourceLog.DEACTIVATION_SUCCEEDED_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.adaptation.logs.AdaptationSourceLog.INITIATE_GREEN_SOURCE_DISCONNECTION_LOG;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DEACTIVATE_GREEN_SOURCE_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareFailureReply;
import static jade.lang.acl.ACLMessage.REQUEST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.message.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour requests in the selected server that a given Green Source should be deactivated
 */
public class InitiateGreenSourceDeactivation extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateGreenSourceDeactivation.class);

	private final GreenEnergyAgent myGreenAgent;
	private final ACLMessage adaptationMessage;

	private InitiateGreenSourceDeactivation(GreenEnergyAgent agent, ACLMessage deactivationMessage) {
		super(agent, deactivationMessage);
		this.myGreenAgent = agent;
		this.adaptationMessage = agent.adapt().getGreenSourceDisconnectionState().getOriginalAdaptationMessage();
	}

	/**
	 * Method creating a behaviour
	 *
	 * @param greenEnergyAgent  green source executing the behaviour
	 * @param server            server to which the message is sent
	 * @return new InitiateGreenSourceDeactivation behaviour
	 */
	public static InitiateGreenSourceDeactivation create(final GreenEnergyAgent greenEnergyAgent, String server) {
		final ACLMessage deactivationMessage = MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withMessageProtocol(DEACTIVATE_GREEN_SOURCE_PROTOCOL)
				.withStringContent(DEACTIVATE_GREEN_SOURCE_PROTOCOL)
				.withReceivers(new AID(server, AID.ISGUID))
				.build();
		return new InitiateGreenSourceDeactivation(greenEnergyAgent, deactivationMessage);
	}

	/**
	 * Method handles the REFUSE response retrieved from Server informing that the Green Source is not connected
	 * to the given server
	 *
	 * @param refuse retrieved response
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		logger.info(DEACTIVATION_FAILED_LOG, refuse.getSender().getName());
		myGreenAgent.adapt().getGreenSourceDisconnectionState().setBeingDisconnected(false);
		myGreenAgent.send(prepareFailureReply(adaptationMessage.createReply()));
	}

	/**
	 * Method handles the INFORM response retrieved from Server informing that the Green Source was successfully
	 * deactivated
	 *
	 * @param inform retrieved response
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		logger.info(DEACTIVATION_SUCCEEDED_LOG, inform.getSender().getName());

		final long serverJobsLeftCount = myGreenAgent.getServerJobs().keySet().stream()
				.filter(job -> job.getServer().equals(inform.getSender()))
				.count();

		if (serverJobsLeftCount > 0) {
			logger.info(DEACTIVATION_FINISH_REMAIN_JOBS_LOG, serverJobsLeftCount);
			myGreenAgent.adapt().getGreenSourceDisconnectionState().setServerToBeDisconnected(inform.getSender());
		} else {
			logger.info(INITIATE_GREEN_SOURCE_DISCONNECTION_LOG);
			myGreenAgent.addBehaviour(InitiateGreenSourceDisconnection.create(myGreenAgent, inform.getSender()));
		}
	}

}
