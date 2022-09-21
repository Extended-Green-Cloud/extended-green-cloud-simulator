package com.gui.gui.panels;

import static com.gui.gui.panels.domain.PanelConstants.ADMIN_PANEL_AGENTS_TYPE_LABEL;
import static com.gui.gui.panels.domain.PanelConstants.ADMIN_PANEL_COMBO_BOX_ATTRIBUTES;
import static com.gui.gui.panels.domain.PanelConstants.ADMIN_PANEL_DEFAULT_EMPTY_PANEL_TITLE;
import static com.gui.gui.panels.domain.PanelConstants.ADMIN_PANEL_DEFAULT_PANEL_LAYOUT;
import static com.gui.gui.panels.domain.PanelConstants.ADMIN_PANEL_EVENT_PANEL_ATTRIBUTES;
import static com.gui.gui.panels.domain.PanelConstants.ADMIN_PANEL_SEPARATOR_ATTRIBUTES;
import static com.gui.gui.panels.domain.PanelConstants.ADMIN_PANEL_TITLE;
import static com.gui.gui.panels.domain.PanelConstants.IS_NETWORK_AGENT;
import static com.gui.gui.utils.GUIComponentUtils.createDefaultAgentComboBox;
import static com.gui.gui.utils.GUIComponentUtils.createDefaultAgentComboBoxModel;
import static com.gui.gui.utils.GUIComponentUtils.createSeparator;
import static com.gui.gui.utils.GUIContainerUtils.createBorderPanel;
import static com.gui.gui.utils.GUIContainerUtils.createDefaultEmptyPanel;
import static com.gui.gui.utils.GUILabelUtils.addPanelHeader;
import static com.gui.gui.utils.domain.GUIStyleConstants.GRAY_2_COLOR;
import static java.util.Objects.nonNull;

import java.awt.CardLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.gui.agents.AbstractAgentNode;
import com.gui.event.EventGUIService;
import com.gui.event.EventGUIServiceImpl;

/**
 * Panel providing functionalities to the administrator to cause events impacting the environment
 */
public class AdminPanel {

	private final JPanel mainPanel;
	private final List<AbstractAgentNode> allNetworkAgentNodes;
	private final Map<AbstractAgentNode, Component> networkAgentNodesEventPanels;
	private final JComboBox agentComboBox;
	private final EventGUIService eventGUIService;
	private JPanel eventPanel;

	/**
	 * Default constructor
	 */
	public AdminPanel() {
		this.eventGUIService = new EventGUIServiceImpl();
		this.allNetworkAgentNodes = new ArrayList<>();
		this.networkAgentNodesEventPanels = initializeAgentEventPanelMap();
		this.agentComboBox = createDefaultAgentComboBox(allNetworkAgentNodes, e -> changeSelectedNetworkAgent(),
				ADMIN_PANEL_AGENTS_TYPE_LABEL);
		this.mainPanel = initializeAdminPanel();
	}

	/**
	 * @return administrator panel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * Method updates the network drop-down with new agent nodes
	 */
	public void revalidateComboBoxModel(final AbstractAgentNode modifiedAgentNode, final boolean doDelete) {
		if (IS_NETWORK_AGENT.test(modifiedAgentNode)) {
			if (doDelete) {
				allNetworkAgentNodes.remove(modifiedAgentNode);
				networkAgentNodesEventPanels.remove(modifiedAgentNode);
				eventPanel.remove(getEventPanelByName(modifiedAgentNode.getAgentName()));
			} else {
				final Component newAgentEventPanel = eventGUIService.createEventPanelForAgent(modifiedAgentNode);
				allNetworkAgentNodes.add(modifiedAgentNode);
				networkAgentNodesEventPanels.putIfAbsent(modifiedAgentNode, newAgentEventPanel);
				eventPanel.add(newAgentEventPanel, modifiedAgentNode.getAgentName());
			}
			createDefaultAgentComboBoxModel(allNetworkAgentNodes, agentComboBox, ADMIN_PANEL_AGENTS_TYPE_LABEL);
		}
	}

	private JPanel initializeAdminPanel() {
		final JPanel panel = createBorderPanel(ADMIN_PANEL_DEFAULT_PANEL_LAYOUT);
		initializePanelHeader(panel);
		eventPanel = new JPanel(new CardLayout());
		eventPanel.add(createDefaultEmptyPanel(), ADMIN_PANEL_DEFAULT_EMPTY_PANEL_TITLE);
		networkAgentNodesEventPanels.forEach((agent, agentPanel) -> eventPanel.add(agentPanel, agent.getAgentName()));
		panel.add(eventPanel, ADMIN_PANEL_EVENT_PANEL_ATTRIBUTES);
		return panel;
	}

	private void initializePanelHeader(final JPanel panel) {
		addPanelHeader(ADMIN_PANEL_TITLE, panel);
		panel.add(agentComboBox, ADMIN_PANEL_COMBO_BOX_ATTRIBUTES);
		panel.add(createSeparator(GRAY_2_COLOR), ADMIN_PANEL_SEPARATOR_ATTRIBUTES);
	}

	private Map<AbstractAgentNode, Component> initializeAgentEventPanelMap() {
		final Map<AbstractAgentNode, Component> agentEventPanelMap = new HashMap<>();
		allNetworkAgentNodes.forEach(
				agentNode -> agentEventPanelMap.put(agentNode, eventGUIService.createEventPanelForAgent(agentNode)));
		return agentEventPanelMap;
	}

	private void changeSelectedNetworkAgent() {
		final String panelToShow = agentComboBox.getSelectedIndex() == 0 ?
				ADMIN_PANEL_DEFAULT_EMPTY_PANEL_TITLE :
				(String) agentComboBox.getSelectedItem();
		((CardLayout) eventPanel.getLayout()).show(eventPanel, panelToShow);
	}

	private JScrollPane getEventPanelByName(final String agentName) {
		return (JScrollPane) Arrays.stream(eventPanel.getComponents())
				.filter(component -> nonNull(component) && nonNull(component.getName()) && component.getName()
						.equals(agentName))
				.findFirst()
				.orElse(null);
	}
}
