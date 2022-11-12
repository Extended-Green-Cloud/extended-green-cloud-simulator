package com.greencloud.application.agents.scheduler.managment;

import java.time.Duration;

import com.greencloud.application.domain.job.ClientJob;

/**
 * Set of utilities used to manage the configuration of scheduler agent
 */
public class SchedulerConfigurationManagement {

	private double deadlineWeightPriority;
	private double powerWeightPriority;

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

	public void setDeadlineWeightPriority(double deadlineWeightPriority) {
		this.deadlineWeightPriority = deadlineWeightPriority;
	}

	public void setPowerWeightPriority(double powerWeightPriority) {
		this.powerWeightPriority = powerWeightPriority;
	}
}
