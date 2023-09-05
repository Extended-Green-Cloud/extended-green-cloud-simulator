package com.greencloud.application.agents.client.behaviour.df;

import static com.greencloud.application.agents.client.behaviour.df.logs.ClientDFLog.NO_SCHEDULER_FOUND_LOG;
import static com.greencloud.application.agents.client.constants.ClientAgentConstants.SCHEDULER_AGENT;
import static com.greencloud.application.utils.GuiUtils.announceNewClient;
import static com.greencloud.application.yellowpages.YellowPagesService.search;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.greencloud.application.agents.client.ClientAgent;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour finds Scheduler Agent for communication
 */
public class FindSchedulerAgent extends OneShotBehaviour {

	private static final Logger logger = getLogger(FindSchedulerAgent.class);
	private final ClientAgent myClientAgent;

	/**
	 * Behaviour constructor
	 *
	 * @param clientAgent - agent executing the behaviour
	 */
	public FindSchedulerAgent(final ClientAgent clientAgent) {
		super(clientAgent);
		this.myClientAgent = clientAgent;
	}

	/**
	 * Method looks for Scheduler Agent and puts the retrieved result in data store.
	 */
	@Override
	public void action() {
		final List<AID> schedulerAgents = new ArrayList<>(search(myClientAgent, myClientAgent.getParentDFAddress(),
				SCHEDULER_SERVICE_TYPE));

		if (schedulerAgents.isEmpty()) {
			logger.info(NO_SCHEDULER_FOUND_LOG);
			myClientAgent.doDelete();
		} else {

			if (!myClientAgent.isAnnounced()) {
				announceNewClient(myClientAgent);
				myClientAgent.announce();
			}
			getParent().getDataStore().put(SCHEDULER_AGENT, schedulerAgents.get(0));
		}
	}
}
