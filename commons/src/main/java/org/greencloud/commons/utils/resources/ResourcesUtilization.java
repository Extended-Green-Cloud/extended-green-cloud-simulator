package org.greencloud.commons.utils.resources;

import static java.util.Collections.emptyList;
import static java.util.Collections.max;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.SetUtils.union;
import static org.greencloud.commons.constants.TimeConstants.MILLIS_IN_MIN;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.utils.resources.domain.JobWithTime.TimeType.END_TIME;
import static org.greencloud.commons.utils.resources.domain.JobWithTime.TimeType.START_TIME;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.domain.resources.ResourceCharacteristic;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.utils.resources.domain.JobWithTime;
import org.greencloud.commons.utils.resources.domain.SubJobList;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * Class with algorithms used to compute resource utilization
 */
public class ResourcesUtilization {

	/**
	 * Method computes estimated earliest possible job execution time
	 *
	 * @param jobList              list of the jobs of interest
	 * @param initialResources     initial owned resources
	 * @param jobRequiredResources resources required by the job
	 * @param duration             job duration in milliseconds
	 */
	public static <T extends PowerJob> Instant getEarliestPossibleJobStartTime(
			final Set<T> jobList,
			final Map<String, Resource> initialResources,
			final Map<String, Resource> jobRequiredResources,
			final long duration) {
		final List<JobWithTime<T>> jobsWithTimeMap = getJobsWithTimesForInterval(jobList, null, null);

		final AtomicReference<Instant> earliestPossibleJobStart = new AtomicReference<>();

		if (jobsWithTimeMap.isEmpty()) {
			return getCurrentTime();
		}

		jobsWithTimeMap.stream().anyMatch(jobWithTime -> {
			final Instant startTime = jobWithTime.time;
			final Instant endTime = startTime.plusMillis(duration);
			final Map<String, Resource> maxUsedResource =
					getMaximumUsedResourcesDuringTimeStamp(jobList, initialResources, startTime, endTime);
			final Map<String, Resource> remainingResources =
					computeResourceDifference(initialResources, maxUsedResource);

			if(areSufficient(remainingResources, jobRequiredResources)) {
				earliestPossibleJobStart.set(startTime);
				return true;
			}
			earliestPossibleJobStart.set(endTime);
			return false;
		});

		return  earliestPossibleJobStart.get();
	}

	/**
	 * Method computes the average CPU usage during given time-stamp
	 *
	 * @param jobList   list of the jobs of interest
	 * @param startTime start time of the interval
	 * @param endTime   end time of the interval
	 */
	public static <T extends PowerJob> Double getAverageCPUUsageDuringTimeStamp(
			final Set<T> jobList,
			final Instant startTime,
			final Instant endTime) {
		final List<JobWithTime<T>> jobsWithTimeMap = getJobsWithTimesForInterval(jobList, startTime, endTime);

		final List<T> openIntervalJobs = new ArrayList<>();
		final AtomicDouble lastCPU = new AtomicDouble(0);
		final List<Double> cpuUsageInInterval = new ArrayList<>();

		if (jobsWithTimeMap.isEmpty()) {
			return lastCPU.get();
		}

		jobsWithTimeMap.forEach(jobWithTime -> {
			final Double requiredCPU = (jobWithTime.job).getRequiredResources().get(CPU).getAmountInCommonUnit();

			if (jobWithTime.timeType.equals(START_TIME)) {
				openIntervalJobs.add(jobWithTime.job);
				lastCPU.updateAndGet(prev -> prev + requiredCPU);
			} else {
				openIntervalJobs.remove(jobWithTime.job);
				cpuUsageInInterval.add(lastCPU.get());
				lastCPU.set(openIntervalJobs.isEmpty() ? 0 : lastCPU.get() - requiredCPU);
			}
		});

		return cpuUsageInInterval.stream().mapToDouble(Double::doubleValue).average().orElse(0);
	}

	/**
	 * Method computes the maximum resource usage during given time-stamp
	 *
	 * @param jobList   list of the jobs of interest
	 * @param startTime start time of the interval
	 * @param endTime   end time of the interval
	 */
	public static <T extends PowerJob> Map<String, Resource> getMaximumUsedResourcesDuringTimeStamp(
			final Set<T> jobList,
			final Map<String, Resource> initialResources,
			final Instant startTime,
			final Instant endTime) {
		final List<JobWithTime<T>> jobsWithTimeMap = getJobsWithTimesForInterval(jobList, startTime, endTime);
		final Set<String> resourceKeys = initialResources.keySet();

		final List<T> openIntervalJobs = new ArrayList<>();
		final Map<String, Resource> lastResourceMap = initialResources.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getEmptyResource()));
		final Map<String, List<Resource>> resourceUsageInInterval = resourceKeys.stream()
				.collect(toMap(key -> key, key -> new ArrayList<>()));

		if (jobsWithTimeMap.isEmpty()) {
			return lastResourceMap;
		}

		jobsWithTimeMap.forEach(jobWithTime -> {
			final Map<String, Resource> resources = (jobWithTime.job).getRequiredResources();
			if (jobWithTime.timeType.equals(START_TIME)) {
				openIntervalJobs.add(jobWithTime.job);

				resources.forEach((key, resource) ->
						lastResourceMap.computeIfPresent(key, (k, prevVal) -> prevVal.addResource(resource)));
			} else {
				openIntervalJobs.remove(jobWithTime.job);

				resourceKeys.forEach(key -> {
					resourceUsageInInterval.get(key).add(lastResourceMap.get(key));
					lastResourceMap.computeIfPresent(key,
							(k, currResource) -> openIntervalJobs.isEmpty() ?
									initialResources.get(key).getEmptyResource() :
									currResource.reserveResource(resources.get(k)));
				});
			}
		});

		return resourceKeys.stream().collect(toMap(key -> key, key -> max(resourceUsageInInterval.get(key),
				(resource1, resource2) -> initialResources.get(key).compareResource(resource1, resource2))));
	}

	/**
	 * Method computes the minimized available power during specific time-stamp.
	 * The momentum available power is a difference between available green power and the power in use at the
	 * specific moment
	 * <p/>
	 * IMPORTANT! All time frames used in the calculation refer to the real time (not simulation time). The main
	 * reason for that is that the weather forecast requires real job execution time
	 *
	 * @param jobList        list of the jobs of interest (that uses real times instead of simulation times)
	 * @param startTime      start time of the interval (in real time)
	 * @param endTime        end time of the interval (in real time)
	 * @param intervalLength length of single sub-interval in minutes
	 * @param agentProps     manager properties of green energy agent
	 * @param monitoringData weather data necessary to compute available capacity
	 */
	public static double getMinimalAvailableEnergyDuringTimeStamp(
			final Set<ServerJob> jobList,
			final Instant startTime,
			final Instant endTime,
			final long intervalLength,
			final GreenEnergyAgentProps agentProps,
			final MonitoringData monitoringData) {
		final List<JobWithTime<ServerJob>> jobsWithTimeMap = getJobsWithTimesForInterval(jobList, startTime, endTime);

		final Deque<Map.Entry<Instant, Double>> powerInIntervals = jobsWithTimeMap.isEmpty() ? new ArrayDeque<>() :
				getEnergyForJobIntervals(jobsWithTimeMap.subList(0, jobsWithTimeMap.size() - 1));
		final Set<Instant> subIntervals = divideIntoSubIntervals(startTime, endTime, intervalLength * MILLIS_IN_MIN);

		final AtomicReference<Double> minimumAvailableEnergy = new AtomicReference<>(
				(double) agentProps.getMaximumGeneratorCapacity());
		final AtomicReference<Map.Entry<Instant, Double>> lastOpenedPowerInterval = new AtomicReference<>(null);

		subIntervals.forEach(time -> {
			while (!powerInIntervals.isEmpty() && !powerInIntervals.peekFirst().getKey().isAfter(time)) {
				lastOpenedPowerInterval.set(powerInIntervals.removeFirst());
			}
			final double availableCapacity = agentProps.getAvailableGreenEnergy(monitoringData, time);
			final double powerInUse = nonNull(lastOpenedPowerInterval.get()) ?
					lastOpenedPowerInterval.get().getValue() : 0;
			final double availablePower = availableCapacity - powerInUse;

			if (availablePower >= 0 && availablePower < minimumAvailableEnergy.get()) {
				minimumAvailableEnergy.set(availablePower);
			}
		});

		return minimumAvailableEnergy.get();
	}

	/**
	 * Method divides the given interval into sub-intervals of specified size
	 *
	 * @param startTime time interval start time
	 * @param endTime   time interval end time
	 * @param length    length of sub-interval
	 * @return set of sub-intervals represented by their start times
	 */
	public static Set<Instant> divideIntoSubIntervals(final Instant startTime, final Instant endTime,
			final Long length) {
		final AtomicReference<Instant> currentTime = new AtomicReference<>(startTime);
		final Set<Instant> subIntervals = new LinkedHashSet<>();

		do {
			subIntervals.add(currentTime.get());
			currentTime.getAndUpdate(time -> time.plusMillis(length));
		} while (currentTime.get().isBefore(endTime) && length != 0);

		subIntervals.add(endTime);

		return subIntervals;
	}

	/**
	 * Method retrieves from the list of jobs, the ones which summed power will be the closest to the finalPower
	 *
	 * @param jobs       list of jobs to go through
	 * @param finalPower power bound
	 * @return list of jobs withing power bound
	 */
	public static List<ServerJob> findJobsWithinPower(final List<ServerJob> jobs, final double finalPower) {
		if (finalPower == 0) {
			return emptyList();
		}
		final AtomicReference<SubJobList<ServerJob>> result = new AtomicReference<>(new SubJobList<>());
		final Set<SubJobList<ServerJob>> sums = new HashSet<>();

		sums.add(result.get());
		jobs.forEach(job -> {
			final Set<SubJobList<ServerJob>> newSums = new HashSet<>();
			sums.forEach(sum -> {
				final List<ServerJob> newSubList = new ArrayList<>(sum.subList);
				newSubList.add(job);
				final SubJobList<ServerJob> newSum = new SubJobList<>(
						sum.energySum + job.getEstimatedEnergy(), newSubList);

				if (newSum.energySum <= finalPower) {
					newSums.add(newSum);
					if (newSum.energySum > result.get().energySum) {
						result.set(newSum);
					}
				}
			});
			sums.addAll(newSums);
		});
		return result.get().subList;
	}

	private static Deque<Map.Entry<Instant, Double>> getEnergyForJobIntervals(
			final List<JobWithTime<ServerJob>> jobsWithTimeMap) {
		final Deque<Map.Entry<Instant, Double>> powerInIntervals = new ArrayDeque<>();
		final AtomicDouble lastIntervalPower = new AtomicDouble(0D);

		jobsWithTimeMap.forEach(jobWithTime -> {
			final double energy = jobWithTime.job.getEstimatedEnergy();
			if (jobWithTime.timeType.equals(START_TIME)) {
				lastIntervalPower.updateAndGet(power -> power + energy);
			} else {
				lastIntervalPower.updateAndGet(power -> power - energy);
			}
			powerInIntervals.removeIf(entry -> entry.getKey().equals(jobWithTime.time));
			powerInIntervals.addLast(Map.entry(jobWithTime.time, lastIntervalPower.get()));
		});
		return powerInIntervals;
	}

	private static <T extends PowerJob> List<JobWithTime<T>> getJobsWithTimesForInterval(final Set<T> jobList,
			final Instant startTime, final Instant endTime) {
		final Instant fixedStartTime = ofNullable(startTime).orElse(getCurrentTime());

		final List<T> jobsWithinInterval = jobList.stream()
				.filter(job -> (isNull(endTime) || requireNonNull(job.getStartTime()).isBefore(endTime)) &&
						job.getExpectedEndTime().isAfter(fixedStartTime))
				.toList();
		return jobsWithinInterval.stream()
				.map(job -> mapToJobWithTime(job, fixedStartTime, endTime))
				.flatMap(List::stream)
				.sorted(ResourcesUtilization::compareJobs)
				.toList();
	}

	private static <T extends PowerJob> List<JobWithTime<T>> mapToJobWithTime(final T job, final Instant startTime,
			final Instant endTime) {
		final Instant realStart =
				requireNonNull(job.getStartTime()).isBefore(startTime) ? startTime : job.getStartTime();
		final Instant realEnd =
				nonNull(endTime) && job.getExpectedEndTime().isAfter(endTime) ? endTime : job.getExpectedEndTime();

		return List.of(new JobWithTime<>(job, realStart, START_TIME), new JobWithTime<>(job, realEnd, END_TIME));
	}

	private static <T extends PowerJob> int compareJobs(final JobWithTime<T> job1, final JobWithTime<T> job2) {
		final int comparingTimeResult = job1.time.compareTo(job2.time);

		if (job1.job.equals(job2.job) && comparingTimeResult == 0) {
			return job1.timeType.equals(START_TIME) ? 1 : -1;
		}
		return comparingTimeResult;
	}

	/**
	 * Method subtracts from the resources, the resources given as an argument.
	 *
	 * @param initialResources    initial amounts of resources
	 * @param resourcesToSubtract resources that are to be subtracted
	 * @return difference between resources
	 */
	public static Map<String, Resource> computeResourceDifference(final Map<String, Resource> initialResources,
			final Map<String, Resource> resourcesToSubtract) {
		return initialResources.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> {
					if (resourcesToSubtract.containsKey(entry.getKey())) {
						return entry.getValue().reserveResource(resourcesToSubtract.get(entry.getKey()));
					}
					return entry.getValue();
				}));
	}

	/**
	 * Method removes from the resources, the resources given as an argument.
	 *
	 * @param initialResources  initial amounts of resources
	 * @param resourcesToRemove resources that are to be removed
	 * @return resources after removing
	 */
	public static Map<String, Resource> removeResources(final Map<String, Resource> initialResources,
			final Map<String, Resource> resourcesToRemove) {
		return initialResources.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> {
					if (resourcesToRemove.containsKey(entry.getKey())) {
						return entry.getValue().removeResourceAmounts(resourcesToRemove.get(entry.getKey()));
					}
					return entry.getValue();
				}));
	}

	/**
	 * Method returns information if the resource amount is sufficient with regard to given required amount.
	 *
	 * @param resources         owned resources
	 * @param requiredResources required amount of resource
	 * @return boolean indicating if resource amount is sufficient
	 */
	public static boolean areSufficient(final Map<String, Resource> resources,
			final Map<String, Resource> requiredResources) {
		final Map<String, Resource> filteredResources = requiredResources.entrySet().stream()
				.filter(resource -> resource.getValue().getIsRequired())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		// if server does not own all resources
		if (!resources.keySet().containsAll(filteredResources.keySet())) {
			return false;
		}

		return resources.entrySet().stream().allMatch(entry -> {
			// if given resource is not required then it is sufficient
			if (!filteredResources.containsKey(entry.getKey())) {
				return true;
			}
			final Resource resourceRequirement = requiredResources.get(entry.getKey());
			final Set<String> requiredCharacteristics = resourceRequirement.getCharacteristics().entrySet()
					.stream()
					.filter(characteristic -> characteristic.getValue().getIsRequired())
					.map(Map.Entry::getKey)
					.collect(toSet());

			// if given resource does not define all required characteristics then it is not sufficient
			if (!entry.getValue().getCharacteristics().keySet().containsAll(requiredCharacteristics)) {
				return false;
			}

			// run resource sufficiency validator
			return entry.getValue().isSufficient(resourceRequirement);
		});
	}

	/**
	 * Method aggregates two sets of resources.
	 *
	 * @param resources1 first resources
	 * @param resources2 second resources
	 * @return aggregated resources
	 */
	public static synchronized Map<String, Resource> addResources(final Map<String, Resource> resources1,
			final Map<String, Resource> resources2) {
		final Set<String> resourceKeys = union(resources1.keySet(), resources2.keySet());
		final Map<String, Resource> aggregatedResources = new HashMap<>();

		resourceKeys.forEach(key -> {
			if (resources1.containsKey(key) && resources2.containsKey(key)) {
				aggregatedResources.computeIfPresent(key,
						(_k, _v) -> resources1.get(key).addResource(resources2.get(key)));
				aggregatedResources.putIfAbsent(key, resources1.get(key).addResource(resources2.get(key)));
			} else if (!resources1.containsKey(key)) {
				aggregatedResources.computeIfPresent(key, (_k, _v) -> ImmutableResource.copyOf(resources2.get(key)));
				aggregatedResources.putIfAbsent(key, ImmutableResource.copyOf(resources2.get(key)));
			} else {
				aggregatedResources.computeIfPresent(key, (_k, _v) -> ImmutableResource.copyOf(resources1.get(key)));
				aggregatedResources.putIfAbsent(key, ImmutableResource.copyOf(resources1.get(key)));
			}
		});

		return aggregatedResources;
	}

	/**
	 * Method returns default empty resource representation
	 *
	 * @param resource resource for which empty resource is to be constructed
	 * @return resource representation when it is fully occupied
	 */
	public static Resource getDefaultEmptyResource(final Resource resource) {
		final Map<String, ResourceCharacteristic> newCharacteristics = new HashMap<>(resource.getCharacteristics());
		newCharacteristics.computeIfPresent(AMOUNT,
				(k, val) -> ImmutableResourceCharacteristic.copyOf(val).withValue(0));
		return ImmutableResource.copyOf(resource).withCharacteristics(newCharacteristics);
	}

	/**
	 * Method calculates the amount of in use resources based on given set of jobs
	 *
	 * @param jobs      set of jobs
	 * @param resources available resources
	 * @return aggregated resource values
	 */
	public static <T extends PowerJob> Map<String, Resource> getInUseResourcesForJobs(final List<T> jobs,
			final Map<String, Resource> resources) {
		final AtomicReference<String> key = new AtomicReference<>();
		return jobs.stream().map(PowerJob::getRequiredResources)
				.flatMap(resourceMap -> resourceMap.entrySet().stream())
				.filter(resourceEntry -> resources.containsKey(resourceEntry.getKey()))
				.collect(toMap(entry -> {
					key.set(entry.getKey());
					return entry.getKey();
				}, Map.Entry::getValue, (job1, job2) -> resources.get(key.get()).addResource(job1, job2)));
	}
}
