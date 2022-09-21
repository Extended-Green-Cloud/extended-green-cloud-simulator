package com.gui.graph;

import static com.gui.graph.domain.GraphConfigurationConstants.LAYOUT_EDGE_LENGTH_PARAMETER;
import static com.gui.graph.domain.GraphConfigurationConstants.LAYOUT_MAX_ITERATIONS;
import static com.gui.graph.domain.GraphConfigurationConstants.LAYOUT_NODE_AREA;
import static com.gui.graph.domain.GraphConfigurationConstants.TIMER_DELAY_MILLISECONDS;
import static com.gui.graph.domain.GraphExceptionConstants.EDGE_NOT_FOUND;
import static com.gui.graph.domain.GraphExceptionConstants.INCORRECT_EDGE_TYPE;
import static com.gui.graph.domain.GraphExceptionConstants.NODES_NOT_FOUND;
import static com.gui.graph.domain.GraphExceptionConstants.NODE_NOT_FOUND;
import static com.gui.graph.domain.GraphStyleConstants.CLOUD_NETWORK_INACTIVE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.CLOUD_NETWORK_NODE_SIZE;
import static com.gui.graph.domain.GraphStyleConstants.CONNECTOR_EDGE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.GREEN_ENERGY_NODE_SIZE;
import static com.gui.graph.domain.GraphStyleConstants.GREEN_SOURCE_INACTIVE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.MESSAGE_EDGE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.MESSAGE_HIDDEN_EDGE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.MONITORING_NODE_SIZE;
import static com.gui.graph.domain.GraphStyleConstants.MONITORING_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.SERVER_INACTIVE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.SERVER_NODE_SIZE;

import com.gui.agents.AbstractAgentNode;
import com.gui.agents.CloudNetworkAgentNode;
import com.gui.agents.GreenEnergyAgentNode;
import com.gui.agents.ServerAgentNode;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;

public class GraphServiceImpl implements GraphService {

    private static final Logger logger = LoggerFactory.getLogger(GraphServiceImpl.class);

    private final mxGraph graph;
    private final mxGraphComponent graphComponent;
    private final mxOrganicLayout graphLayout;
    private final GraphStyleService graphStyleService;

    /**
     * Default constructor which initializes the mxGraph
     */
    public GraphServiceImpl(final Dimension graphDimension) {
        this.graph = new mxGraph();
        this.graphComponent = new mxGraphComponent(graph);
        this.graphLayout = new mxOrganicLayout(graph, getLayoutBounds(graphDimension));
        this.graphStyleService = new GraphStyleServiceImpl();
    }

    @Override
    public mxGraphComponent createGraphComponent() {
        configureGraphPermissions();
        configureGraphLayout();
        graphStyleService.addStyleSheetToGraph(graph);
        return graphComponent;
    }

    @Override
    public void createAndAddNodeToGraph(final AbstractAgentNode node) {
        final Pair<Integer, String> vertexProperties = getVertexPropertiesForAgentNode(node);
        final Integer vertexSize = vertexProperties.getKey();
        final String vertexStyle = vertexProperties.getValue();
        final String vertexValue = node.getAgentName();

        graph.getModel().beginUpdate();
        try {
            graph.insertVertex(graph.getDefaultParent(), vertexValue, vertexValue, 0, 0, vertexSize, vertexSize, vertexStyle);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    @Override
    public void removeNodeFromGraph(final AbstractAgentNode node) {
        final Object vertexToRemove = ((mxGraphModel) graph.getModel()).getCell(node.getAgentName());

        if (Objects.nonNull(vertexToRemove)) {
            graph.getModel().beginUpdate();
            try {
                graph.removeCells(new Object[]{vertexToRemove}, true);
            } finally {
                graph.getModel().endUpdate();
            }
        } else {
            logger.error(NODE_NOT_FOUND, node.getAgentName());
        }
    }

    @Override
    public void updateNodeStyle(final String node, final String styleName) {
        final Object vertexToUpdate = ((mxGraphModel) graph.getModel()).getCell(node);
        if (Objects.nonNull(vertexToUpdate)) {
            graphStyleService.changeGraphElementStylesheet(vertexToUpdate, graph, styleName);
        } else {
            logger.error(NODE_NOT_FOUND, node);
        }
    }

    @Override
    public void createAndAddEdgeToGraph(final String nodeSource, final String nodeTarget, final boolean isDirected) {
        final Pair<Object, Object> vertices = getSourceAndTargetVertices(nodeSource, nodeTarget);

        if (Objects.nonNull(vertices)) {
            final String edgeName = isDirected ?
                    String.join("_", nodeSource, nodeTarget) :
                    String.join("_", nodeSource, nodeTarget, "BI");
            final String edgeStyle = isDirected ? MESSAGE_HIDDEN_EDGE_STYLE : CONNECTOR_EDGE_STYLE;

            graph.getModel().beginUpdate();
            try {
                graph.insertEdge(graph.getDefaultParent(), edgeName, "", vertices.getFirst(), vertices.getSecond(), edgeStyle);
            } finally {
                graph.getModel().endUpdate();
            }
        } else {
            logger.error(NODES_NOT_FOUND, nodeSource, nodeTarget);
        }
    }

    @Override
    public void updateEdgeStyle(final String sourceNode, final String targetNode, final boolean isDirected, final String styleName) {
        final String edgeName = isDirected ?
                String.join("_", sourceNode, targetNode) :
                String.join("_", sourceNode, targetNode, "BI");
        final mxCell edgeToUpdate = (mxCell) ((mxGraphModel) graph.getModel()).getCell(edgeName);

        if (Objects.nonNull(edgeToUpdate)) {
            if (edgeToUpdate.isEdge()) {
                graphStyleService.changeGraphElementStylesheet(edgeToUpdate, graph, styleName);
            } else {
                logger.info(INCORRECT_EDGE_TYPE);
            }
        } else {
            logger.error(EDGE_NOT_FOUND, edgeName);
        }
    }


    @Override
    public void displayMessageEdges(final String sourceNode, final List<String> targetNodes) {
        targetNodes.forEach(target -> updateEdgeStyle(sourceNode, target, true, MESSAGE_EDGE_STYLE));
        final ActionListener hideMessageArrowAction = e -> {
            targetNodes.forEach(target -> updateEdgeStyle(sourceNode, target, true, MESSAGE_HIDDEN_EDGE_STYLE));
            graph.refresh();
        };
        final Timer hideMessageArrowTimer = new Timer(TIMER_DELAY_MILLISECONDS, hideMessageArrowAction);
        hideMessageArrowTimer.start();
    }

    @Override
    public void updateGraphLayout() {
        graph.getModel().beginUpdate();
        try {
            graphLayout.execute(graph.getDefaultParent());
        } finally {
            graph.getModel().endUpdate();
        }
    }

    private Rectangle2D getLayoutBounds(final Dimension graphDimension) {
        final Rectangle2D layoutBounds = new Rectangle2D.Double();
        layoutBounds.setRect(0, 0, graphDimension.width, graphDimension.height);
        return layoutBounds;
    }

    private void configureGraphPermissions() {
        graph.setCellsEditable(false);
        graph.setCellsResizable(false);
        graph.setCellsSelectable(false);
        graph.setCellsBendable(false);
        graph.setCellsDeletable(false);
        graph.setCellsMovable(false);
        graph.setKeepEdgesInBackground(true);
        graph.setMultigraph(true);
        graph.setAutoSizeCells(true);
        graph.setEnabled(false);
        graph.setAllowLoops(true);
        graph.setAllowNegativeCoordinates(false);
        graphComponent.setAntiAlias(true);
        graphComponent.scrollToCenter(true);
        graphComponent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        graphComponent.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    private void configureGraphLayout() {
        graphLayout.setAverageNodeArea(LAYOUT_NODE_AREA);
        graphLayout.setOptimizeEdgeCrossing(true);
        graphLayout.setOptimizeBorderLine(true);
        graphLayout.setOptimizeEdgeLength(true);
        graphLayout.setFineTuning(true);
        graphLayout.setMaxIterations(LAYOUT_MAX_ITERATIONS);
        graphLayout.setEdgeLengthCostFactor(LAYOUT_EDGE_LENGTH_PARAMETER);
    }

    private Pair<Integer, String> getVertexPropertiesForAgentNode(final AbstractAgentNode node) {
        if (node instanceof CloudNetworkAgentNode) {
            return Pair.create(CLOUD_NETWORK_NODE_SIZE, CLOUD_NETWORK_INACTIVE_STYLE);
        } else if (node instanceof ServerAgentNode) {
            return Pair.create(SERVER_NODE_SIZE, SERVER_INACTIVE_STYLE);
        } else if (node instanceof GreenEnergyAgentNode) {
            return Pair.create(GREEN_ENERGY_NODE_SIZE, GREEN_SOURCE_INACTIVE_STYLE);
        } else {
            return Pair.create(MONITORING_NODE_SIZE, MONITORING_STYLE);
        }
    }


    private Pair<Object, Object> getSourceAndTargetVertices(final String node1Name, final String node2Name) {
        final Object sourceVertex = ((mxGraphModel) graph.getModel()).getCell(node1Name);
        final Object targetVertex = ((mxGraphModel) graph.getModel()).getCell(node2Name);
        return Objects.isNull(sourceVertex) || Objects.isNull(targetVertex) ? null : Pair.create(sourceVertex, targetVertex);
    }
}
