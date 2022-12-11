package com.greencloud.application.agents.cloudnetwork.behaviour.df;

import static com.google.common.collect.Sets.symmetricDifference;
import static com.greencloud.application.agents.cloudnetwork.behaviour.df.logs.CloudNetworkDFLog.FOUND_NEW_SERVERS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.df.logs.CloudNetworkDFLog.FOUND_REMOVED_SERVERS_LOG;
import static com.greencloud.application.yellowpages.YellowPagesService.search;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_TYPE;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.ANNOUNCE_NETWORK_CHANGE;
import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class NetworkChangeListener extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(NetworkChangeListener.class);

	private CloudNetworkAgent myCloudNetworkAgent;

	@Override
	public void onStart() {
		super.onStart();
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
	}

	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(ANNOUNCE_NETWORK_CHANGE);

		if (Objects.nonNull(message)) {
			handleNetworkChange();
		}
	}

	private void handleNetworkChange() {
		Set<AID> serverAgents = search(myAgent, SA_SERVICE_TYPE, myAgent.getName());
		Set<AID> presentServers = new HashSet<>(myCloudNetworkAgent.getOwnedServers());
		Set<AID> symmetricDifference = symmetricDifference(serverAgents, presentServers);

		Set<AID> addedServers = symmetricDifference.stream()
				.filter(serverAgents::contains)
				.collect(toSet());
		Set<AID> removedServers = symmetricDifference.stream()
				.filter(presentServers::contains)
				.collect(toSet());

		if (!addedServers.isEmpty()) {
			handleAddedServers(addedServers);
		}

		if (!removedServers.isEmpty()) {
			handleRemovedServers(removedServers);
		}
	}

	private void handleAddedServers(Set<AID> addedServers) {
		logger.info(FOUND_NEW_SERVERS_LOG, addedServers.size());
		initializeWeights(addedServers);
		myCloudNetworkAgent.getOwnedServers().addAll(addedServers);
	}

	private void handleRemovedServers(Set<AID> removedServers) {
		logger.info(FOUND_REMOVED_SERVERS_LOG, removedServers.size());
		myCloudNetworkAgent.getOwnedServers().removeAll(removedServers);
		removedServers.forEach(myCloudNetworkAgent.manageConfig().getWeightsForServersMap()::remove);
	}

	private void initializeWeights(Set<AID> addedServers) {
		addedServers.forEach(server -> myCloudNetworkAgent.manageConfig().getWeightsForServersMap().put(server, 1));
	}
}
