package com.greencloud.application.agents.cloudnetwork.behaviour.df.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.df.initiator.logs.CloudNetworkDFInitiatorLog.UPDATE_MAX_CAPACITY_LOG;
import static com.greencloud.application.utils.MessagingUtils.retrieveForPerformative;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.ASK_FOR_POWER_PROTOCOL;
import static com.greencloud.application.utils.PowerUtils.updateAgentMaximumCapacity;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.Double.parseDouble;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.domain.CloudNetworkPowerUpdateEnum;
import com.greencloud.commons.message.MessageBuilder;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour sends a request to the given servers asking for information regarding maximum capacity
 * provided by their services
 */
public class InitiateCapacityUpdate extends AchieveREInitiator {

	private static final Logger logger = getLogger(InitiateCapacityUpdate.class);

	private final CloudNetworkAgent myCloudNetwork;
	private final CloudNetworkPowerUpdateEnum powerUpdateType;

	private InitiateCapacityUpdate(final Agent agent, final ACLMessage msg,
			final CloudNetworkPowerUpdateEnum powerUpdateType) {
		super(agent, msg);
		this.myCloudNetwork = (CloudNetworkAgent) agent;
		this.powerUpdateType = powerUpdateType;
	}

	/**
	 * Method creates InitiateCapacityUpdate behaviour
	 *
	 * @param cloudNetworkAgent agent executing the behaviour
	 * @param serversToAsk      servers that are to be asked for power
	 * @param powerUpdateType   flag defining a way in which Cloud Network should update its power
	 * @return InitiateCapacityUpdate
	 */
	public static InitiateCapacityUpdate create(final CloudNetworkAgent cloudNetworkAgent,
			final Set<AID> serversToAsk, final CloudNetworkPowerUpdateEnum powerUpdateType) {
		final ACLMessage message = MessageBuilder.builder()
				.withMessageProtocol(ASK_FOR_POWER_PROTOCOL)
				.withStringContent(ASK_FOR_POWER_PROTOCOL)
				.withPerformative(REQUEST)
				.withReceivers(serversToAsk)
				.build();
		return new InitiateCapacityUpdate(cloudNetworkAgent, message, powerUpdateType);
	}

	/**
	 * Method handles the messages containing the information regarding servers maximum capacities
	 *
	 * @param resultNotifications vector of retrieved results
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void handleAllResultNotifications(final Vector resultNotifications) {
		final Collection<ACLMessage> informs = retrieveForPerformative((Vector<ACLMessage>) resultNotifications,
				INFORM);

		logger.info(UPDATE_MAX_CAPACITY_LOG);
		updateAgentMaximumCapacity(getNewCapacity(informs), myCloudNetwork);
	}

	private double getNewCapacity(final Collection<ACLMessage> informs) {
		final double capacity = informs.stream().map(msg -> parseDouble(msg.getContent())).reduce(0.0, Double::sum);

		return switch (powerUpdateType) {
			case UPDATE_ALL -> capacity;
			case DECREMENT_CAPACITY -> myCloudNetwork.getMaximumCapacity() - capacity;
			case INCREMENT_CAPACITY -> myCloudNetwork.getMaximumCapacity() + capacity;
		};
	}
}
