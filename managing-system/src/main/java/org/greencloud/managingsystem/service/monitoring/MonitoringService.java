package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.types.DataType.HEALTH_CHECK;
import static com.database.knowledge.types.DataType.SERVER_MONITORING;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_HEALTH_PERIOD;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_LONG_TIME_PERIOD;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_ADAPTATION_GOALS_LOG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.greencloud.managingsystem.service.monitoring.goalservices.AbstractGoalService;
import org.greencloud.managingsystem.service.monitoring.goalservices.BackUpPowerUsageService;
import org.greencloud.managingsystem.service.monitoring.goalservices.JobSuccessRatioService;
import org.greencloud.managingsystem.service.monitoring.goalservices.TrafficDistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.types.DataType;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.agent.MonitoringData;
import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.types.GoalType;
import org.greencloud.commons.exception.InvalidGoalIdentifierException;
import com.google.common.annotations.VisibleForTesting;
import org.greencloud.commons.args.agent.EGCSAgentType;

/**
 * Service containing methods connected with monitoring the quality of the system
 */
public class MonitoringService extends AbstractManagingService {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);

	private JobSuccessRatioService jobSuccessRatioService;
	private BackUpPowerUsageService backUpPowerUsageService;
	private TrafficDistributionService trafficDistributionService;

	public MonitoringService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
		this.jobSuccessRatioService = new JobSuccessRatioService(managingAgent);
		this.backUpPowerUsageService = new BackUpPowerUsageService(managingAgent);
		this.trafficDistributionService = new TrafficDistributionService(managingAgent);
	}

	/**
	 * Method is used to read from the database, the system's adaptation goals
	 */
	public void readSystemAdaptationGoals() {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			logger.info(READ_ADAPTATION_GOALS_LOG);
			managingAgent.setAdaptationGoalList(managingAgent.getAgentNode().getDatabaseClient().readAdaptationGoals());
			managingAgent.getAgentNode().registerManagingAgent(
					managingAgent.getAdaptationGoalList());
		}
	}

	/**
	 * Method retrieves service for the provided goal
	 *
	 * @param goal goal for the service
	 * @return Service for the given goal
	 */
	public AbstractGoalService getGoalService(GoalType goal) {
		return switch (goal) {
			case MAXIMIZE_JOB_SUCCESS_RATIO -> jobSuccessRatioService;
			case MINIMIZE_USED_BACKUP_POWER -> backUpPowerUsageService;
			case DISTRIBUTE_TRAFFIC_EVENLY -> trafficDistributionService;
		};
	}

	/**
	 * Method evaluates the goals' qualities and retrieves list informing about goals' fulfillment
	 *
	 * @return list indicating goals' fulfillment
	 */
	public List<Boolean> getGoalsFulfillment() {
		return Arrays.stream(GoalType.values())
				.map(goal -> getGoalService(goal).evaluateAndUpdate())
				.toList();
	}

	/**
	 * Method retrieves adaptation goal data based on given goal type
	 *
	 * @param goalEnum goal type
	 * @return adaptation goal data
	 */
	public AdaptationGoal getAdaptationGoal(final GoalType goalEnum) {
		return managingAgent.getAdaptationGoalList().stream()
				.filter(goal -> goal.id().equals(goalEnum.getAdaptationGoalId()))
				.findFirst()
				.orElseThrow(() -> new InvalidGoalIdentifierException(goalEnum.getAdaptationGoalId()));
	}

	/**
	 * Method computes current system quality indicator
	 *
	 * @return quality indicator
	 */
	public double computeSystemIndicator() {
		return Arrays.stream(GoalType.values())
				.mapToDouble(goal -> {
					final AdaptationGoal adaptationGoal = getAdaptationGoal(goal);
					final double lastMeasuredQuality = getGoalService(goal).readLastMeasuredGoalQuality();
					final double goalQuality = adaptationGoal.isAboveThreshold() ?
							lastMeasuredQuality :
							1 - lastMeasuredQuality;
					return goalQuality * adaptationGoal.weight();
				})
				.sum();
	}

	/**
	 * Method return last measured adaptation goal qualities
	 *
	 * @return map containing adaptation goal qualities
	 */
	public Map<GoalType, Double> getLastMeasuredGoalQualities() {
		return Arrays.stream(GoalType.values())
				.collect(toMap(goal -> goal, goal -> getGoalService(goal).readLastMeasuredGoalQuality()));
	}

	/**
	 * Method return current state of adaptation goal qualities
	 *
	 * @return map containing adaptation goal qualities
	 */
	public Map<GoalType, Double> getCurrentGoalQualities() {
		return Arrays.stream(GoalType.values())
				.collect(toMap(goal -> goal, goal -> getGoalService(goal).computeCurrentGoalQuality()));
	}

	/**
	 * Method verifies if given quality is withing bounds of selected adaptation goal
	 *
	 * @param quality system quality
	 * @return boolean indicating verification result
	 */
	public boolean isQualityInBounds(final double quality, final GoalType goalEnum) {
		final AdaptationGoal goal = managingAgent.monitor().getAdaptationGoal(goalEnum);
		return goal.isAboveThreshold() ?
				quality >= goal.threshold() :
				quality <= goal.threshold();
	}

	/**
	 * Method updates system statistics in GUI
	 */
	public void updateSystemStatistics() {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			final Map<Integer, Double> qualityMap = getLastMeasuredGoalQualities().entrySet().stream()
					.collect(toMap(entry -> entry.getKey().getAdaptationGoalId(), Map.Entry::getValue));
			managingAgent.getAgentNode().updateQualityIndicators(computeSystemIndicator(),
					qualityMap);
		}
	}

	/**
	 * Method retrieves list of AID's for agents of given type which are currently alive
	 *
	 * @param agentType type of the agent
	 * @return list of alive agent
	 */
	public List<String> getAliveAgents(final EGCSAgentType agentType) {
		final List<AgentData> healthAgentData =
				managingAgent.getAgentNode().getDatabaseClient()
						.readLastMonitoringDataForDataTypes(singletonList(HEALTH_CHECK),
								MONITOR_SYSTEM_DATA_HEALTH_PERIOD);

		final Predicate<MonitoringData> isAgentAlive = data -> {
			var healthData = ((HealthCheck) data);
			return healthData.alive() && healthData.agentType().equals(agentType);
		};

		return healthAgentData.stream()
				.filter(data -> isAgentAlive.test(data.monitoringData()))
				.map(AgentData::aid)
				.collect(Collectors.toSet()).stream()
				.toList();
	}

	/**
	 * Method retrieves list of AID's for active servers
	 *
	 * @return list of active servers
	 */
	public List<String> getActiveServers() {
		final List<AgentData> activeServers = getActiveServersData();
		final List<String> aliveServers = managingAgent.monitor().getAliveAgents(SERVER);

		// getting servers which are active but have not yet registered data in the database
		final List<String> activeServersWithoutData =
				managingAgent.monitor().getAgentsNotPresentInTheDatabase(activeServers, aliveServers);
		final List<String> activeServersNames = new ArrayList<>(activeServers.stream().map(AgentData::aid).toList());
		activeServersNames.addAll(activeServersWithoutData);

		return activeServersNames;
	}

	/**
	 * Method retrieves list of latest data registered by active servers
	 *
	 * @return list of active servers
	 */
	public List<AgentData> getActiveServersData() {
		final List<String> servers = getAliveAgents(SERVER);

		final Predicate<AgentData> isServerActive = data ->
				!((ServerMonitoringData) data.monitoringData()).isDisabled() &&
						servers.contains(data.aid());

		return managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(singletonList(SERVER_MONITORING)).stream()
				.filter(isServerActive)
				.toList();
	}

	/**
	 * Method retrieves alive central manager agent.
	 *
	 * @return central manager AID or null if no central manager agent is alive
	 */
	public String getAliveCentralManager() {
		return getAliveAgents(CENTRAL_MANAGER).stream()
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method retrieves list of AID's for agents which are alive and which belong to a given agent's name set
	 *
	 * @param allALiveAgents   list of agents that are alive (their AIDs)
	 * @param agentsOfInterest list of all agents of interest (their local names)
	 * @return list of alive agent (AIDs)
	 */
	public List<String> getAliveAgentsIntersection(List<String> allALiveAgents, List<String> agentsOfInterest) {
		final Predicate<String> isAgentNameValid = agentName -> agentsOfInterest.contains(agentName.split("@")[0]);

		return allALiveAgents.stream().filter(isAgentNameValid).toList();
	}

	/**
	 * Method retrieves list of AID's for agents which are alive and which belong to a given agent's name set
	 *
	 * @param agentType        type of agents
	 * @param agentsOfInterest list of all agents of interest (their local names)
	 * @return list of alive agent (AIDs)
	 */
	public List<String> getAliveAgentsIntersection(final EGCSAgentType agentType, List<String> agentsOfInterest) {
		return getAliveAgentsIntersection(getAliveAgents(agentType), agentsOfInterest);
	}

	/**
	 * Method returns list od agents which did not report the data into database but despite that should be considered
	 *
	 * @param agentsFromDatabase  - list retrieved from the database
	 * @param allConsideredAgents - list of all agents that should be considered (their AIDs or names)
	 * @return list of AIDs (or names) of agents not present in the database
	 */
	public List<String> getAgentsNotPresentInTheDatabase(final List<AgentData> agentsFromDatabase,
			final List<String> allConsideredAgents) {
		final List<String> agentWithDatabaseRecords = agentsFromDatabase.stream()
				.map(AgentData::aid)
				.map(aid -> aid.split("@")[0])
				.toList();

		return allConsideredAgents.stream()
				.filter(agent -> !agentWithDatabaseRecords.contains(agent.split("@")[0]))
				.toList();
	}

	/**
	 * Method retrieves data from the database for given agents and averages specific value using predefined
	 * averaging function
	 *
	 * @param dataType         type of the data to be retrieved
	 * @param agentsOfInterest agents which are taken into account
	 * @param averagingFunc    function used to average given entry
	 * @return map of agents and assigned to them averaged data
	 */
	public Map<String, Double> getAverageValuesForAgents(final DataType dataType, final List<String> agentsOfInterest,
			final ToDoubleFunction<AgentData> averagingFunc) {
		if (agentsOfInterest.isEmpty()) {
			return emptyMap();
		}

		final List<AgentData> agentsData = managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypeAndAID(dataType, agentsOfInterest, MONITOR_SYSTEM_DATA_LONG_TIME_PERIOD);

		final Map<String, Double> agentsWithRecordsMap = agentsData.stream()
				.collect(groupingBy(AgentData::aid, TreeMap::new, averagingDouble(averagingFunc)));
		final Map<String, Double> agentsWithNoRecords = getAgentsNotPresentInTheDatabase(agentsData, agentsOfInterest)
				.stream()
				.collect(toMap(agent -> agent, agent -> 0.0));
		agentsWithRecordsMap.putAll(agentsWithNoRecords);

		return agentsWithRecordsMap;
	}

	/**
	 * Method concatenate data of given agents with data of alive agents that has not yet registered data
	 *
	 * @param databaseAgentData data registered in the database
	 * @param agentType         type of agents for which the data is retrieved
	 * @param valueFunction     function used to retrieve given data field
	 * @param defaultValue      default value used when no data is in the database
	 * @return map of agents and assigned to them values
	 */
	public <T> Map<String, T> concatLatestAgentDataWithNotRegistered(final List<AgentData> databaseAgentData,
			final EGCSAgentType agentType, final Function<AgentData, T> valueFunction, final T defaultValue) {
		if (databaseAgentData.isEmpty()) {
			return emptyMap();
		}
		final List<String> aliveServers = managingAgent.monitor().getAliveAgents(agentType);
		final Map<String, T> agentsNotPresentInDatabase = managingAgent.monitor()
				.getAgentsNotPresentInTheDatabase(databaseAgentData, aliveServers).stream()
				.collect(toMap(aid -> aid, aid -> defaultValue));

		final Map<String, T> agentsWithDataInDatabase =
				databaseAgentData.stream().collect(toMap(AgentData::aid, valueFunction));
		agentsWithDataInDatabase.putAll(agentsNotPresentInDatabase);

		return agentsWithDataInDatabase;
	}

	/**
	 * Method returns map containing agent names and corresponding average traffic values
	 *
	 * @param agentAIDs list of AIDs of agents of interest
	 * @param dataType  type of data to retrieve
	 * @return map of agents and assigned to them averaged traffic data
	 */
	public Map<String, Double> getAverageTrafficForNetworkComponent(final List<String> agentAIDs,
			final DataType dataType) {
		final ToDoubleFunction<AgentData> getTrafficForServer =
				data -> ((NetworkComponentMonitoringData) data.monitoringData()).getCurrentTraffic();
		return getAverageValuesForAgents(dataType, agentAIDs, getTrafficForServer);
	}

	@VisibleForTesting
	protected void setJobSuccessRatioService(final JobSuccessRatioService jobSuccessRatioService) {
		this.jobSuccessRatioService = jobSuccessRatioService;
	}

	@VisibleForTesting
	protected void setBackUpPowerUsageService(final BackUpPowerUsageService backUpPowerUsageService) {
		this.backUpPowerUsageService = backUpPowerUsageService;
	}

	@VisibleForTesting
	protected void setTrafficDistributionService(final TrafficDistributionService trafficDistributionService) {
		this.trafficDistributionService = trafficDistributionService;
	}

}
