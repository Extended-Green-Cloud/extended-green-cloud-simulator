package org.greencloud.commons.args.agent.greenenergy.agent;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.Comparator.comparingLong;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.COEFFICIENT_OF_PERFORMANCE;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.ROTOR_SWEPT_AREA;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.TEST_MULTIPLIER;
import static org.greencloud.commons.args.agent.greenenergy.agent.logs.GreenEnergyAgentPropsLog.CURRENT_AVAILABLE_POWER_LOG;
import static org.greencloud.commons.args.agent.greenenergy.agent.logs.GreenEnergyAgentPropsLog.POWER_JOB_ACCEPTED_LOG;
import static org.greencloud.commons.args.agent.greenenergy.agent.logs.GreenEnergyAgentPropsLog.POWER_JOB_FAILED_LOG;
import static org.greencloud.commons.args.agent.greenenergy.agent.logs.GreenEnergyAgentPropsLog.POWER_JOB_FINISH_LOG;
import static org.greencloud.commons.args.agent.greenenergy.agent.logs.GreenEnergyAgentPropsLog.POWER_JOB_START_LOG;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACTIVE_JOB_STATUSES;
import static org.greencloud.commons.enums.job.JobIdentificationEnum.JOB_INSTANCE_ID;
import static org.greencloud.commons.utils.time.TimeComparator.isWithinTimeStamp;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.greencloud.commons.args.agent.EGCSAgentProps;
import org.greencloud.commons.args.agent.EGCSAgentType;
import org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.counter.JobCounter;
import org.greencloud.commons.domain.job.duration.JobExecutionDuration;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.domain.weather.WeatherData;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.greencloud.commons.enums.job.JobExecutionResultEnum;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.commons.utils.math.MathOperations;
import org.greencloud.commons.utils.resources.ResourcesUtilization;
import org.greencloud.commons.utils.time.TimeSimulation;
import org.jrba.rulesengine.constants.LoggingConstants;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.shredzone.commons.suncalc.SunTimes;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Green Energy Agent
 */
@Getter
@Setter
public class GreenEnergyAgentProps extends EGCSAgentProps {

	private static final Logger logger = getLogger(GreenEnergyAgentProps.class);
	private final AtomicInteger shortagesAccumulator;
	private final AtomicInteger weatherShortagesCounter;
	protected GreenSourceDisconnectionProps greenSourceDisconnection;
	protected ConcurrentMap<ServerJob, JobExecutionStatusEnum> serverJobs;
	protected ConcurrentMap<ServerJob, Integer> ruleSetForJob;
	protected JobExecutionDuration<ServerJob> jobsExecutionTime;
	protected ConcurrentMap<ServerJob, Double> priceForJob;
	protected Location location;
	protected GreenEnergySourceTypeEnum energyType;
	protected AID monitoringAgent;
	protected AID ownerServer;
	protected double pricePerPowerUnit;
	protected double weatherPredictionError;
	protected int maximumGeneratorCapacity;
	protected boolean hasError;

	public GreenEnergyAgentProps(final String agentName) {
		super(EGCSAgentType.GREEN_ENERGY, agentName);
		this.greenSourceDisconnection = new GreenSourceDisconnectionProps();
		this.shortagesAccumulator = new AtomicInteger(0);
		this.weatherShortagesCounter = new AtomicInteger(0);
		this.hasError = false;
		this.serverJobs = new ConcurrentHashMap<>();
		this.ruleSetForJob = new ConcurrentHashMap<>();
		this.jobsExecutionTime = new JobExecutionDuration<>(JOB_INSTANCE_ID);
		this.priceForJob = new ConcurrentHashMap<>();
	}

	/**
	 * Constructor
	 *
	 * @param agentName                name of the agent
	 * @param location                 location of the Green Source
	 * @param energyType               type of the Green Source
	 * @param monitoringAgent          agent with which Green Source communicate to retrieve weather
	 * @param ownerServer              Server Agent to which initially the Green Source is connected
	 * @param pricePerPowerUnit        price for energy production (per kWh)
	 * @param weatherPredictionError   estimated error in prediction of weather conditions
	 * @param maximumGeneratorCapacity capacity of energy generator
	 */
	public GreenEnergyAgentProps(final String agentName, final Location location,
			final GreenEnergySourceTypeEnum energyType, final AID monitoringAgent, final AID ownerServer,
			final double pricePerPowerUnit, final double weatherPredictionError, final int maximumGeneratorCapacity) {
		this(agentName);

		this.location = location;
		this.energyType = energyType;
		this.monitoringAgent = monitoringAgent;
		this.ownerServer = ownerServer;
		this.pricePerPowerUnit = pricePerPowerUnit;
		this.weatherPredictionError = weatherPredictionError;
		this.maximumGeneratorCapacity = maximumGeneratorCapacity;
	}

	/**
	 * Method adds new client job
	 *
	 * @param job     job that is to be added
	 * @param ruleSet rule set with which the job is to be handled
	 * @param status  status of the job
	 */
	public void addJob(final ServerJob job, final Integer ruleSet, final JobExecutionStatusEnum status) {
		serverJobs.put(job, status);
		jobsExecutionTime.addDurationMap(job);
		ruleSetForJob.put(job, ruleSet);
	}

	/**
	 * Method removes client job
	 *
	 * @param job job that is to be removed
	 */
	public void removeJob(final ServerJob job) {
		serverJobs.remove(job);
		priceForJob.remove(job);
		jobsExecutionTime.removeDurationMap(job);
		ruleSetForJob.remove(job);
	}

	/**
	 * Method computes final cost of job instance execution
	 *
	 * @param job job for which cost is to be calculated
	 */
	public double computeFinalJobInstancePrice(final ServerJob job) {
		final double price = priceForJob.get(job);
		return jobsExecutionTime.computeFinalPrice(job, price);
	}

	/**
	 * Method computes currently used amount of energy.
	 *
	 * @return energy in use
	 */
	public double getCurrentEnergyInUse() {
		return serverJobs.entrySet()
				.stream()
				.filter(job -> job.getValue().equals(JobExecutionStatusEnum.IN_PROGRESS))
				.mapToDouble(job -> job.getKey().getEstimatedEnergy())
				.sum();
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
		final Instant realJobEndTime = convertToRealTime(job.getExpectedEndTime());
		final double availablePowerError = MathOperations.computeIncorrectMaximumValProbability(realJobStartTime,
				realJobEndTime, GreenEnergyAgentPropsConstants.INTERVAL_LENGTH_MIN);

		return min(1, availablePowerError + weatherPredictionError);
	}

	/**
	 * Computes energy available in the Green Source at the given moment
	 *
	 * @param time    time of the check (in real time)
	 * @param weather monitoring data with weather for requested timetable
	 * @return average available energy as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getAvailableEnergy(final Instant time, final MonitoringData weather) {
		final double inUseCapacity = serverJobs.entrySet().stream()
				.filter(job -> ACTIVE_JOB_STATUSES.contains(job.getValue()) && isWithinTimeStamp(job.getKey(), time))
				.map(Map.Entry::getKey)
				.mapToDouble(ServerJob::getEstimatedEnergy)
				.sum();
		final Double availablePower = (hasError ? 0 : getAvailableGreenEnergy(weather, time)) - inUseCapacity;
		final String power = format("%.2f", availablePower);
		logger.info(CURRENT_AVAILABLE_POWER_LOG, energyType, power, time);

		return Optional.of(availablePower).filter(powerVal -> powerVal >= 0.0);
	}

	/**
	 * Method computes the energy usage percentage (with respect to maximal generator capacity).
	 *
	 * @param availableEnergy amount of available energy
	 * @return energy percentage
	 */
	public double getEnergyPercentage(final double availableEnergy) {
		return maximumGeneratorCapacity == 0 ? 0 : availableEnergy / (double) maximumGeneratorCapacity;
	}

	/**
	 * Function computes available energy, based on retrieved monitoring data and time
	 *
	 * @param monitoringData - weather information
	 * @param dateTime       - time when the power will be used
	 * @return power in Watts
	 */
	public double getAvailableGreenEnergy(final MonitoringData monitoringData, final Instant dateTime) {
		if (hasError) {
			return 0D;
		}
		final WeatherData weather = monitoringData.getDataForTimestamp(dateTime)
				.orElse(getNearestWeather(monitoringData, dateTime));
		final double availableEnergy = getEnergyForSourceType(weather, dateTime.atZone(UTC));
		return min(availableEnergy, maximumGeneratorCapacity);
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
		final Set<JobExecutionStatusEnum> jobStatuses = isNewJob ?
				JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES :
				ACTIVE_JOB_STATUSES;
		final Set<ServerJob> serverJobsOfInterest = serverJobs.entrySet().stream()
				.filter(job -> jobStatuses.contains(job.getValue()))
				.map(Map.Entry::getKey)
				.map(JobMapper::mapToServerJobRealTime)
				.collect(toSet());

		final Instant realJobStartTime = convertToRealTime(serverJob.getStartTime());
		final Instant realJobEndTime = convertToRealTime(serverJob.getExpectedEndTime());

		final double availableEnergy = hasError ? 0.0 :
				ResourcesUtilization.getMinimalAvailableEnergyDuringTimeStamp(
						serverJobsOfInterest,
						realJobStartTime,
						realJobEndTime,
						GreenEnergyAgentPropsConstants.INTERVAL_LENGTH_MIN,
						this,
						weather);
		final String power = format("%.2f", availableEnergy);

		MDC.put(LoggingConstants.MDC_JOB_ID, serverJob.getJobId());
		logger.info("Calculated available {} average energy {} between {} and {}", energyType, power, realJobStartTime,
				realJobEndTime);
		return Optional.of(availableEnergy).filter(powerVal -> powerVal > 0.0);
	}

	/**
	 * Method creates new instances for given server job that will be affected by the power shortage and executes
	 * the post job division handler.
	 *
	 * @param job                job that is to be divided into instances
	 * @param powerShortageStart time when the power shortage will start
	 * @return Pair consisting of previous job instance and job instance for transfer (if there is only job instance
	 * * for transfer then previous job instance element is null)
	 */
	public RuleSetFacts divideJobForPowerShortage(final ServerJob job, final Instant powerShortageStart,
			final RuleSetFacts facts) {
		return super.divideJobForPowerShortage(job, powerShortageStart, serverJobs, facts, ruleSetForJob);
	}

	/**
	 * Method substitutes existing job instance with new instances associated with power shortage transfer
	 *
	 * @param jobTransfer job transfer information
	 * @param originalJob original job that is to be divided
	 */
	public RuleSetFacts divideJobForPowerShortage(final JobPowerShortageTransfer jobTransfer,
			final ServerJob originalJob, final RuleSetFacts facts) {
		return super.divideJobForPowerShortage(jobTransfer, originalJob, serverJobs, facts, ruleSetForJob);
	}

	@Override
	protected ConcurrentMap<JobExecutionResultEnum, JobCounter> getJobCountersMap() {
		return new ConcurrentHashMap<>(Map.of(
				JobExecutionResultEnum.FAILED, new JobCounter(jobId ->
						logger.info(POWER_JOB_FAILED_LOG, jobCounters.get(JobExecutionResultEnum.FAILED).getCount())),
				JobExecutionResultEnum.ACCEPTED, new JobCounter(jobId ->
						logger.info(POWER_JOB_ACCEPTED_LOG,
								jobCounters.get(JobExecutionResultEnum.ACCEPTED).getCount())),
				JobExecutionResultEnum.STARTED, new JobCounter(jobId ->
						logger.info(POWER_JOB_START_LOG, jobId,
								jobCounters.get(JobExecutionResultEnum.STARTED).getCount(),
								jobCounters.get(JobExecutionResultEnum.ACCEPTED).getCount())),
				JobExecutionResultEnum.FINISH, new JobCounter(jobId ->
						logger.info(POWER_JOB_FINISH_LOG, jobId,
								jobCounters.get(JobExecutionResultEnum.FINISH).getCount(),
								jobCounters.get(JobExecutionResultEnum.STARTED).getCount()))
		));
	}

	private double getEnergyForSourceType(final WeatherData weather, final ZonedDateTime dateTime) {
		return switch (energyType) {
			case SOLAR -> getSolarEnergy(weather, dateTime, location);
			case WIND -> getWindEnergy(weather);
		};
	}

	/**
	 * @implNote Formula: P (Watts) = 0.5 * p * R^2 * Cp * V^3  where:
	 * <p> p - air density (kg/m^3) </p>
	 * <p> R - rotor swept area (m^3) </p>
	 * <p> Cp - coefficient of performance </p>
	 * <p> V - wind speed (m/s) </p>
	 */
	private double getWindEnergy(WeatherData weather) {
		return 0.5 * COEFFICIENT_OF_PERFORMANCE * pow(ROTOR_SWEPT_AREA, 2) * weather.getAirDensity() * pow(
				weather.getWindSpeed(), 3) * TEST_MULTIPLIER;
	}

	private double getSolarEnergy(WeatherData weather, ZonedDateTime dateTime, Location location) {
		final SunTimes sunTimes = TimeSimulation.getSunTimes(dateTime, location);
		final LocalTime dayTime = dateTime.toLocalTime();

		if (!GreenEnergyAgentPropsConstants.MOCK_SOLAR_ENERGY || (
				dayTime.isBefore(requireNonNull(sunTimes.getRise()).toLocalTime()) ||
						dayTime.isAfter(requireNonNull(sunTimes.getSet()).toLocalTime()))) {
			logger.trace("SOLAR farm is shutdown at {}, sunrise at {} & sunset at {}", dateTime, sunTimes.getRise(),
					sunTimes.getSet());
			return 0;
		}

		return maximumGeneratorCapacity * min(weather.getCloudCover() / 100 + 0.1, 1)
				* TEST_MULTIPLIER;
	}

	private WeatherData getNearestWeather(final MonitoringData monitoringData, final Instant timestamp) {
		return monitoringData.getWeatherData().stream()
				.min(comparingLong(i -> Math.abs(i.getTime().getEpochSecond() - timestamp.getEpochSecond())))
				.orElseThrow(() -> new NoSuchElementException("No value present"));
	}
}
