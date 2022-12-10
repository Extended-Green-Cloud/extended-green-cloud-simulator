package com.greencloud.application.agents.cloudnetwork.behaviour.df;

import static com.google.common.collect.Sets.symmetricDifference;
import static com.greencloud.application.yellowpages.YellowPagesService.search;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_TYPE;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;

public class NetworkChangeListener extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(NetworkChangeListener.class);

	private CloudNetworkAgent myCloudNetworkAgent;

	@Override
	public void onStart() {
		this.onStart();
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
	}

	@Override
	public void action() {
		Set<AID> serverAgents = search(myAgent, SA_SERVICE_TYPE, myAgent.getName());
		Set<AID> presentServers = new HashSet<>(myCloudNetworkAgent.getOwnedServers());

		Set<AID> symmetricDifference = symmetricDifference(serverAgents, presentServers);

		// TODO
	}
}
