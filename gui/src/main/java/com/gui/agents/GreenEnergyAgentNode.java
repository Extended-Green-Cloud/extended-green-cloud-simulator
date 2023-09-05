package com.gui.agents;

import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static java.lang.Double.parseDouble;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.util.Optional;

import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.greenenergy.ImmutableGreenEnergyNodeArgs;
import com.greencloud.commons.domain.location.ImmutableLocation;
import com.greencloud.commons.domain.location.Location;
import com.gui.event.domain.PowerShortageEvent;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.message.ImmutableUpdateServerConnectionMessage;
import com.gui.message.domain.ImmutableServerConnection;

/**
 * Agent node class representing the green energy source
 */
public class GreenEnergyAgentNode extends AbstractNetworkAgentNode implements Serializable {

	private Location location;
	private GreenEnergyAgentArgs greenEnergyAgentArgs;

	public GreenEnergyAgentNode() {
		super();
	}

	/**
	 * Green energy source node constructor
	 *
	 * @param args arguments provided for green energy agent creation
	 */
	public GreenEnergyAgentNode(GreenEnergyAgentArgs args) {
		super(args.getName());
		this.location = new ImmutableLocation(parseDouble(args.getLatitude()), parseDouble(args.getLongitude()));
		this.greenEnergyAgentArgs = args;
	}

	@Override
	public void addToGraph() {
		getAgentsWebSocket().send(ImmutableRegisterAgentMessage.builder()
				.agentType("GREEN_ENERGY")
				.data(ImmutableGreenEnergyNodeArgs.builder()
						.monitoringAgent(greenEnergyAgentArgs.getMonitoringAgent())
						.serverAgent(greenEnergyAgentArgs.getOwnerSever())
						.maximumCapacity(greenEnergyAgentArgs.getMaximumCapacity())
						.name(agentName)
						.agentLocation(location)
						.energyType(greenEnergyAgentArgs.getEnergyType())
						.pricePerPower(greenEnergyAgentArgs.getPricePerPowerUnit())
						.weatherPredictionError(greenEnergyAgentArgs.getWeatherPredictionError() * 100)
						.build())
				.build());
	}

	/**
	 * Function updates current value of weather prediction error
	 *
	 * @param value new weather prediction error value
	 */
	public void updatePredictionError(final double value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value * 100)
				.agentName(agentName)
				.type("SET_WEATHER_PREDICTION_ERROR")
				.build());
	}

	/**
	 * Function updates currently supplied energy amount
	 *
	 * @param energy currently supplied energy
	 */
	public void updateEnergyInUse(final double energy) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(energy)
				.agentName(agentName)
				.type("UPDATE_ENERGY_IN_USE")
				.build());
	}

	/**
	 * Function updates the amount of available green energy for given agent
	 *
	 * @param value amount of available green energy
	 */
	public void updateGreenEnergyAmount(final double value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_AVAILABLE_GREEN_ENERGY")
				.build());
	}

	/**
	 * Function updates in the GUI the connection state for given server
	 *
	 * @param serverName  name of the server connected/disconnected to Green Source
	 * @param isConnected flag indicating if the server should be connected/disconnected
	 */
	public void updateServerConnection(final String serverName, final boolean isConnected) {
		getAgentsWebSocket().send(ImmutableUpdateServerConnectionMessage.builder()
				.agentName(this.agentName)
				.data(ImmutableServerConnection.builder()
						.isConnected(isConnected)
						.serverName(serverName)
						.build())
				.build());

	}

	public Optional<PowerShortageEvent> getEvent() {
		return ofNullable((PowerShortageEvent) eventsQueue.poll());
	}
}
