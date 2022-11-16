package com.greencloud.application.agents.scheduler.managment;

import java.time.Duration;

import com.greencloud.commons.job.ClientJob;

/**
 * Set of utilities used to manage the configuration of scheduler agent
 */
public class SchedulerConfigurationManagement {

	private double deadlineWeightPriority;
	private double powerWeightPriority;
	private int maximumQueueSize;
	private int jobSplitThreshold;
	private int splittingFactor;

	/**
	 * Constructor
	 *
	 * @param deadlineWeightPriority initial weight of deadline priority
	 * @param powerWeightPriority    initial weight of power priority
	 * @param maximumQueueSize       maximum queue size
	 *                               //@param jobSplitThreshold	 job size at which splitting will be triggered, can be adjusted by the ManagingAgent
	 */
	public SchedulerConfigurationManagement(double deadlineWeightPriority, double powerWeightPriority,
			int maximumQueueSize) {
		this.deadlineWeightPriority = deadlineWeightPriority;
		this.powerWeightPriority = powerWeightPriority;
		this.maximumQueueSize = maximumQueueSize;
		this.jobSplitThreshold = 40;
		this.splittingFactor = 4;
	}

	/**
	 * Method computes the priority for the given job
	 *
	 * @param clientJob job of interest
	 * @return double being the job priority
	 */
	public double getJobPriority(final ClientJob clientJob) {
		return deadlineWeightPriority * getTimeToDeadline(clientJob) + powerWeightPriority * clientJob.getPower();
	}

	private double getTimeToDeadline(final ClientJob clientJob) {
		return Duration.between(clientJob.getEndTime(), clientJob.getDeadline()).toMillis();
	}

	public int getMaximumQueueSize() {
		return maximumQueueSize;
	}

	public int getJobSplitThreshold() {
		return jobSplitThreshold;
	}

	public int getSplittingFactor() {
		return splittingFactor;
	}
}
