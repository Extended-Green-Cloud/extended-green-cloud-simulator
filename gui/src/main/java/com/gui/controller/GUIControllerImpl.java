package com.gui.controller;

import static com.gui.controller.domain.GUIControllerConstants.ADMIN_FRAME_SIZE;
import static com.gui.controller.domain.GUIControllerConstants.ADMIN_PANEL_TITLE;
import static com.gui.controller.domain.GUIControllerConstants.AGENT_DETAILS_PANEL_ATTRIBUTES;
import static com.gui.controller.domain.GUIControllerConstants.GRAPH_DIMENSIONS;
import static com.gui.controller.domain.GUIControllerConstants.GRAPH_PANEL_ATTRIBUTES;
import static com.gui.controller.domain.GUIControllerConstants.INFORMATION_PANEL_ATTRIBUTES;
import static com.gui.controller.domain.GUIControllerConstants.MAIN_PANEL_LAYOUT;
import static com.gui.controller.domain.GUIControllerConstants.MAIN_PANEL_TITLE;
import static com.gui.controller.domain.GUIControllerConstants.MAIN_PANEL_TITLE_ATTRIBUTES;
import static com.gui.controller.domain.GUIControllerConstants.MAIN_SIZE;
import static com.gui.controller.domain.GUIControllerConstants.NETWORK_DETAIL_PANEL_ATTRIBUTES;
import static com.gui.controller.domain.GUIControllerConstants.NETWORK_DETAIL_PANEL_LAYOUT;
import static com.gui.controller.domain.GUIControllerConstants.SUMMARY_PANEL_ATTRIBUTES;
import static com.gui.controller.domain.GUIControllerConstants.USER_PANEL_TITLE;
import static com.gui.gui.utils.GUIContainerUtils.createDefaultFrame;
import static com.gui.gui.utils.GUIContainerUtils.createDefaultScrollPanel;
import static com.gui.gui.utils.GUIContainerUtils.createShadowPanel;
import static com.gui.gui.utils.GUILabelUtils.createTitleLabel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.gui.agents.AbstractAgentNode;
import com.gui.agents.ClientAgentNode;
import com.gui.graph.GraphService;
import com.gui.graph.GraphServiceImpl;
import com.gui.gui.panels.AdminPanel;
import com.gui.gui.panels.DetailsPanel;
import com.gui.gui.panels.InformationPanel;
import com.gui.gui.panels.SummaryPanel;
import com.mxgraph.swing.mxGraphComponent;

import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

public class GUIControllerImpl implements GUIController {

	private final List<AbstractAgentNode> graphNodes;
	private final mxGraphComponent graph;
	private final GraphService graphService;
	private final InformationPanel informationPanel;
	private final SummaryPanel summaryPanel;
	private final DetailsPanel detailsPanel;
	private final AdminPanel adminPanel;
	private final JFrame mainFrame;
	private final JFrame adminFrame;

	/**
	 * Default constructor
	 */
	public GUIControllerImpl() {
		this.graphNodes = new ArrayList<>();
		this.summaryPanel = new SummaryPanel();
		this.informationPanel = new InformationPanel();
		this.detailsPanel = new DetailsPanel();
		this.adminPanel = new AdminPanel();
		this.graphService = new GraphServiceImpl(GRAPH_DIMENSIONS);
		this.graph = graphService.createGraphComponent();
		this.adminFrame = createDefaultFrame(ADMIN_PANEL_TITLE, ADMIN_FRAME_SIZE, adminPanel.getMainPanel());
		this.mainFrame = createDefaultFrame(USER_PANEL_TITLE, MAIN_SIZE, createMainPanel());
	}

	@Override
	public void run() {
		mainFrame.setVisible(true);
		SwingUtilities.invokeLater(() -> adminFrame.setVisible(true));
	}

	@Override
	public synchronized void addAgentNodeToGraph(final AbstractAgentNode agent) {
		graphNodes.add(agent);
		if (!(agent instanceof ClientAgentNode)) {
			agent.addToGraph(graphService);
			detailsPanel.revalidateComboBoxModel(graphNodes, false);
			adminPanel.revalidateComboBoxModel(agent, false);
		} else {
			detailsPanel.revalidateComboBoxModel(graphNodes, true);
		}
	}

	@Override
	public void removeAgentNodeFromGraph(final AbstractAgentNode agent) {
		graphNodes.remove(agent);
		if (!(agent instanceof ClientAgentNode)) {
			graphService.removeNodeFromGraph(agent);
			detailsPanel.revalidateComboBoxModel(graphNodes, false);
			adminPanel.revalidateComboBoxModel(agent, true);
		} else {
			detailsPanel.revalidateComboBoxModel(graphNodes, true);
		}
	}

	@Override
	public synchronized void createEdges() {
		graphNodes.forEach(AbstractAgentNode::createEdges);
		graphService.updateGraphLayout();
	}

	@Override
	public void updateClientsCountByValue(int value) {
		summaryPanel.updateClientsCount(value);
		refreshMainFrame();
	}

	@Override
	public void updateActiveJobsCountByValue(int value) {
		summaryPanel.updateActiveJobsCountByValue(value);
		refreshMainFrame();
	}

	@Override
	public void updateAllJobsCountByValue(int value) {
		summaryPanel.updateAllJobsCountByValue(value);
		refreshMainFrame();
	}

	@Override
	public synchronized void addNewInformation(String information) {
		informationPanel.addNewInformation(information);
		refreshMainFrame();
	}

	@Override
	public void displayMessageArrow(final AbstractAgentNode senderAgent, final List<String> receiversNames) {
		if (!(senderAgent instanceof ClientAgentNode)) {
			graphService.displayMessageEdges(senderAgent.getAgentName(), receiversNames);
		}
	}

	private JScrollPane createMainPanel() {
		final JPanel mainPanel = new JPanel();
		mainPanel.setPreferredSize(MAIN_SIZE);
		mainPanel.setLayout(MAIN_PANEL_LAYOUT);
		mainPanel.setBackground(Color.WHITE);

		mainPanel.add(createTitleLabel(MAIN_PANEL_TITLE), MAIN_PANEL_TITLE_ATTRIBUTES);
		mainPanel.add(createNetworkDetailsPanel(), NETWORK_DETAIL_PANEL_ATTRIBUTES);
		mainPanel.add(graph, GRAPH_PANEL_ATTRIBUTES);
		mainPanel.add(createInformationPanel(), INFORMATION_PANEL_ATTRIBUTES);

		final JScrollPane scrollPane = createDefaultScrollPanel(mainPanel);
		scrollPane.setPreferredSize(MAIN_SIZE);
		return scrollPane;
	}

	private JPanel createNetworkDetailsPanel() {
		final JPanel networkDetailsPanel = createShadowPanel(NETWORK_DETAIL_PANEL_LAYOUT);
		networkDetailsPanel.add(summaryPanel.getMainPanel(), SUMMARY_PANEL_ATTRIBUTES);
		networkDetailsPanel.add(detailsPanel.getMainPanel(), AGENT_DETAILS_PANEL_ATTRIBUTES);
		return networkDetailsPanel;
	}

	private JPanel createInformationPanel() {
		final JPanel networkDetailsPanel = createShadowPanel(new MigLayout(new LC().fill().wrapAfter(1)));
		networkDetailsPanel.add(informationPanel.getMainPanel(), new CC().spanX().grow());
		return networkDetailsPanel;
	}

	private void refreshMainFrame() {
		synchronized (mainFrame) {
			mainFrame.revalidate();
			mainFrame.repaint();
			adminFrame.revalidate();
			adminFrame.repaint();
		}
	}
}
