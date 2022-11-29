package com.gui.agents;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.greencloud.commons.args.agent.cloudnetwork.ImmutableCloudNetworkNodeArgs;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the cloud network
 */
public class CloudNetworkAgentNode extends AbstractAgentNode {

	private final List<String> serverAgents;
	private final AtomicReference<Double> maximumCapacity;
	private final AtomicReference<Double> traffic;
	private final AtomicInteger totalNumberOfClients;
	private final AtomicInteger totalNumberOfExecutedJobs;

	/**
	 * Cloud network node constructor
	 *
	 * @param name            name of the node
	 * @param maximumCapacity maximum capacity of cloud network
	 * @param serverAgents    list of server agents names
	 */
	public CloudNetworkAgentNode(String name, double maximumCapacity, List<String> serverAgents) {
		super(name);
		this.serverAgents = serverAgents;
		this.maximumCapacity = new AtomicReference<>(maximumCapacity);
		this.traffic = new AtomicReference<>(0D);
		this.totalNumberOfClients = new AtomicInteger(0);
		this.totalNumberOfExecutedJobs = new AtomicInteger(0);

	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
		webSocketClient.send(ImmutableRegisterAgentMessage.builder()
				.agentType("CLOUD_NETWORK")
				.data(ImmutableCloudNetworkNodeArgs.builder()
						.name(agentName)
						.serverAgents(serverAgents)
						.maximumCapacity(maximumCapacity.get())
						.build())
				.build());
	}

	/**
	 * Function updates the number of clients to given value
	 *
	 * @param value value indicating the client number
	 */
	public void updateClientNumber(final int value) {
		this.totalNumberOfClients.set(value);
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_CLIENT_NUMBER")
				.build());
	}

	/**
	 * Function updates the current traffic
	 *
	 * @param powerInUse current power in use
	 */
	public void updateTraffic(final double powerInUse) {
		this.traffic.set((powerInUse / maximumCapacity.get()) * 100);
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.type("SET_TRAFFIC")
				.agentName(agentName)
				.data(powerInUse)
				.build());
	}

	/**
	 * Function updates the number of currently executed jobs to given value
	 *
	 * @param value new jobs count value
	 */
	public void updateJobsCount(final int value) {
		this.totalNumberOfExecutedJobs.set(value);
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.type("SET_JOBS_COUNT")
				.agentName(agentName)
				.data(value)
				.build());
	}

	/**
	 * Function updates the current job success ratio of a cloud network
	 *
	 * @param value new success ratio
	 */
	public void updateCurrentJobSuccessRatio(final double value) {
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.type("SET_JOB_SUCCESS_RATIO")
				.agentName(agentName)
				.data(value * 100)
				.build());
	}
}
