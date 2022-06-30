package com.gui.domain.nodes;

import static com.gui.utils.GraphUtils.addAgentEdgeToGraph;
import static com.gui.utils.domain.StyleConstants.MONITORING_STYLE;

import org.graphstream.graph.Graph;

/**
 * Agent node class representing the monitoring agent
 */
public class MonitoringAgentNode extends AgentNode {

    private final String greenEnergyAgent;

    /**
     * Monitoring node constructor
     *
     * @param name             node name
     * @param greenEnergyAgent owner green energy agent
     */
    public MonitoringAgentNode(String name, String greenEnergyAgent) {
        super(name);
        this.style = MONITORING_STYLE;
        this.greenEnergyAgent = greenEnergyAgent;
    }

    @Override
    public void createEdges(Graph graph) {
        addAgentEdgeToGraph(graph, edges, name, greenEnergyAgent);
    }

}
