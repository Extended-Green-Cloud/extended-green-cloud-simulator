package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.ADAPTATION_CONNECT_SERVER_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.ADAPTATION_INCREASE_ERROR_LOG;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CONNECT_GREEN_SOURCE_PROTOCOL;
import static jade.lang.acl.ACLMessage.REQUEST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.managingsystem.planner.AdjustGreenSourceErrorParameters;
import com.greencloud.application.agents.greenenergy.behaviour.adaptation.InitiateNewServerConnection;
import com.greencloud.commons.managingsystem.planner.ConnectGreenSourceParameters;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Set of methods used to adapt the current configuration of Green Energy agent
 */
public class GreenEnergyAdaptationManagement {

	private static final Logger logger = LoggerFactory.getLogger(GreenEnergyAdaptationManagement.class);

	private final GreenEnergyAgent greenEnergyAgent;

	/**
	 * Default constructor
	 *
	 * @param greenEnergyAgent - agent representing given source
	 */
	public GreenEnergyAdaptationManagement(GreenEnergyAgent greenEnergyAgent) {
		this.greenEnergyAgent = greenEnergyAgent;
	}

	/**
	 * Method adapts the current weather prediction error of Green Energy Agent
	 *
	 * @param params adaptation parameters
	 */
	public boolean adaptAgentWeatherPredictionError(AdjustGreenSourceErrorParameters params) {
		final double currentError = greenEnergyAgent.getWeatherPredictionError();
		final double newError = currentError + params.getPercentageChange();
		final String log = params.getPercentageChange() > 0 ? "Increasing" : "Decreasing";

		logger.info(ADAPTATION_INCREASE_ERROR_LOG, log, currentError, newError);

		greenEnergyAgent.setWeatherPredictionError(newError);
		greenEnergyAgent.manage().updateGreenSourceGUI();
		return true;
	}

	/**
	 * Method registers green source service available for a new server
	 *
	 * @param params            adaptation parameters
	 * @param adaptationMessage original adaptation message
	 */
	public void connectNewServerToGreenSource(ConnectGreenSourceParameters params, ACLMessage adaptationMessage) {
		final String serverToBeConnected = params.getServerName().split("@")[0];
		logger.info(ADAPTATION_CONNECT_SERVER_LOG, serverToBeConnected);

		final ACLMessage connectionRequest = new ACLMessage(REQUEST);
		connectionRequest.setContent(CONNECT_GREEN_SOURCE_PROTOCOL);
		connectionRequest.setProtocol(CONNECT_GREEN_SOURCE_PROTOCOL);
		connectionRequest.addReceiver(new AID(params.getServerName(), AID.ISGUID));

		greenEnergyAgent.addBehaviour(
				new InitiateNewServerConnection(greenEnergyAgent, connectionRequest, adaptationMessage,
						serverToBeConnected));
	}
}
