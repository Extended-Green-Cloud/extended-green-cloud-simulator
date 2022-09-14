package com.greencloud.application.agents.client.behaviour.df;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.behaviour.df.logs.ClientDFLog;
import com.greencloud.application.agents.client.domain.ClientAgentConstants;
import com.greencloud.application.utils.GUIUtils;
import com.greencloud.application.yellowpages.YellowPagesService;
import com.greencloud.application.yellowpages.domain.DFServiceConstants;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour finds cloud network com.greencloud.application.agents for communication
 */
public class FindCloudNetworkAgents extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(FindCloudNetworkAgents.class);
	private ClientAgent myClientAgent;

	/**
	 * Method casts the abstract agent to agent of type ClientAgent.
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myClientAgent = (ClientAgent) myAgent;
	}

	/**
	 * Method looks for Cloud Network Agents and puts the retrieved result in data store.
	 */
	@Override
	public void action() {
		final List<AID> cloudNetworkAgents = YellowPagesService.search(myAgent, DFServiceConstants.CNA_SERVICE_TYPE);
		if (cloudNetworkAgents.isEmpty()) {
			logger.info(ClientDFLog.NO_CLOUD_NETWORKS_FOUND_LOG);
			myClientAgent.doDelete();
			return;
		}
		if (!myClientAgent.isAnnounced()) {
			GUIUtils.announceNewClient(myClientAgent);
			myClientAgent.announce();
		}
		getParent().getDataStore().put(ClientAgentConstants.CLOUD_NETWORK_AGENTS, cloudNetworkAgents);
	}
}
