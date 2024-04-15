package org.greencloud.gui.agents.egcs;

import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;

import java.io.Serializable;

import org.greencloud.commons.args.agent.EGCSAgentType;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.gui.messages.ImmutableIsActiveMessage;
import org.greencloud.gui.messages.ImmutableSetNumericValueMessage;
import org.jrba.agentmodel.domain.args.AgentArgs;
import org.jrba.agentmodel.domain.props.AgentProps;

/**
 * Class represents abstract generic agent node which is a part of regional manager
 */
public abstract class EGCSNetworkNode<T extends AgentArgs, E extends AgentProps> extends EGCSNode<T, E>
		implements Serializable {

	protected EGCSNetworkNode() {
	}

	/**
	 * Network agent node constructor
	 *
	 * @param nodeArgs arguments used to create agent node
	 * @param nodeType type of agent node
	 */
	protected EGCSNetworkNode(final T nodeArgs, final EGCSAgentType nodeType) {
		super(nodeArgs, nodeType);
	}

	/**
	 * Function updates the current traffic for given value
	 *
	 * @param traffic current traffic
	 */
	public void updateTraffic(final double traffic) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(traffic)
				.agentName(agentName)
				.type("SET_TRAFFIC")
				.build());
	}

	/**
	 * Function updates the information if the given network node is active
	 *
	 * @param isActive information if the network node is active
	 */
	public void updateIsActive(final boolean isActive) {
		if (!(this instanceof RegionalManagerNode)) {
			getAgentsWebSocket().send(ImmutableIsActiveMessage.builder()
					.data(isActive)
					.agentName(agentName)
					.build());
		}
	}

	/**
	 * Function updates the number of currently executed jobs
	 *
	 * @param value new jobs count
	 */
	public void updateJobsCount(final int value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_JOBS_COUNT")
				.build());
	}

	/**
	 * Function updates the number of jobs being on hold to given value
	 *
	 * @param value number of jobs that are on hold
	 */
	public void updateJobsOnHoldCount(final int value) {
		if (!(this instanceof RegionalManagerNode)) {
			getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
					.data(value)
					.agentName(agentName)
					.type("SET_ON_HOLD_JOBS_COUNT")
					.build());
		}
	}

	/**
	 * Function updates the current job success ratio of a network agent
	 *
	 * @param value new success ratio
	 */
	public void updateCurrentJobSuccessRatio(final double value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.type("SET_JOB_SUCCESS_RATIO")
				.agentName(agentName)
				.data(value * 100)
				.build());
	}
}
