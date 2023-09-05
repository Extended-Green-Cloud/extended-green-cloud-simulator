package com.greencloud.application.agents.scheduler.managment;

import static com.greencloud.application.agents.scheduler.managment.logs.SchedulerManagementLog.INCREASE_DEADLINE_WEIGHT_LOG;
import static com.greencloud.application.agents.scheduler.managment.logs.SchedulerManagementLog.INCREASE_CPU_WEIGHT_LOG;
import static com.greencloud.application.utils.AlgorithmUtils.nextFibonacci;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.greencloud.application.agents.AbstractAgentManagement;
import com.greencloud.application.agents.scheduler.SchedulerAgent;

/**
 * Set of utilities used to adapt the Scheduler Agent
 */
public class SchedulerAdaptationManagement extends AbstractAgentManagement {

	private static final Logger logger = getLogger(SchedulerAdaptationManagement.class);

	private final SchedulerAgent schedulerAgent;

	/**
	 * Default constructor
	 *
	 * @param schedulerAgent parent scheduler agent
	 */
	public SchedulerAdaptationManagement(final SchedulerAgent schedulerAgent) {
		this.schedulerAgent = schedulerAgent;
	}

	/**
	 * Method increases the deadline weight to the next number in a Fibonacci sequence
	 */
	public boolean increaseDeadlineWeight() {
		final int oldPriority = schedulerAgent.getDeadlinePriority();
		schedulerAgent.setDeadlinePriority(nextFibonacci(oldPriority));
		logger.info(INCREASE_DEADLINE_WEIGHT_LOG, oldPriority, schedulerAgent.getDeadlinePriority());
		schedulerAgent.manage().updateWeightsGUI();
		return true;
	}

	/**
	 * Method increases the power weight to the next number in a Fibonacci sequence
	 */
	public boolean increaseCPUWeight() {
		final int oldPriority = schedulerAgent.getCPUPriority();
		schedulerAgent.setCPUPriority(nextFibonacci(oldPriority));
		logger.info(INCREASE_CPU_WEIGHT_LOG, oldPriority, schedulerAgent.getCPUPriority());
		schedulerAgent.manage().updateWeightsGUI();
		return true;
	}
}
