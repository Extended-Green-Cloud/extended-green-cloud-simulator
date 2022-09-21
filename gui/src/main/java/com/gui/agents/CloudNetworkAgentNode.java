package com.gui.agents;

import static com.gui.graph.domain.GraphStyleConstants.CLOUD_NETWORK_HIGH_TRAFFIC_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.CLOUD_NETWORK_LOW_TRAFFIC_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.CLOUD_NETWORK_MEDIUM_TRAFFIC_STYLE;
import static com.gui.gui.utils.GUILabelUtils.createListLabel;
import static com.gui.gui.utils.GUILabelUtils.formatToHTML;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.gui.graph.domain.GraphStyleConstants;
import com.gui.gui.panels.domain.listlabels.AgentDetailsPanelListLabelEnum;

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
	 * @param serverAgents    list of server com.greencloud.application.agents names
	 */
	public CloudNetworkAgentNode(String name, double maximumCapacity, List<String> serverAgents) {
		super(name);
		this.serverAgents = serverAgents;
		this.maximumCapacity = new AtomicReference<>(maximumCapacity);
		this.traffic = new AtomicReference<>(0D);
		this.totalNumberOfClients = new AtomicInteger(0);
		this.totalNumberOfExecutedJobs = new AtomicInteger(0);
		initializeLabelsMap();
		createInformationPanel();
	}

	/**
	 * Function updates the number of clients to given value
	 *
	 * @param value value indicating the client number
	 */
	public void updateClientNumber(final int value) {
		this.totalNumberOfClients.set(value);
		agentDetailLabels.get(AgentDetailsPanelListLabelEnum.TOTAL_NUMBER_OF_CLIENTS_LABEL)
				.setText(formatToHTML(String.valueOf(totalNumberOfClients)));
	}

	/**
	 * Function updates the current traffic
	 *
	 * @param powerInUse current power in use
	 */
	public void updateTraffic(final double powerInUse) {
		this.traffic.set((powerInUse / maximumCapacity.get()) * 100);
		agentDetailLabels.get(AgentDetailsPanelListLabelEnum.TRAFFIC_LABEL)
				.setText(formatToHTML(String.format("%.2f%%", traffic.get())));
		updateGraphUI();
	}

	/**
	 * Function updates the number of currently executed jobs to given value
	 *
	 * @param value new jobs count value
	 */
	public void updateJobsCount(final int value) {
		this.totalNumberOfExecutedJobs.set(value);
		agentDetailLabels.get(AgentDetailsPanelListLabelEnum.NUMBER_OF_EXECUTED_JOBS_LABEL)
				.setText(formatToHTML(String.valueOf(totalNumberOfExecutedJobs)));
	}

	@Override
	public void updateGraphUI() {
		if (traffic.get() > 85) {
			graphService.updateNodeStyle(agentName, CLOUD_NETWORK_HIGH_TRAFFIC_STYLE);
		} else if (traffic.get() > 50) {
			graphService.updateNodeStyle(agentName, CLOUD_NETWORK_MEDIUM_TRAFFIC_STYLE);
		} else if (traffic.get() > 0) {
			graphService.updateNodeStyle(agentName, CLOUD_NETWORK_LOW_TRAFFIC_STYLE);
		} else {
			graphService.updateNodeStyle(agentName, GraphStyleConstants.CLOUD_NETWORK_INACTIVE_STYLE);
		}
	}

	@Override
	public void createEdges() {
		serverAgents.forEach(serverName -> graphService.createAndAddEdgeToGraph(agentName, serverName, true));
	}

	@Override
	public void initializeLabelsMap() {
		agentDetailLabels.put(AgentDetailsPanelListLabelEnum.SERVERS_NUMBER_LABEL,
				createListLabel(String.valueOf(serverAgents.size())));
		agentDetailLabels.put(AgentDetailsPanelListLabelEnum.CURRENT_MAXIMUM_CAPACITY_LABEL,
				createListLabel(String.valueOf(maximumCapacity)));
		agentDetailLabels.put(AgentDetailsPanelListLabelEnum.TRAFFIC_LABEL,
				createListLabel(String.format("%.2f%%", traffic.get())));
		agentDetailLabels.put(AgentDetailsPanelListLabelEnum.TOTAL_NUMBER_OF_CLIENTS_LABEL,
				createListLabel(String.valueOf(totalNumberOfClients)));
		agentDetailLabels.put(AgentDetailsPanelListLabelEnum.NUMBER_OF_EXECUTED_JOBS_LABEL,
				createListLabel(String.valueOf(totalNumberOfExecutedJobs)));
	}
}
