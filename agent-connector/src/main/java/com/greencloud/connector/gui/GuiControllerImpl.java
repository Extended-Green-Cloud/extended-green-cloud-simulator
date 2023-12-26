package com.greencloud.connector.gui;

import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getClientsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getCloudNetworkSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getManagingSystemSocket;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.event.DisableServerEvent;
import org.greencloud.gui.event.EnableServerEvent;
import org.greencloud.gui.event.PowerShortageEvent;
import org.greencloud.gui.event.ServerMaintenanceEvent;
import org.greencloud.gui.event.WeatherDropEvent;
import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.greencloud.gui.messages.domain.ImmutableGreenSourceCreator;
import org.greencloud.gui.messages.domain.ImmutableServerCreator;
import org.greencloud.gui.messages.domain.ServerCreator;
import org.greencloud.gui.websocket.WebSocketConnections;
import org.greencloud.gui.websocket.enums.SocketTypeEnum;
import org.greencloud.rulescontroller.ruleset.domain.ModifyAgentRuleSetEvent;

import com.greencloud.connector.factory.AgentControllerFactory;

public class GuiControllerImpl implements GuiController {

	private final EventListener eventSocket;

	public GuiControllerImpl(final Map<SocketTypeEnum, String> hostUris) {
		WebSocketConnections.initialize(hostUris);
		eventSocket = new EventListener(
				URI.create(hostUris.get(SocketTypeEnum.EVENTS_WEB_SOCKET) + "event"));
	}

	@Override
	public void run() {
		WebSocketConnections.connect();
		eventSocket.connect();
	}

	@Override
	public void connectWithAgentFactory(final AgentControllerFactory factory) {
		eventSocket.connectWithAgentFactory(factory);
	}

	@Override
	public void reportSystemStartTime(final Instant time) {
		getAgentsWebSocket().reportSystemStartTime(time);
		getClientsWebSocket().reportSystemStartTime(time);
		getManagingSystemSocket().reportSystemStartTime(time);
		getCloudNetworkSocket().reportSystemStartTime(time);
		eventSocket.reportSystemStartTime(time);
	}

	@Override
	public void addAgentNodeToGraph(final EGCSNode agent) {
		if (Objects.nonNull(agent)) {
			eventSocket.addAgentNode(agent);
			agent.addToGraph();
		}
	}

	@Override
	public void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent) {
		eventSocket.triggerPowerShortage(powerShortageEvent);
	}

	@Override
	public void triggerWeatherDropEvent(final WeatherDropEvent weatherDropEvent) {
		eventSocket.triggerWeatherDrop(weatherDropEvent);
	}

	@Override
	public void disableServerEvent(final DisableServerEvent disableServerEvent) {
		eventSocket.switchServerOff(disableServerEvent);
	}

	@Override
	public void enableServerEvent(final EnableServerEvent enableServerEvent) {
		eventSocket.switchServerOn(enableServerEvent);
	}

	@Override
	public void modifySystemRuleSetEvent(final ModifyAgentRuleSetEvent modifyAgentRuleSetEvent) {
		eventSocket.modifyRuleSet(modifyAgentRuleSetEvent);
	}

	@Override
	public void modifyServerResources(final ServerMaintenanceEvent serverMaintenanceEvent) {
		eventSocket.performServerMaintenance(serverMaintenanceEvent);
	}

	@Override
	public void createNewServerEvent(final String name, final String regionalManager, final double maxPower,
			final double idlePower, final Map<String, Resource> resources, final long jobProcessingLimit,
			final double price) {
		final ServerCreator serverCreator = ImmutableServerCreator.builder()
				.regionalManager(regionalManager)
				.name(name)
				.occurrenceTime(Instant.now())
				.isFinished(false)
				.resources(resources)
				.maxPower(maxPower)
				.idlePower(idlePower)
				.jobProcessingLimit(jobProcessingLimit)
				.price(price)
				.build();

		eventSocket.createNewServer(serverCreator);
	}

	@Override
	public void createNewGreenSourceEvent(final String name, final String server, final double latitude,
			final double longitude, final double pricePerPowerUnit, final double predictionError,
			final long maxCapacity, final GreenEnergySourceTypeEnum greenEnergyType) {
		final GreenSourceCreator greenSourceCreator = ImmutableGreenSourceCreator.builder()
				.occurrenceTime(Instant.now())
				.name(name)
				.server(server)
				.longitude(longitude)
				.latitude(latitude)
				.pricePerPowerUnit(pricePerPowerUnit)
				.weatherPredictionError(predictionError)
				.maximumCapacity(maxCapacity)
				.energyType(greenEnergyType)
				.build();

		eventSocket.createNewGreenSource(greenSourceCreator);
	}
}
