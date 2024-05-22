package org.greencloud.agentsystem.strategies.algorithms.priority;

import static java.time.Duration.between;
import static java.util.Comparator.comparingLong;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.util.Collection;
import java.util.List;

import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithExecutionTimeAndError;

import jade.lang.acl.ACLMessage;

/**
 * Class implementing common methods evaluating jobs priority.
 */
public class PriorityEstimator {

	/**
	 * Method computes the priority of the job using solely the deadline.
	 *
	 * @param job job for which priority is to be estimated
	 * @return job priority
	 */
	public static double evaluatePriorityBasedOnDeadline(final ClientJob job) {
		return between(getCurrentTime(), job.getDeadline()).toMillis();
	}

	/**
	 * Method computes the priority of the job using the information about its estimated execution time.
	 *
	 * @param messages messages received from region managers
	 * @return job priority
	 * @implNote used algorithm: [<a href="https://hal.science/hal-04498634v1/document">Multi-queue allocation</a>]
	 */
	public static double evaluatePriorityBasedOnFastestExecutionTime(final Collection<ACLMessage> messages) {
		final List<JobWithExecutionTimeAndError> rmaResults = messages.stream()
				.map(message -> readMessageContent(message, JobWithExecutionTimeAndError.class))
				.sorted(comparingLong(JobWithExecutionTimeAndError::getExecutionTime))
				.toList();
		final JobWithExecutionTimeAndError bestResult = rmaResults.getFirst();
		final Long error = bestResult.getHighestError();

		if (rmaResults.size() == 1) {
			return 1D;
		}

		final Long secondBestExecutionTime = rmaResults.get(1).getExecutionTime();
		final Double timeDifference = (double) (secondBestExecutionTime - bestResult.getExecutionTime());
		return error.equals(0L) ? timeDifference : ((timeDifference + error) / 2 * error);
	}

}
