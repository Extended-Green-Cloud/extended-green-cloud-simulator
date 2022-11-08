package com.greencloud.application.utils;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;

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
}
