package runner.configuration;

import static com.google.common.collect.Range.closed;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import java.util.Properties;

import com.google.common.collect.Range;

/**
 * Constants used to set up resource generator used in simulation
 */
public class ResourceRequirementConfiguration extends AbstractConfiguration {

	/**
	 * Range of CPU requirement values of random jobs
	 */
	public static Range<Long> cpuRange;

	/**
	 * Range of memory requirement values of random jobs
	 */
	public static Range<Long> memoryRange;

	/**
	 * Range of seconds for being job duration
	 */
	public static Range<Long> durationRange;

	/**
	 * Range of seconds added to job duration indicating its deadline
	 */
	public static Range<Long> deadlineRange;

	/**
	 * Range of storage requirement values of random jobs
	 */
	public static Range<Long> storageRange;

	/**
	 * Range of steps number of jobs
	 */
	public static Range<Integer> stepsNumberRange;

	/**
	 * Range of budget limit requirement values of random jobs
	 */
	public static Range<Double> budgetRange;

	/**
	 * Method reads the properties set for the jobs requirements
	 */
	public static void readJobsRequirements(Properties props) {

		final Long minCpu = parseLong(ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.mincpu"), "0"));
		final Long maxCpu = parseLong(ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.maxcpu"), "0"));
		cpuRange = closed(minCpu, maxCpu);

		final Long minMemory = parseLong(ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.minmemory"), "0"));
		final Long maxMemory = parseLong(ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.maxmemory"), "0"));
		memoryRange = closed(minMemory, maxMemory);

		final Long minStorage = parseLong(ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.minstorage"), "0"));
		final Long maxStorage = parseLong(ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.maxstorage"), "0"));
		storageRange = closed(minStorage, maxStorage);

		final Long minDuration = parseLong(
				ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.minduration"), "0"));
		final Long maxDuration = parseLong(
				ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.maxduration"), "0"));
		durationRange = closed(minDuration, maxDuration);

		final Long minDeadline = parseLong(
				ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.mindeadline"), "0"));
		final Long maxDeadline = parseLong(
				ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.maxdeadline"), "0"));
		deadlineRange = closed(minDeadline, maxDeadline);

		final Integer minSteps = parseInt(
				ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.steps.minnumber"), "1"));
		final Integer maxSteps = parseInt(
				ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.steps.maxnumber"), "1"));
		stepsNumberRange = closed(minSteps, maxSteps);

		final Double minBudget = parseDouble(ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.minBudget"), "0"));
		final Double maxBudget = parseDouble(ifNotBlankThenGetOrElse(props.getProperty("scenario.jobs.maxBudget"), "0"));
		budgetRange = closed(minBudget, maxBudget);

	}
}
