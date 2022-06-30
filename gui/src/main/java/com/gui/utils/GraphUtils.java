package com.gui.utils;

import static com.gui.utils.domain.StyleConstants.*;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import java.util.List;

/**
 * Class provides set of utilities connected with graph methods which are used by multiple classes
 */
public class GraphUtils {

    /**
     * Method creates edge based on given agents
     *
     * @param graph  graph to which the edge is added
     * @param agent1 name of the first agent
     * @param agent2 name of the second agent
     */
    public static void addAgentEdgeToGraph(final Graph graph,
                                           final List<Edge> nodeEdges,
                                           final String agent1,
                                           final String agent2) {
        final String edgeName = String.join("_", agent1, agent2);
        final Edge edge = graph.addEdge(edgeName, agent1, agent2, true);
        edge.setAttribute("ui.class", EDGE_HIDDEN_MESSAGE_STYLE);
        nodeEdges.add(edge);
    }

    /**
     * Method creates bidirectional edge based on given agents
     *
     * @param graph  graph to which the edge is added
     * @param agent1 name of the first agent
     * @param agent2 name of the second agent
     */
    public static void addAgentBidirectionalEdgeToGraph(final Graph graph,
                                                        final List<Edge> nodeEdges,
                                                        final String agent1,
                                                        final String agent2) {
        final String biDirectionEdgeName = String.join("_", agent1, agent2, "BI");
        final Edge edge = graph.addEdge(biDirectionEdgeName, agent1, agent2, false);
        edge.setAttribute("ui.class", EDGE_INACTIVE_STYLE);
        edge.setAttribute("layout.weight", 4);
        nodeEdges.add(edge);
    }

    /**
     * Method updates edge style between two agents when the given network component becomes active
     *
     * @param edges    list of edges
     * @param isActive is component active status
     */
    public static void updateActiveEdgeStyle(final List<Edge> edges, final boolean isActive, final String agent1, final String agent2) {
        final String dynamicEdgeStyle = isActive ? EDGE_ACTIVE_STYLE : EDGE_INACTIVE_STYLE;
        final String edgeName = String.join("_", agent1, agent2, "BI");
        edges.stream().filter(edge -> edge.getId().equals(edgeName)).forEach(edge -> edge.setAttribute("ui.class", dynamicEdgeStyle));
    }
}
