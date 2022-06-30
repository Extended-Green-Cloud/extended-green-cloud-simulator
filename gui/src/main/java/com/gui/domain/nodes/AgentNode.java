package com.gui.domain.nodes;

import static com.gui.utils.GUIUtils.*;
import static com.gui.utils.domain.StyleConstants.LABEL_STYLE;

import com.gui.domain.types.LabelEnum;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import javax.swing.*;
import java.util.*;

/**
 * Class represents abstract agent node
 */
public class AgentNode {

    protected String name;
    protected String style;
    protected Node node;
    protected List<Edge> edges;
    protected JPanel informationPanel;
    protected Map<LabelEnum, JLabel> labelsMap;

    /**
     * Class constructor
     *
     * @param name name of the agent
     */
    public AgentNode(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }

    /**
     * Abstract method responsible for adding the node to the graph
     *
     * @param graph graph to which the node is to be added
     * @return added node
     */
    public Node addToGraph(final Graph graph){
        final Node newNode = graph.addNode(name);
        newNode.setAttribute("ui.label", newNode.getId());
        newNode.setAttribute("ui.class", concatenateStyles(List.of(LABEL_STYLE, style)));
        this.node = newNode;
        this.updateGraphUI();
        return newNode;
    }

    /**
     * Abstract method responsible for creating edges for given node
     *
     * @param graph graph for which the edges are to be created
     */
    public void createEdges(final Graph graph) { }

    /**
     * Abstract method which based on the agent status creates the JPanel displaying all data
     */
    public void createInformationPanel() {
        final JPanel panel = createLabelListPanel(labelsMap);
        this.informationPanel = panel;
    }

    /**
     * Abstract method responsible for updating graph style based on the internal state of agent node
     */
    public void updateGraphUI() {}

    /**
     * Abstract method used to initialize labels map for given agent node
     */
    protected void initializeLabelsMap() {
        this.labelsMap = new LinkedHashMap<>();
    }

    /**
     * @return agent node name
     */
    public String getName() {
        return name;
    }

    /**
     * @return list of node edges
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * @return information panel of agent node
     */
    public JPanel getInformationPanel() {
        return informationPanel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentNode agentNode = (AgentNode) o;
        return name.equals(agentNode.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
