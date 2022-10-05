package com.greencloud.application.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;

import jade.core.AID;

/**
 * Class defines set of utilities used together with GUI Controller
 */
public class GUIUtils {

	/**
	 * Method updates the GUI to indicate that the job execution has finished
	 *
	 * @param agent agent updating the GUI
	 */
	public static void announceFinishedJob(final CloudNetworkAgent agent) {
		agent.getGuiController().updateActiveJobsCountByValue(-1);
		agent.getGuiController().updateAllJobsCountByValue(-1);
	}

	/**
	 * Method updates the GUI to indicate that a new job is planned to be executed
	 *
	 * @param agent agent updating the GUI
	 */
	public static void announceBookedJob(final AbstractAgent agent) {
		agent.getGuiController().updateAllJobsCountByValue(1);
	}

	/**
	 * Method updates the GUI to indicate that new client is using Cloud Network
	 *
	 * @param agent agent updating the GUI
	 */
	public static void announceNewClient(final AbstractAgent agent) {
		agent.getGuiController().updateClientsCountByValue(1);
	}

	/**
	 * Method displays the message arrow in GUI
	 *
	 * @param agent    agent being the sender
	 * @param receiver address of the receiver
	 */
	public static void displayMessageArrow(final AbstractAgent agent, final AID receiver) {
		agent.getGuiController()
				.displayMessageArrow(agent.getAgentNode(), Collections.singletonList(receiver.getLocalName()));
	}

	/**
	 * Method displays the message arrow in GUI
	 *
	 * @param agent     agent being the sender
	 * @param receivers addresses of the receivers
	 */
	public static void displayMessageArrow(final AbstractAgent agent, final List<AID> receivers) {
		agent.getGuiController()
				.displayMessageArrow(agent.getAgentNode(), receivers.stream().map(AID::getLocalName)
						.toList());
	}

	/**
	 * Method displays the message arrow in GUI
	 *
	 * @param agent     agent being the sender
	 * @param receivers iterator of addresses of the receivers
	 */
	public static void displayMessageArrow(final AbstractAgent agent, final Iterator receivers) {
		final Iterable<AID> iterable = () -> receivers;
		final Stream<AID> stream = StreamSupport.stream(iterable.spliterator(), false);
		agent.getGuiController().displayMessageArrow(agent.getAgentNode(), stream.map(AID::getLocalName).toList());
	}
}
