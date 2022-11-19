package com.greencloud.application.agents.cloudnetwork.behaviour.df;

import static com.greencloud.application.agents.cloudnetwork.behaviour.df.logs.CloudNetworkDFLog.NO_SCHEDULER_FOUND_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.df.logs.CloudNetworkDFLog.NO_SERVERS_FOUND_LOG;
import static com.greencloud.application.yellowpages.YellowPagesService.search;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_TYPE;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_TYPE;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviours finds corresponding Server Agents for given Cloud Network Agent in DF
 */
public class FindSchedulerAndServerAgents extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(FindSchedulerAndServerAgents.class);

	private CloudNetworkAgent myCloudNetworkAgent;

	/**
	 * Method casts the agent to the agent of type CloudNetworkAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
	}

	/**
	 * Method searches the Directory Facilitator for the corresponding to the given Cloud Network Agent servers
	 */
	@Override
	public void action() {
		final List<AID> serverAgents = search(myAgent, SA_SERVICE_TYPE, myAgent.getName());
		final List<AID> schedulerAgents = search(myAgent, SCHEDULER_SERVICE_TYPE);

		if (serverAgents.isEmpty()) {
			logger.info(NO_SERVERS_FOUND_LOG);
			myCloudNetworkAgent.doDelete();
		}
		if (schedulerAgents.isEmpty()) {
			logger.info(NO_SCHEDULER_FOUND_LOG);
			myCloudNetworkAgent.doDelete();
		}

		myCloudNetworkAgent.setOwnedServers(serverAgents);
		myCloudNetworkAgent.setScheduler(schedulerAgents.get(0));
		initializeWeights();
	}

	private void initializeWeights() {
		myCloudNetworkAgent.getOwnedServers().forEach(
				server -> myCloudNetworkAgent.manageConfig().getWeightsForServersMap().put(server, 1));
	}
}
