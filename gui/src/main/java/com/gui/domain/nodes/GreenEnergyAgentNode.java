package com.gui.domain.nodes;

import static com.gui.utils.GUIUtils.*;
import static com.gui.utils.GraphUtils.*;
import static com.gui.utils.domain.StyleConstants.*;

import com.gui.domain.Location;
import com.gui.domain.types.AgentNodeLabelEnum;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import java.util.List;

/**
 * Agent node class representing the green energy source
 */
public class GreenEnergyAgentNode extends AgentNode {

    private boolean isActive;
    private final Location location;
    private final double maximumCapacity;
    private double traffic;
    private int numberOfExecutedJobs;
    private final String monitoringAgent;
    private final String serverAgent;

    /**
     * Green energy source node constructor
     *
     * @param name            node name
     * @param maximumCapacity maximum capacity of green source
     * @param serverAgent     owner server name
     * @param monitoringAgent connected monitoring agent
     */
    public GreenEnergyAgentNode(String name, double maximumCapacity, String monitoringAgent, String serverAgent, Location location) {
        super(name);
        this.maximumCapacity = maximumCapacity;
        this.location = location;
        this.serverAgent = serverAgent;
        this.monitoringAgent = monitoringAgent;
        this.style = GREEN_ENERGY_STYLE;
        this.isActive = false;
        this.traffic = 0;
        this.numberOfExecutedJobs = 0;
        initializeLabelsMap();
        createInformationPanel();
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

    /**
     * Function updates the information if the given green source is active
     *
     * @param isActive information if the green source is active
     */
    public synchronized void updateIsActive(final boolean isActive) {
        this.isActive = isActive;
        labelsMap.get(AgentNodeLabelEnum.IS_ACTIVE_LABEL).setText(formatToHTML(isActive ? "ACTIVE" : "INACTIVE"));
        updateGraphUI();
    }

    @Override
    public void updateGraphUI() {
        final String dynamicStyle = isActive ? GREEN_ENERGY_ACTIVE_STYLE : GREEN_ENERGY_INACTIVE_STYLE;
        node.setAttribute("ui.class", concatenateStyles(List.of(LABEL_STYLE, style, dynamicStyle)));
        updateActiveEdgeStyle(edges, isActive, name, serverAgent);
    }

    @Override
    public void createEdges(final Graph graph) {
        addAgentBidirectionalEdgeToGraph(graph, edges, name, serverAgent);
        addAgentBidirectionalEdgeToGraph(graph, edges, name, monitoringAgent);
        addAgentEdgeToGraph(graph, edges, name, monitoringAgent);
        addAgentEdgeToGraph(graph, edges, name, serverAgent);
    }

    @Override
    protected void initializeLabelsMap() {
        super.initializeLabelsMap();
        labelsMap.put(AgentNodeLabelEnum.IS_ACTIVE_LABEL, createListLabel(isActive ? "ACTIVE" : "INACTIVE"));
        labelsMap.put(AgentNodeLabelEnum.LOCATION_LATITUDE_LABEL, createListLabel(location.getLatitude()));
        labelsMap.put(AgentNodeLabelEnum.LOCATION_LONGITUDE_LABEL, createListLabel(location.getLongitude()));
        labelsMap.put(AgentNodeLabelEnum.MAXIMUM_CAPACITY_LABEL, createListLabel(String.valueOf(maximumCapacity)));
        labelsMap.put(AgentNodeLabelEnum.TRAFFIC_LABEL, createListLabel(String.valueOf(traffic)));
        labelsMap.put(AgentNodeLabelEnum.NUMBER_OF_EXECUTED_JOBS_LABEL, createListLabel(String.valueOf(numberOfExecutedJobs)));
    }
}
