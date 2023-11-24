package org.greencloud.commons.args.agent;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.AgentType.BASIC;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;

import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class extended by classes representing properties of individual agent types
 */
@Getter
@Setter
public class AgentProps {

	private static final Logger logger = getLogger(AgentProps.class);

	protected String agentName;
	protected AgentNodeProps<AgentProps> agentNode;
	protected String agentType;
	protected Map<String, Map<String, Object>> systemKnowledge;
	protected Facts agentKnowledge;

	/**
	 * Default constructor that sets the type of the agent
	 *
	 * @param agentName name of the agent
	 */
	public AgentProps(final String agentName) {
		this.agentType = BASIC.name();
		this.agentName = agentName;
		this.systemKnowledge = new HashMap<>();
		this.agentKnowledge = new Facts();
	}

	/**
	 * Default constructor that sets the type of the agent
	 *
	 * @param agentName name of the agent
	 */
	public AgentProps(final AgentType agentType, final String agentName) {
		this.agentType = agentType.name();
		this.agentName = agentName;
		this.systemKnowledge = new HashMap<>();
		this.agentKnowledge = new Facts();
	}

	/**
	 * Default constructor that sets the type of the agent
	 *
	 * @param agentName name of the agent
	 */
	public AgentProps(final String agentType, final String agentName) {
		this.agentType = agentType;
		this.agentName = agentName;
		this.systemKnowledge = new HashMap<>();
		this.agentKnowledge = new Facts();
	}

	/**
	 * Method used in updating GUI associated with given agent (to be overridden)
	 */
	public void updateGUI() {
		if (nonNull(agentNode)) {
			agentNode.updateGUI(this);
		}
	}

	/**
	 * Method used in updating GUI associated with given agent (to be overridden)
	 */
	public void saveMonitoringData() {
		if (nonNull(agentNode)) {
			agentNode.saveMonitoringData(this);
		}
	}

	public void setSystemKnowledge(
			final Map<String, Map<String, Object>> systemKnowledge) {
			this.systemKnowledge = new HashMap<>(systemKnowledge);
	}
}
