package com.gui.domain.guielements;

import static com.gui.utils.GUIUtils.*;
import static com.gui.utils.domain.StyleConstants.LIGHT_GRAY_COLOR;
import static com.gui.utils.domain.StyleConstants.VERY_LIGHT_GRAY_COLOR;

import com.gui.domain.nodes.AgentNode;
import com.gui.domain.nodes.MonitoringAgentNode;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

/**
 * Class represents the details' panel of the GUI
 * <p>
 * {@value TITLE_LABEL}          title of the details panel
 */
public class DetailsPanel {

    private static final String TITLE_LABEL = "AGENT STATISTICS";
    private static final JPanel DEFAULT_INFO_PANEL = createDefaultMessagePanel();
    private static final CC DETAIL_PANEL_STYLE = new CC().height("100%").growX().spanX().gapY("10px", "10px");

    private static final int INFORMATION_PANEL_IDX = 2;
    private List<AgentNode> allNetworkAgentNodes;
    private final JPanel detailPanel;
    private final JComboBox comboBox;
    private JPanel agentDetailsPanel;

    /**
     * Class constructor
     *
     * @param agentNodes all agents nodes included in the select
     */
    public DetailsPanel(final List<AgentNode> agentNodes) {
        this.allNetworkAgentNodes = agentNodes;
        this.agentDetailsPanel = DEFAULT_INFO_PANEL;
        this.comboBox = initializeComboBox();
        this.detailPanel = createDetailsPanel();
    }

    private static JPanel createDefaultMessagePanel() {
        final JPanel jPanel = new JPanel();
        jPanel.setBackground(VERY_LIGHT_GRAY_COLOR);
        return jPanel;
    }

    /**
     * Method creates an initial details panel
     *
     * @return JPanel being details panel
     */
    public JPanel createDetailsPanel() {
        final MigLayout panelLayout = new MigLayout(new LC().fillX());
        final JPanel detailsPanel = createBorderPanel(panelLayout);
        addPanelHeader(TITLE_LABEL, detailsPanel);
        detailsPanel.add(comboBox, new CC().height("25px").growX().spanX());
        detailsPanel.add(agentDetailsPanel, DETAIL_PANEL_STYLE);
        return detailsPanel;
    }

    /**
     * Method updates the drop-down with new agent nodes
     */
    public void revalidateComboBoxModel(final List<AgentNode> agentNodes) {
        allNetworkAgentNodes = agentNodes;
        comboBox.setModel(new DefaultComboBoxModel(getDropDownNetworkAgentsNames()));
    }

    /**
     * @return details panel
     */
    public JPanel getDetailPanel() {
        return detailPanel;
    }

    private JComboBox initializeComboBox() {
        final JComboBox jComboBox = createDefaultComboBox(getDropDownNetworkAgentsNames());
        jComboBox.addActionListener(e -> changeSelectedAgent((String) jComboBox.getSelectedItem()));
        return jComboBox;
    }

    private String[] getDropDownNetworkAgentsNames() {
        final List<String> agentNames = new java.util.ArrayList<>(allNetworkAgentNodes.stream()
                                                                          .filter(agentNode -> !(agentNode instanceof MonitoringAgentNode))
                                                                          .map(AgentNode::getName)
                                                                          .toList());
        agentNames.add(0, "Please select the agent");
        return agentNames.toArray(new String[0]);
    }

    private void changeSelectedAgent(final String newAgentName) {
        agentDetailsPanel = allNetworkAgentNodes.stream()
                .filter(agent -> agent.getName().equals(newAgentName))
                .findFirst()
                .map(AgentNode::getInformationPanel)
                .orElse(DEFAULT_INFO_PANEL);
        refreshDetailsPanel();
    }

    private void refreshDetailsPanel() {
        detailPanel.remove(INFORMATION_PANEL_IDX);
        detailPanel.add(agentDetailsPanel, DETAIL_PANEL_STYLE);
        detailPanel.revalidate();
        detailPanel.repaint();
    }
}
