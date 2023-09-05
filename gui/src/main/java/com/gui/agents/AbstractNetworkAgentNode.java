package com.gui.agents;

import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;

import java.io.Serializable;

import com.gui.message.ImmutableIsActiveMessage;
import com.gui.message.ImmutableSetNumericValueMessage;

/**
 * Class represents abstract generic agent node which is a part of cloud network
 */
public abstract class AbstractNetworkAgentNode extends AbstractAgentNode implements Serializable {

	protected AbstractNetworkAgentNode() {
	}

	/**
	 * Network agent node constructor
	 *
	 * @param agentName agent node name
	 */
	protected AbstractNetworkAgentNode(final String agentName) {
		super(agentName);
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
		if (!(this instanceof CloudNetworkAgentNode)) {
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
		if (!(this instanceof CloudNetworkAgentNode)) {
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
