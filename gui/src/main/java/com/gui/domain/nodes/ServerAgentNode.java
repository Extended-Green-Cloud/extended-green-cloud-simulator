package com.gui.domain.nodes;


import static com.gui.utils.GUIUtils.*;
import static com.gui.utils.GraphUtils.*;
import static com.gui.utils.domain.StyleConstants.*;

import com.gui.domain.types.AgentNodeLabelEnum;
import org.graphstream.graph.Graph;

import java.util.List;

/**
 * Agent node class representing the server
 */
public class ServerAgentNode extends AgentNode {

    private final double maximumCapacity;
    private final String cloudNetworkAgent;
    private final List<String> greenEnergyAgents;
    private boolean isActive;
    private double traffic;
    private int totalNumberOfClients;
    private int numberOfExecutedJobs;

    /**
     * Server node constructor
     *
     * @param name              node name
     * @param maximumCapacity   maximum server capacity
     * @param cloudNetworkAgent name of the owner cloud network
     * @param greenEnergyAgents names of owned green sources
     */
    public ServerAgentNode(String name, double maximumCapacity, String cloudNetworkAgent, List<String> greenEnergyAgents) {
        super(name);
        this.style = SERVER_STYLE;
        this.isActive = false;
        this.maximumCapacity = maximumCapacity;
        this.cloudNetworkAgent = cloudNetworkAgent;
        this.totalNumberOfClients = 0;
        this.numberOfExecutedJobs = 0;
        this.greenEnergyAgents = greenEnergyAgents;
        initializeLabelsMap();
        createInformationPanel();
    }

    /**
     * Function updates the information if the given server is active
     *
     * @param isActive information if the server is active
     */
    public synchronized void updateIsActive(final boolean isActive) {
        this.isActive = isActive;
        labelsMap.get(AgentNodeLabelEnum.IS_ACTIVE_LABEL).setText(formatToHTML(isActive ? "ACTIVE" : "INACTIVE"));
        updateGraphUI();
    }

    /**
     * Function updates the number of clients by given value
     *
     * @param value value to be added to client number
     */
    public synchronized void updateClientNumber(final int value) {
        this.totalNumberOfClients += value;
        labelsMap.get(AgentNodeLabelEnum.TOTAL_NUMBER_OF_CLIENTS_LABEL).setText(formatToHTML(String.valueOf(totalNumberOfClients)));
    }

    /**
     * Function updates the current traffic
     *
     * @param powerInUse current power in use
     */
    public synchronized void updateTraffic(final double powerInUse) {
        this.traffic = (powerInUse / maximumCapacity) * 100;
        labelsMap.get(AgentNodeLabelEnum.TRAFFIC_LABEL).setText(formatToHTML(String.valueOf(traffic)));
        updateGraphUI();
    }

    /**
     * Function updates the number of currently executed jobs by given value
     *
     * @param value value to be added to the number of jobs being executed
     */
    public synchronized void updateJobsCount(final int value) {
        this.numberOfExecutedJobs += value;
        labelsMap.get(AgentNodeLabelEnum.NUMBER_OF_EXECUTED_JOBS_LABEL).setText(formatToHTML(String.valueOf(numberOfExecutedJobs)));
    }

    @Override
    public void updateGraphUI() {
        final String dynamicNodeStyle = isActive ? SERVER_ACTIVE_STYLE : SERVER_INACTIVE_STYLE;
        node.setAttribute("ui.class", concatenateStyles(List.of(LABEL_STYLE, style, dynamicNodeStyle)));
        updateActiveEdgeStyle(edges, isActive, name, cloudNetworkAgent);
    }

    @Override
    public void createEdges(final Graph graph) {
        addAgentBidirectionalEdgeToGraph(graph, edges, name, cloudNetworkAgent);
        greenEnergyAgents.forEach(greenEnergyName -> addAgentEdgeToGraph(graph, edges, name, greenEnergyName));
        addAgentEdgeToGraph(graph, edges, name, cloudNetworkAgent);
    }

    @Override
    protected void initializeLabelsMap() {
        super.initializeLabelsMap();
        labelsMap.put(AgentNodeLabelEnum.IS_ACTIVE_LABEL, createListLabel(isActive ? "ACTIVE" : "INACTIVE"));
        labelsMap.put(AgentNodeLabelEnum.MAXIMUM_CAPACITY_LABEL, createListLabel(String.valueOf(maximumCapacity)));
        labelsMap.put(AgentNodeLabelEnum.TRAFFIC_LABEL, createListLabel(String.valueOf(traffic)));
        labelsMap.put(AgentNodeLabelEnum.TOTAL_NUMBER_OF_CLIENTS_LABEL, createListLabel(String.valueOf(totalNumberOfClients)));
        labelsMap.put(AgentNodeLabelEnum.NUMBER_OF_EXECUTED_JOBS_LABEL, createListLabel(String.valueOf(numberOfExecutedJobs)));
    }
}
