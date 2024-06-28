package org.greencloud.agentsystem.strategies.algorithms.priority;

import static java.lang.Math.log10;
import static java.time.Duration.between;
import static java.util.Comparator.comparingLong;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CU;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.extended.JobWithExecutionTimeAndError;

import jade.lang.acl.ACLMessage;

/**
 * Class implementing common methods evaluating jobs priority.
 */
@SuppressWarnings("unchecked")
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
	 * Method computes the priority of the job using solely the duration.
	 *
	 * @param job job for which priority is to be estimated
	 * @return job priority
	 */
	public static double evaluatePriorityBasedOnDuration(final ClientJob job) {
		return ofNullable(job.getDuration())
				.filter(duration -> duration != 0.0)
				.map(duration -> 1 / (double) duration)
				.orElse(0.0);
	}

	/**
	 * Method computes the priority of the job using the information about its estimated execution time.
	 *
	 * @param messages messages received from region managers
	 * @return job priority
	 */
	public static double evaluatePriorityBasedOnFastestExecutionTime(final Collection<ACLMessage> messages) {
		return 1 / (double) messages.stream()
				.map(message -> readMessageContent(message, Long.class))
				.mapToLong(Long::longValue)
				.min()
				.orElse(0);
	}

	/**
	 * Method computes the priority of the job using the information about its estimated execution time.
	 *
	 * @param messages messages received from region managers
	 * @return job priority
	 * @implNote used algorithm: [<a href="https://hal.science/hal-04498634v1/document">Multi-queue allocation</a>]
	 */
	public static double evaluatePriorityBasedOnEnhancedFastestExecutionTime(final Collection<ACLMessage> messages) {
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

	/**
	 * Method evaluates job priority based on 4 credits: length, priority, deadline and cost.
	 *
	 * @param job               job for which the priority is to be computed
	 * @param allJobs           all jobs that are currently considered (sorted in ascending order by duration)
	 * @param executorResources resources of possible job executors
	 * @return job priority
	 * @implNote used algorithm: [<a href="https://www.sciencedirect.com/science/article/pii/S1110866519303330">Priority-based allocation</a>]
	 */
	public static double evaluatePriorityBasedOnCombinedCredits(final ClientJob job, final List<ClientJob> allJobs,
			final List<Map<String, Object>> executorResources) {
		final int lengthCredit = computeJobLengthCredit(job, allJobs);
		final double priorityCredit = computeJobPriorityCredit(job, allJobs);
		final double deadlineCredit = computeJobDeadlineCredit(lengthCredit, priorityCredit, executorResources);
		final double costCredit = computeJobCostCredit(job, executorResources);

		return lengthCredit + priorityCredit + deadlineCredit + costCredit;
	}

	private static double computeJobCostCredit(final ClientJob job,
			final List<Map<String, Object>> executorResources) {
		return executorResources.stream()
				.map(resourceMap -> (Map<String, Double>) resourceMap.get(BUDGET))
				.mapToDouble(costMap -> costMap.get(job.getJobId()))
				.average()
				.orElse(1.0);
	}

	private static double computeJobDeadlineCredit(final int lengthCredit, final double priorityCredit,
			final List<Map<String, Object>> executorResources) {
		final double highestCU = executorResources.stream()
				.map(resourceMap -> resourceMap.get(CU))
				.mapToDouble(Double.class::cast)
				.max()
				.orElse(1.0);

		return (lengthCredit * priorityCredit) / highestCU;
	}

	private static double computeJobPriorityCredit(final ClientJob job, final List<ClientJob> allJobs) {
		final long highestPriority = allJobs.stream()
				.map(PowerJob::getPriority)
				.filter(Objects::nonNull)
				.max(Integer::compareTo)
				.orElse(1);
		final int divisionFactor = (int) (log10(highestPriority) + 1);

		return ofNullable(job.getPriority())
				.map(priority -> (double) priority / divisionFactor)
				.orElse(0.0);
	}

	private static int computeJobLengthCredit(final ClientJob job, final List<ClientJob> allJobs) {
		final long longestDuration = allJobs.getLast().getDuration();

		final double firstThreshold = ((double) longestDuration) / 5;
		final double secondThreshold = ((double) longestDuration) / 4;
		final double thirdThreshold = secondThreshold + firstThreshold;
		final double fourthThreshold = thirdThreshold - secondThreshold;

		final Map<Integer, Double> creditForThresholdMap = Map.of(
				5, firstThreshold,
				4, secondThreshold,
				3, thirdThreshold,
				2, fourthThreshold
		);

		return creditForThresholdMap.entrySet().stream()
				.filter(creditForThreshold -> job.getDuration() <= creditForThreshold.getValue())
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(1);
	}
}
