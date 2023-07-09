package com.gui.controller;

import java.io.Serializable;
import java.time.Instant;

import com.gui.agents.AbstractAgentNode;
import com.gui.event.domain.PowerShortageEvent;

/**
 * Controller for GUI
 */
public interface GuiController extends Runnable, Serializable {

	/**
	 * Method creates the GUI
	 */
	void run();

	/**
	 * Method adds next agent node to the graph
	 *
	 * @param agent node of the specified agent
	 */
	void addAgentNodeToGraph(final AbstractAgentNode agent);

	/**
	 * Method reports to the socket server time when the system was started
	 *
	 * @param time time of system start
	 */
	void reportSystemStartTime(final Instant time);

	/**
	 * Method updates the failed jobs number by given value
	 *
	 * @param value value to be added to the failed jobs number
	 */
	void updateFailedJobsCountByValue(final int value);

	/**
	 * Method updates the finished jobs number by given value
	 *
	 * @param value value to be added to the finished jobs number
	 */
	void updateFinishedJobsCountByValue(final int value);

	/**
	 * Method updates the number of jobs finished jobs in cloud by given value
	 *
	 * @param value value to be added to the number of jobs finished in cloud
	 */
	void updateJobsFinishedInCloudCountByValue(final int value);

	/**
	 * Method updates the clients number by given value
	 *
	 * @param value value to be added to the clients number
	 */
	void updateClientsCountByValue(final int value);

	/**
	 * Method updates the active jobs number by given value
	 *
	 * @param value value to be added to the active jobs number
	 */
	void updateActiveJobsCountByValue(final int value);

	/**
	 * Method updates the number of jobs active in cloud
	 *
	 * @param value value to be added to the number of jobs active in cloud
	 */
	void updateActiveInCloudJobsCountByValue(final int value);

	/**
	 * Method updates the all jobs number by given value
	 *
	 * @param value value to be added to the all jobs number
	 */
	void updateAllJobsCountByValue(final int value);

	/**
	 * Method triggers the power shortage event in the Cloud Network for specified agent
	 *
	 * @param powerShortageEvent data for event triggering
	 * @param agentName          name of the agent for which the event is being triggered
	 */
	void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent, final String agentName);

}
