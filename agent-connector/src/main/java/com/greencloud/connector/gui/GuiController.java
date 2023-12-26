package com.greencloud.connector.gui;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.event.DisableServerEvent;
import org.greencloud.gui.event.EnableServerEvent;
import org.greencloud.gui.event.PowerShortageEvent;
import org.greencloud.gui.event.ServerMaintenanceEvent;
import org.greencloud.gui.event.WeatherDropEvent;
import org.greencloud.rulescontroller.ruleset.domain.ModifyAgentRuleSetEvent;

import com.greencloud.connector.factory.AgentControllerFactory;

/**
 * Controller for GUI
 */
public interface GuiController extends Runnable, Serializable {

	/**
	 * Method creates the GUI
	 */
	void run();

	/**
	 * Method connects GUI with agent factory
	 */
	void connectWithAgentFactory(final AgentControllerFactory factory);

	/**
	 * Method adds next agent node to the graph
	 *
	 * @param agent node of the specified agent
	 */
	void addAgentNodeToGraph(final EGCSNode agent);

	/**
	 * Method reports to the socket server time when the system was started
	 *
	 * @param time time of system start
	 */
	void reportSystemStartTime(final Instant time);

	/**
	 * Method triggers the power shortage event for specified agent
	 *
	 * @param powerShortageEvent data for event triggering
	 */
	void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent);

	/**
	 * Method triggers the weather drop event in the given Regional Manager
	 *
	 * @param weatherDropEvent data for event triggering
	 */
	void triggerWeatherDropEvent(final WeatherDropEvent weatherDropEvent);

	/**
	 * Method triggers server disabling event
	 *
	 * @param disableServerEvent data for event triggering
	 */
	void disableServerEvent(final DisableServerEvent disableServerEvent);

	/**
	 * Method triggers server enabling event
	 *
	 * @param enableServerEvent data for event triggering
	 */
	void enableServerEvent(final EnableServerEvent enableServerEvent);

	/**
	 * Method changes the rule set of selected system region
	 *
	 * @param modifyAgentRuleSetEvent data for event triggering
	 */
	void modifySystemRuleSetEvent(final ModifyAgentRuleSetEvent modifyAgentRuleSetEvent);

	/**
	 * Method modifies the resources of the selected server
	 *
	 * @param serverMaintenanceEvent data for event triggering
	 */
	void modifyServerResources(final ServerMaintenanceEvent serverMaintenanceEvent);

	/**
	 * Method used to create new server in the network
	 *
	 * @param name               name of the server that is to be created
	 * @param regionalManager    name of the regional manager with which the server is to be connected
	 * @param maxPower           maximal server power
	 * @param idlePower          idle server power
	 * @param resources          resources of the server
	 * @param jobProcessingLimit number of jobs that can be processed at a single time
	 * @param price              price of the job execution
	 */
	void createNewServerEvent(final String name,
			final String regionalManager,
			final double maxPower,
			final double idlePower,
			final Map<String, Resource> resources,
			final long jobProcessingLimit,
			final double price);

	/**
	 * Method used to create new green source in the network
	 *
	 * @param name              name of the server that is to be created
	 * @param latitude          latitude of the green source location
	 * @param longitude         longitude of the green source location
	 * @param pricePerPowerUnit price per single energy supply unit
	 * @param predictionError   error associated with weather prediction
	 * @param maxCapacity       maximum capacity of green source
	 * @param greenEnergyType   type of the energy source
	 */
	void createNewGreenSourceEvent(final String name,
			final String server,
			final double latitude,
			final double longitude,
			final double pricePerPowerUnit,
			final double predictionError,
			final long maxCapacity,
			final GreenEnergySourceTypeEnum greenEnergyType);

}
