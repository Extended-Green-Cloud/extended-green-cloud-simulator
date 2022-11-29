package com.gui.agents;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.gui.message.ImmutableIsActiveMessage;
import com.gui.message.ImmutableSetMaximumCapacityMessage;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.message.domain.ImmutableCapacity;

/**
 * Class represents abstract generic agent node which is a part of cloud network
 */
public abstract class AbstractNetworkAgentNode extends AbstractAgentNode {

	protected double initialMaximumCapacity;
	protected AtomicReference<Double> currentMaximumCapacity;
	protected AtomicBoolean isActive;
	protected AtomicInteger numberOfExecutedJobs;
	protected AtomicInteger numberOfJobsOnHold;

	protected AbstractNetworkAgentNode() {

	}

	/**
	 * Network agent node constructor
	 *
	 * @param agentName       agent node name
	 * @param maximumCapacity maximum capacity of network agent
	 */
	protected AbstractNetworkAgentNode(final String agentName, final double maximumCapacity) {
		super(agentName);
		this.initialMaximumCapacity = maximumCapacity;
		this.currentMaximumCapacity = new AtomicReference<>(maximumCapacity);
		this.isActive = new AtomicBoolean(false);
		this.numberOfExecutedJobs = new AtomicInteger(0);
		this.numberOfJobsOnHold = new AtomicInteger(0);
	}

	/**
	 * Function updates the current maximum capacity to given value
	 *
	 * @param maxCapacity new maximum capacity
	 */
	public void updateMaximumCapacity(final int maxCapacity, final int powerInUse) {
		this.currentMaximumCapacity.set((double) maxCapacity);
		webSocketClient.send(ImmutableSetMaximumCapacityMessage.builder()
				.agentName(agentName)
				.data(ImmutableCapacity.builder()
						.maximumCapacity(maxCapacity)
						.powerInUse(powerInUse)
						.build())
				.build());
	}

	/**
	 * Function updates the current traffic for given value
	 *
	 * @param powerInUse current power in use
	 */
	public void updateTraffic(final double powerInUse) {
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.data(powerInUse)
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
		webSocketClient.send(ImmutableIsActiveMessage.builder()
				.data(isActive)
				.agentName(agentName)
				.build());
	}

	/**
	 * Function updates the number of currently executed jobs
	 *
	 * @param value new jobs count
	 */
	public void updateJobsCount(final int value) {
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
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
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_ON_HOLD_JOBS_COUNT")
				.build());
	}

	/**
	 * Function updates the current job success ratio of a network agent
	 *
	 * @param value new success ratio
	 */
	public void updateCurrentJobSuccessRatio(final double value) {
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.type("SET_JOB_SUCCESS_RATIO")
				.agentName(agentName)
				.data(value * 100)
				.build());
	}
}
