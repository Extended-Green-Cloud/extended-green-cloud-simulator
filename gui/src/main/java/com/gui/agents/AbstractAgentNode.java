package com.gui.agents;

import static com.gui.gui.utils.GUIContainerUtils.createLabelListPanel;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.gui.event.domain.AbstractEvent;
import com.gui.event.domain.EventTypeEnum;
import com.gui.graph.GraphService;
import com.gui.gui.panels.domain.listlabels.ListLabelEnum;

/**
 * Class represents abstract generic agent node
 */
public abstract class AbstractAgentNode implements AbstractAgentNodeInterface {

	protected String agentName;
	protected JPanel agentDetailsPanel;
	protected LinkedHashMap<ListLabelEnum, JLabel> agentDetailLabels;
	protected Map<EventTypeEnum, Boolean> agentEvents;
	protected Stack<AbstractEvent> invokedAgentEvents;
	protected GraphService graphService;

	/**
	 * Class constructor
	 *
	 * @param agentName name of the agent
	 */
	protected AbstractAgentNode(String agentName) {
		this.agentName = agentName;
		this.agentEvents = new EnumMap<>(EventTypeEnum.class);
		this.invokedAgentEvents = new Stack<>();
		this.agentDetailLabels = new LinkedHashMap<>();
	}

	@Override
	public void addToGraph(final GraphService graphService) {
		this.graphService = graphService;
		graphService.createAndAddNodeToGraph(this);
	}

	@Override
	public void createInformationPanel() {
		this.agentDetailsPanel = createLabelListPanel(agentDetailLabels);
	}

	@Override
	public void addEventToStack(final AbstractEvent event) {
		invokedAgentEvents.push(event);
	}

	@Override
	public String getAgentName() {
		return agentName;
	}

	@Override
	public JPanel getAgentDetailsPanel() {
		return agentDetailsPanel;
	}

	@Override
	public AbstractEvent removeEventFromStack() {
		return invokedAgentEvents.isEmpty() ? null : invokedAgentEvents.pop();
	}

	@Override
	public Map<EventTypeEnum, Boolean> getAgentEvents() {
		return agentEvents;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AbstractAgentNode agentNode = (AbstractAgentNode) o;
		return agentName.equals(agentNode.agentName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(agentName);
	}

}
