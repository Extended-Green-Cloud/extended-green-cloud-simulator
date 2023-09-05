package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.CUT_ON_WIND_SPEED;
import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.INTERVAL_LENGTH_MIN;
import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.MOCK_SOLAR_ENERGY;
import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.RATED_WIND_SPEED;
import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.TEST_MULTIPLIER;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.AVERAGE_POWER_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.CURRENT_AVAILABLE_POWER_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.SOLAR_FARM_SHUTDOWN_LOG;
import static com.greencloud.application.utils.AlgorithmUtils.computeIncorrectMaximumValProbability;
import static com.greencloud.application.utils.AlgorithmUtils.getMinimalAvailableEnergyDuringTimeStamp;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.getSunTimes;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACTIVE_JOB_STATUSES;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.IN_PROGRESS;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.Comparator.comparingLong;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.shredzone.commons.suncalc.SunTimes;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.AbstractAgentManagement;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.weather.MonitoringData;
import com.greencloud.application.domain.weather.WeatherData;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.commons.domain.job.ServerJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;
import com.greencloud.commons.domain.location.Location;

/**
 * Set of methods used in managing green power of the Green Energy Source
 */
public class GreenPowerManagement extends AbstractAgentManagement {

	private static final Logger logger = getLogger(GreenPowerManagement.class);

	private final GreenEnergyAgent greenEnergyAgent;

	/**
	 * Class constructor
	 *
	 * @param greenEnergyAgent - agent representing given Green Energy Source
	 */
	public GreenPowerManagement(final GreenEnergyAgent greenEnergyAgent) {
		this.greenEnergyAgent = greenEnergyAgent;
	}

	/**
	 * Computes energy available in the Green Source at the given moment
	 *
	 * @param time    time of the check (in real time)
	 * @param weather monitoring data with weather for requested timetable
	 * @return average available energy as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getAvailableEnergy(final Instant time, final MonitoringData weather) {
		final double inUseCapacity = greenEnergyAgent.getServerJobs().entrySet().stream()
				.filter(job -> ACTIVE_JOB_STATUSES.contains(job.getValue()) && isWithinTimeStamp(job.getKey(), time))
				.map(Map.Entry::getKey)
				.mapToDouble(ServerJob::getEstimatedEnergy)
				.sum();
		final Double availablePower = getAvailableGreenEnergy(weather, time) - inUseCapacity;
		final String power = format("%.2f", availablePower);
		logger.info(CURRENT_AVAILABLE_POWER_LOG, greenEnergyAgent.getEnergyType(), power, time);

		return Optional.of(availablePower).filter(powerVal -> powerVal >= 0.0);
	}

	/**
	 * Function computes available energy, based on retrieved monitoring data and time
	 *
	 * @param monitoringData - weather information
	 * @param dateTime       - time when the power will be used
	 * @return power in Watts
	 */
	public double getAvailableGreenEnergy(final MonitoringData monitoringData, final Instant dateTime) {
		final WeatherData weather = monitoringData.getDataForTimestamp(dateTime)
				.orElse(getNearestWeather(monitoringData, dateTime));
		final double availableEnergy = getEnergyForSourceType(weather, dateTime.atZone(UTC));
		return min(availableEnergy, greenEnergyAgent.getMaximumGeneratorCapacity());
	}

	/**
	 * Method computes energy that is available during computation of the given job
	 *
	 * @param serverJob job of interest
	 * @param weather   monitoring data with weather for requested job time frames
	 * @param isNewJob  flag indicating whether job of interest is a new job or already added job
	 * @return available energy as decimal or empty optional if energy is not available
	 */
	public synchronized Optional<Double> getAvailableEnergy(final ServerJob serverJob, final MonitoringData weather,
			final boolean isNewJob) {
		final Set<JobExecutionStatusEnum> jobStatuses = isNewJob ? ACCEPTED_JOB_STATUSES : ACTIVE_JOB_STATUSES;
		final Set<ServerJob> serverJobsOfInterest = greenEnergyAgent.getServerJobs().entrySet().stream()
				.filter(job -> jobStatuses.contains(job.getValue()))
				.map(Map.Entry::getKey)
				.map(JobMapper::mapToServerJobRealTime)
				.collect(toSet());

		final Instant realJobStartTime = convertToRealTime(serverJob.getStartTime());
		final Instant realJobEndTime = convertToRealTime(serverJob.getEndTime());

		final double availableEnergy =
				getMinimalAvailableEnergyDuringTimeStamp(
						serverJobsOfInterest,
						realJobStartTime,
						realJobEndTime,
						INTERVAL_LENGTH_MIN,
						this,
						greenEnergyAgent.getMaximumGeneratorCapacity(),
						weather);
		final String power = format("%.2f", availableEnergy);

		MDC.put(MDC_JOB_ID, serverJob.getJobId());
		logger.info(AVERAGE_POWER_LOG, greenEnergyAgent.getEnergyType(), power, realJobStartTime, realJobEndTime);
		return Optional.of(availableEnergy).filter(powerVal -> powerVal > 0.0);
	}

	/**
	 * Method retrieves combined weather prediction error and the available power calculation error
	 * It was assumed that the smallest time interval unit is equal 10 min
	 *
	 * @param job job of interest
	 * @return entire power calculation error
	 */
	public double computeCombinedPowerError(final ServerJob job) {
		final Instant realJobStartTime = convertToRealTime(job.getStartTime());
		final Instant realJobEndTime = convertToRealTime(job.getEndTime());
		final double availablePowerError = computeIncorrectMaximumValProbability(realJobStartTime, realJobEndTime,
				INTERVAL_LENGTH_MIN);

		return min(1, availablePowerError + greenEnergyAgent.getWeatherPredictionError());
	}

	/**
	 * Method computes the current energy usage percentage (with respect to maximal generator capacity).
	 *
	 * @return energy percentage
	 */
	public double getCurrentEnergyPercentage() {
		return greenEnergyAgent.getMaximumGeneratorCapacity() == 0 ? 0 :
				getCurrentEnergyInUse() / (double) greenEnergyAgent.getMaximumGeneratorCapacity();
	}

	/**
	 * Method computes the energy usage percentage (with respect to maximal generator capacity).
	 *
	 * @param availableEnergy amount of available energy
	 * @return energy percentage
	 */
	public double getEnergyPercentage(final double availableEnergy) {
		return greenEnergyAgent.getMaximumGeneratorCapacity() == 0 ? 0 :
				availableEnergy / (double) greenEnergyAgent.getMaximumGeneratorCapacity();
	}

	/**
	 * Method computes currently used amount of energy.
	 *
	 * @return energy in use
	 */
	public double getCurrentEnergyInUse() {
		return greenEnergyAgent.getServerJobs().entrySet()
				.stream()
				.filter(job -> job.getValue().equals(IN_PROGRESS))
				.mapToDouble(job -> job.getKey().getEstimatedEnergy())
				.sum();
	}

	private double getEnergyForSourceType(final WeatherData weather, final ZonedDateTime dateTime) {
		return switch (greenEnergyAgent.getEnergyType()) {
			case SOLAR -> getSolarEnergy(weather, dateTime, greenEnergyAgent.getLocation());
			case WIND -> getWindEnergy(weather);
		};
	}

	private double getWindEnergy(WeatherData weather) {
		return greenEnergyAgent.getMaximumGeneratorCapacity() * pow((weather.getWindSpeed() + 5 - CUT_ON_WIND_SPEED)
				/ (RATED_WIND_SPEED - CUT_ON_WIND_SPEED), 2) * TEST_MULTIPLIER;
	}

	private double getSolarEnergy(WeatherData weather, ZonedDateTime dateTime, Location location) {
		final SunTimes sunTimes = getSunTimes(dateTime, location);
		final LocalTime dayTime = dateTime.toLocalTime();

		if (!MOCK_SOLAR_ENERGY || (dayTime.isBefore(requireNonNull(sunTimes.getRise()).toLocalTime()) ||
				dayTime.isAfter(requireNonNull(sunTimes.getSet()).toLocalTime()))) {
			logger.trace(SOLAR_FARM_SHUTDOWN_LOG, dateTime, sunTimes.getRise(), sunTimes.getSet());
			return 0;
		}

		return greenEnergyAgent.getMaximumGeneratorCapacity() * min(weather.getCloudCover() / 100 + 0.1, 1)
				* TEST_MULTIPLIER;
	}

	private WeatherData getNearestWeather(final MonitoringData monitoringData, final Instant timestamp) {
		return monitoringData.getWeatherData().stream()
				.min(comparingLong(i -> Math.abs(i.getTime().getEpochSecond() - timestamp.getEpochSecond())))
				.orElseThrow(() -> new NoSuchElementException("No value present"));
	}
}
