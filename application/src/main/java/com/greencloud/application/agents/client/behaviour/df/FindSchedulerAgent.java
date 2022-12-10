package com.greencloud.application.agents.client.behaviour.df;

import static com.greencloud.application.agents.client.domain.ClientAgentConstants.SCHEDULER_AGENT;
import static com.greencloud.application.utils.GUIUtils.announceNewClient;
import static com.greencloud.application.yellowpages.YellowPagesService.search;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.behaviour.df.logs.ClientDFLog;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour finds Scheduler Agent for communication
 */
public class FindSchedulerAgent extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(FindSchedulerAgent.class);
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
	 * Method looks for Scheduler Agent and puts the retrieved result in data store.
	 */
	@Override
	public void action() {
		final List<AID> schedulerAgents = new ArrayList<>(search(myAgent, SCHEDULER_SERVICE_TYPE));
		if (schedulerAgents.isEmpty()) {
			logger.info(ClientDFLog.NO_SCHEDULER_FOUND_LOG);
			myClientAgent.doDelete();
			return;
		}
		if (!myClientAgent.isAnnounced()) {
			announceNewClient(myClientAgent);
			myClientAgent.announce();
		}
		getParent().getDataStore().put(SCHEDULER_AGENT, schedulerAgents.get(0));
	}
}
