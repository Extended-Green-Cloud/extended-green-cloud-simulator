package com.greencloud.application.agents.cloudnetwork.behaviour.df.subscribe;

import static com.greencloud.application.agents.cloudnetwork.behaviour.df.subscribe.logs.CloudNetworkDFSubscribeLog.FOUND_NEW_SERVERS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.df.subscribe.logs.CloudNetworkDFSubscribeLog.FOUND_REMOVED_SERVERS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.df.subscribe.logs.CloudNetworkDFSubscribeLog.SUBSCRIBE_SERVER_SERVICE_LOG;
import static com.greencloud.application.yellowpages.YellowPagesService.prepareSubscription;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.behaviours.df.AbstractSubscriptionInitiator;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour subscribes the Server service in DF
 */
public class SubscribeServerService extends AbstractSubscriptionInitiator {

	private static final Logger logger = getLogger(SubscribeServerService.class);

	private final CloudNetworkAgent myCloudNetworkAgent;

	private SubscribeServerService(final CloudNetworkAgent agent, ACLMessage subscription) {
		super(agent, subscription);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent cloud network subscribing to DF
	 * @return SubscribeServerService
	 */
	public static SubscribeServerService create(final CloudNetworkAgent agent) {
		logger.info(SUBSCRIBE_SERVER_SERVICE_LOG);
		return new SubscribeServerService(agent, prepareSubscription(agent, agent.getDefaultDF(),
				SA_SERVICE_TYPE, agent.getName()));
	}

	@Override
	protected void handleAddedAgents(final Map<AID, Boolean> addedAgents) {
		logger.info(FOUND_NEW_SERVERS_LOG, addedAgents.size());
		initializeWeights(addedAgents.keySet());
		myCloudNetworkAgent.getOwnedServers().putAll(addedAgents);
	}

	@Override
	protected void handleRemovedAgents(final Map<AID, Boolean> removedAgents) {
		logger.info(FOUND_REMOVED_SERVERS_LOG, removedAgents.size());
		myCloudNetworkAgent.getOwnedServers().entrySet().removeIf(server -> removedAgents.containsKey(server.getKey()));
		removedAgents.forEach(myCloudNetworkAgent.getWeightsForServersMap()::remove);
	}

	private void initializeWeights(Set<AID> addedServers) {
		addedServers.forEach(server -> myCloudNetworkAgent.getWeightsForServersMap().put(server, 1));
	}
}
