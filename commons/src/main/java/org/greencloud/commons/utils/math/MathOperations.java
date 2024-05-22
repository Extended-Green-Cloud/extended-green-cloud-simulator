package org.greencloud.commons.utils.math;

import static java.lang.Math.max;
import static java.time.Duration.between;
import static org.greencloud.commons.constants.TimeConstants.MILLIS_IN_MIN;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.divideIntoSubIntervals;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.text.similarity.HammingDistance;
import org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants;

/**
 * Class with mathematical and statistical operations
 */
public class MathOperations {

	/**
	 * Method computes the probability that the given maximum value is incorrect
	 * (it was assumed that the smallest time interval is equal to 10 min)
	 *
	 * @param startTime      start time of the interval (in real time)
	 * @param endTime        end time of the interval (in real time)
	 * @param intervalLength length of single sub-interval in minutes
	 * @return margin of error
	 */
	public static double computeIncorrectMaximumValProbability(final Instant startTime, final Instant endTime,
			final long intervalLength) {
		final Set<Instant> subIntervals = divideIntoSubIntervals(startTime, endTime, intervalLength * MILLIS_IN_MIN);
		final long sampleSize = (long) subIntervals.size() - 1;
		final double populationSize = (double) between(startTime, endTime).toMinutes() / 10;

		return GreenEnergyAgentPropsConstants.SUB_INTERVAL_ERROR + max(1 - sampleSize / populationSize, 0);

	}

	/**
	 * Method computes the next number in the Fibonacci sequence
	 *
	 * @param n previous number
	 */
	public static int nextFibonacci(int n) {
		double a = n * (1 + Math.sqrt(5)) / 2.0;
		return (int) Math.round(a);
	}

	/**
	 * Method uses apache.math3.stat to compute Kendall's Tau coefficient used to check the correlation between
	 * time and variable
	 *
	 * @param timeInstances time instances when the values were computed
	 * @param values        computed values
	 * @return correlation coefficient
	 */
	public static double computeKendallTau(final List<Instant> timeInstances, final List<Double> values) {
		final double[] timeValues = timeInstances.stream().mapToDouble(Instant::toEpochMilli).toArray();
		final double[] valueArray = values.stream().mapToDouble(value -> value).toArray();

		return new KendallsCorrelation().correlation(timeValues, valueArray);
	}

	/**
	 * Method computes similarity matrix between two lists of strings using HammingDistances.
	 *
	 * @param firstCodesList  first list of string codes
	 * @param secondCodesList second list of string codes
	 * @return similarity matrix
	 */
	public static List<List<Integer>> computeStringSimilarityMatrix(final Collection<String> firstCodesList,
			final Collection<String> secondCodesList) {
		return firstCodesList.stream()
				.map(firstCode -> getHammingDistancesForStringEncodings(firstCode, secondCodesList))
				.toList();
	}

	private static List<Integer> getHammingDistancesForStringEncodings(final String firstCode,
			final Collection<String> secondCodesList) {
		return secondCodesList.stream()
				.map(secondCode -> new HammingDistance().apply(secondCode, firstCode))
				.toList();
	}
}
