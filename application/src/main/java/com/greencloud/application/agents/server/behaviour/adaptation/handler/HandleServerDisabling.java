package com.greencloud.application.agents.server.behaviour.adaptation.handler;

import static com.greencloud.application.agents.server.behaviour.adaptation.handler.logs.ServerAdaptationHandlerLog.DISABLING_COMPLETED_LOG;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.greencloud.application.agents.server.ServerAgent;
import com.gui.agents.ServerAgentNode;

import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour completes action of the server disabling
 */
public class HandleServerDisabling extends OneShotBehaviour {

	private static final Logger logger = getLogger(HandleServerDisabling.class);

	private ServerAgent myServerAgent;

	/**
	 * Method casts abstract agent to agent of type Server Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method sends the information to parent Cloud Network that confirms that the Server was fully disabled
	 */
	@Override
	public void action() {
		logger.info(DISABLING_COMPLETED_LOG);
		((ServerAgentNode) myServerAgent.getAgentNode()).disableServer();
	}
}
