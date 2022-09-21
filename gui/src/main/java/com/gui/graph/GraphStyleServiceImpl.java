package com.gui.graph;

import static com.gui.graph.domain.GraphStyleConstants.CLOUD_NETWORK_HIGH_TRAFFIC_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.CLOUD_NETWORK_INACTIVE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.CLOUD_NETWORK_LOW_TRAFFIC_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.CLOUD_NETWORK_MEDIUM_TRAFFIC_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.CONNECTOR_EDGE_ACTIVE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.CONNECTOR_EDGE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.GREEN_SOURCE_ACTIVE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.GREEN_SOURCE_INACTIVE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.GREEN_SOURCE_ON_HOLD_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.MESSAGE_EDGE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.MESSAGE_HIDDEN_EDGE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.MONITORING_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.SERVER_ACTIVE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.SERVER_BACK_UP_POWER_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.SERVER_INACTIVE_STYLE;
import static com.gui.graph.domain.GraphStyleConstants.SERVER_ON_HOLD_STYLE;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getCloudNetworkHighTrafficStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getCloudNetworkInactiveStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getCloudNetworkLowTrafficStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getCloudNetworkMediumTrafficStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getConnectorActiveEdgeStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getConnectorInactiveEdgeStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getGreenEnergyActiveStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getGreenEnergyInactiveStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getGreenEnergyOnHoldStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getMessageEdgeStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getMessageHiddenEdgeStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getMonitoringStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getServerActiveStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getServerInactiveStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getServerOnBackUpStyleSheet;
import static com.gui.graph.domain.GraphStyleSheetsFactory.getServerOnHoldStyleSheet;

import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import java.util.HashMap;
import java.util.Map;

public class GraphStyleServiceImpl implements GraphStyleService {

    @Override
    public void addStyleSheetToGraph(mxGraph graph) {
        final mxStylesheet graphStylesheet = graph.getStylesheet();
        final Map<String, Map<String, Object>> styleSheetMap = createStyleSheetMap();
        styleSheetMap.forEach(graphStylesheet::putCellStyle);
    }

    @Override
    public void changeGraphElementStylesheet(final Object element, final mxGraph graph, String newStyle) {
        graph.getModel().beginUpdate();
        try {
            graph.setCellStyle(newStyle, new Object[]{element});
        } catch (ArrayIndexOutOfBoundsException e) {
            return;
        } finally {
            graph.getModel().endUpdate();
        }
    }

    private Map<String, Map<String, Object>> createStyleSheetMap() {
        final Map<String, Map<String, Object>> styleSheetMap = new HashMap<>();
        styleSheetMap.put(CLOUD_NETWORK_INACTIVE_STYLE, getCloudNetworkInactiveStyleSheet());
        styleSheetMap.put(CLOUD_NETWORK_LOW_TRAFFIC_STYLE, getCloudNetworkLowTrafficStyleSheet());
        styleSheetMap.put(CLOUD_NETWORK_MEDIUM_TRAFFIC_STYLE, getCloudNetworkMediumTrafficStyleSheet());
        styleSheetMap.put(CLOUD_NETWORK_HIGH_TRAFFIC_STYLE, getCloudNetworkHighTrafficStyleSheet());
        styleSheetMap.put(SERVER_INACTIVE_STYLE, getServerInactiveStyleSheet());
        styleSheetMap.put(SERVER_ACTIVE_STYLE, getServerActiveStyleSheet());
        styleSheetMap.put(SERVER_ON_HOLD_STYLE, getServerOnHoldStyleSheet());
        styleSheetMap.put(SERVER_BACK_UP_POWER_STYLE, getServerOnBackUpStyleSheet());
        styleSheetMap.put(GREEN_SOURCE_ACTIVE_STYLE, getGreenEnergyActiveStyleSheet());
        styleSheetMap.put(GREEN_SOURCE_INACTIVE_STYLE, getGreenEnergyInactiveStyleSheet());
        styleSheetMap.put(GREEN_SOURCE_ON_HOLD_STYLE, getGreenEnergyOnHoldStyleSheet());
        styleSheetMap.put(MONITORING_STYLE, getMonitoringStyleSheet());
        styleSheetMap.put(CONNECTOR_EDGE_STYLE, getConnectorInactiveEdgeStyleSheet());
        styleSheetMap.put(CONNECTOR_EDGE_ACTIVE_STYLE, getConnectorActiveEdgeStyleSheet());
        styleSheetMap.put(MESSAGE_EDGE_STYLE, getMessageEdgeStyleSheet());
        styleSheetMap.put(MESSAGE_HIDDEN_EDGE_STYLE, getMessageHiddenEdgeStyleSheet());
        return styleSheetMap;
    }
}
