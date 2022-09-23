package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.CUT_ON_WIND_SPEED;
import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.MOCK_SOLAR_ENERGY;
import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.RATED_WIND_SPEED;
import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.TEST_MULTIPLIER;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.*;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED_JOB_STATUSES;
import static com.greencloud.application.domain.job.JobStatusEnum.ACTIVE_JOB_STATUSES;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.time.ZoneOffset.UTC;
import static java.util.Comparator.comparingLong;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.domain.job.PowerJob;
import com.gui.agents.GreenEnergyAgentNode;
import org.shredzone.commons.suncalc.SunTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.WeatherData;
import com.greencloud.application.domain.location.Location;
import org.slf4j.MDC;

/**
 * Set of methods used for managing power in Green Energy Agent
 */
public class GreenEnergyPowerManagement {

	private static final Logger logger = LoggerFactory.getLogger(GreenEnergyPowerManagement.class);

	private final GreenEnergyAgent greenEnergyAgent;
	private final int initialMaximumCapacity;
	private int currentMaximumCapacity;

	/**
	 * Class constructor
	 *
	 * @param greenEnergyAgent - agent representing given source
	 */
	public GreenEnergyPowerManagement(int initialMaximumCapacity, final GreenEnergyAgent greenEnergyAgent) {
		this.initialMaximumCapacity = initialMaximumCapacity;
		this.currentMaximumCapacity = initialMaximumCapacity;
		this.greenEnergyAgent = greenEnergyAgent;
	}

	/**
	 * Computes average power available during computation of the job being processed
	 *
	 * @param powerJob job of interest
	 * @param weather  monitoring data with com.greencloud.application.weather for requested timetable
	 * @param isNewJob flag indicating whether job of interest is a processed new job
	 * @return average available power as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getAverageAvailablePower(final PowerJob powerJob,
																  final MonitoringData weather, final boolean isNewJob) {
		var powerChart = getPowerChart(powerJob, weather, isNewJob);
		var availablePower = powerChart.values().stream().mapToDouble(a -> a).average().orElse(0.0D);
		var power = String.format("%.2f", availablePower);
		MDC.put(MDC_JOB_ID, powerJob.getJobId());
		logger.info(AVERAGE_POWER_LOG, greenEnergyAgent.getEnergyType(), power,
				powerJob.getStartTime(), powerJob.getEndTime());

		return powerChart.values().stream().anyMatch(value -> value <= 0) ?
				Optional.empty() :
				Optional.of(availablePower);
	}

	/**
	 * Computes remaining available power available in the given moment
	 *
	 * @param time    time of the check
	 * @param weather monitoring data with com.greencloud.application.weather for requested timetable
	 * @return average available power as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getRemainingAvailablePower(final Instant time, final MonitoringData weather) {
		var availablePower = getRemainingPower(time, weather);
		var power = String.format("%.2f", availablePower);
		logger.info(CURRENT_AVAILABLE_POWER_LOG, greenEnergyAgent.getEnergyType(), power, time);

		return Optional.of(availablePower).filter(powerVal -> powerVal >= 0.0);
	}

	/**
	 * Function computes available power, based on retrieved monitoring data and time
	 *
	 * @param monitoringData - com.greencloud.application.weather information
	 * @param dateTime       - time when the power will be used
	 * @return power in Watts
	 */
	public double getAvailablePower(MonitoringData monitoringData, Instant dateTime) {
		var weather = monitoringData.getDataForTimestamp(dateTime)
				.orElse(getNearestWeather(monitoringData, dateTime));
		return getAvailablePower(weather, dateTime.atZone(UTC));
	}

	/**
	 * Method returns the current power in use by the green source
	 */
	public int getCurrentPowerInUseForGreenSource() {
		return greenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(JobStatusEnum.IN_PROGRESS)
						&& isWithinTimeStamp(job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.mapToInt(job -> job.getKey().getPower())
				.sum();
	}

	/**
	 * Method changes the green source's maximum capacity
	 *
	 * @param newMaximumCapacity new maximum capacity value
	 */
	public void updateMaximumCapacity(final int newMaximumCapacity) {
		greenEnergyAgent.managePower().setMaximumCapacity(newMaximumCapacity);
		final GreenEnergyAgentNode greenEnergyAgentNode = (GreenEnergyAgentNode) greenEnergyAgent.getAgentNode();

		if (nonNull(greenEnergyAgentNode)) {
			greenEnergyAgentNode.updateMaximumCapacity(greenEnergyAgent.managePower().getMaximumCapacity());
		}
	}

	/**
	 * @return initial maximum capacity
	 */
	public int getInitialMaximumCapacity() {
		return initialMaximumCapacity;
	}

	/**
	 * @return current maximum capacity
	 */
	public int getMaximumCapacity() {
		return currentMaximumCapacity;
	}

	/**
	 * @param maximumCapacity - new maximum capacity
	 */
	public void setMaximumCapacity(int maximumCapacity) {
		this.currentMaximumCapacity = maximumCapacity;
	}

	private double getAvailablePower(WeatherData weather, ZonedDateTime dateTime) {
		return switch (greenEnergyAgent.getEnergyType()) {
			case SOLAR -> getSolarPower(weather, dateTime, greenEnergyAgent.getLocation());
			case WIND -> getWindPower(weather);
		};
	}

	private double getWindPower(WeatherData weather) {
		return currentMaximumCapacity * pow(
				(weather.getWindSpeed() + 5 - CUT_ON_WIND_SPEED) / (RATED_WIND_SPEED - CUT_ON_WIND_SPEED), 2)
				* TEST_MULTIPLIER;
	}

	private double getSolarPower(WeatherData weather, ZonedDateTime dateTime, Location location) {
		var sunTimes = getSunTimes(dateTime, location);
		var dayTime = dateTime.toLocalTime();
		if (!MOCK_SOLAR_ENERGY || (dayTime.isBefore(requireNonNull(sunTimes.getRise()).toLocalTime()) ||
				dayTime.isAfter(requireNonNull(sunTimes.getSet()).toLocalTime()))) {
			logger.debug(SOLAR_FARM_SHUTDOWN_LOG, dateTime, sunTimes.getRise(),
					sunTimes.getSet());
			return 0;
		}

		return getMaximumCapacity() * min(weather.getCloudCover() / 100 + 0.1, 1) * TEST_MULTIPLIER;
	}

	private synchronized Double getRemainingPower(Instant start, MonitoringData weather) {
		final double inUseCapacity = greenEnergyAgent.getPowerJobs().keySet().stream()
				.filter(job -> ACCEPTED_JOB_STATUSES.contains(greenEnergyAgent.getPowerJobs().get(job)) &&
						job.isExecutedAtTime(start))
				.mapToInt(PowerJob::getPower)
				.sum();
		return getCapacity(weather, start) - inUseCapacity;
	}

	private SunTimes getSunTimes(ZonedDateTime dateTime, Location location) {
		return SunTimes.compute().on(dateTime).at(location.getLatitude(), location.getLongitude()).execute();
	}

	private Double getCapacity(MonitoringData weather, Instant startTime) {
		final double availablePower = greenEnergyAgent
				.managePower()
				.getAvailablePower(weather, startTime);
		return availablePower > getMaximumCapacity() ? getMaximumCapacity() : availablePower;
	}

	private WeatherData getNearestWeather(MonitoringData monitoringData, Instant timestamp) {
		return monitoringData.getWeatherData().stream()
				.min(comparingLong(i -> Math.abs(i.getTime().getEpochSecond() - timestamp.getEpochSecond())))
				.orElseThrow(() -> new NoSuchElementException("No value present"));
	}

	private synchronized Map<Instant, Double> getPowerChart(PowerJob powerJob, final MonitoringData weather,
															final boolean isNewJob) {
		var start = powerJob.getStartTime();
		var end = powerJob.getEndTime();
		var jobStatuses = isNewJob ? ACCEPTED_JOB_STATUSES : ACTIVE_JOB_STATUSES;

		var timetable = greenEnergyAgent.manageState().getJobsTimetable(powerJob).stream()
				.filter(time -> isWithinTimeStamp(start, end, time))
				.toList();
		var powerJobs = greenEnergyAgent.getPowerJobs().keySet().stream()
				.filter(job -> jobStatuses.contains(greenEnergyAgent.getPowerJobs().get(job)))
				.toList();

		if (powerJobs.isEmpty()) {
			return timetable.stream()
					.collect(toMap(Function.identity(), time -> getCapacity(weather, time)));
		}

		return timetable.stream()
				.collect(toMap(Function.identity(), time ->
						powerJobs.stream()
								.filter(job -> job.isExecutedAtTime(time))
								.map(PowerJob::getPower)
								.map(power -> getCapacity(weather, time) - power)
								.mapToDouble(a -> a)
								.average()
								.orElseGet(() -> 0.0)));
	}
}
