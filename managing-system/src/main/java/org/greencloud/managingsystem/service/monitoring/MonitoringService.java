package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_ADAPTATION_GOALS_LOG;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.exception.InvalidGoalIdentifierException;
import com.google.common.annotations.VisibleForTesting;
import com.gui.agents.ManagingAgentNode;

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
	 * Get service for the provided goal
	 *
	 * @param goal goal for the service
	 * @return Service for the given goal
	 */
	public AbstractGoalService getGoalService(GoalEnum goal) {
		return switch (goal) {
			case MAXIMIZE_JOB_SUCCESS_RATIO -> jobSuccessRatioService;
			case MINIMIZE_USED_BACKUP_POWER -> backUpPowerUsageService;
			case DISTRIBUTE_TRAFFIC_EVENLY -> trafficDistributionService;
		};
	}

	/**
	 * Method is used to read from the database, the system's adaptation goals
	 */
	public void readSystemAdaptationGoals() {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			logger.info(READ_ADAPTATION_GOALS_LOG);
			managingAgent.setAdaptationGoalList(managingAgent.getAgentNode().getDatabaseClient().readAdaptationGoals());
			((ManagingAgentNode) managingAgent.getAgentNode()).registerManagingAgent(
					managingAgent.getAdaptationGoalList());
		}
	}

	/**
	 * Method retrieves adaptation goal data based on given goal type
	 *
	 * @param goalEnum goal type
	 * @return adaptation goal data
	 */
	public AdaptationGoal getAdaptationGoal(final GoalEnum goalEnum) {
		return managingAgent.getAdaptationGoalList().stream()
				.filter(goal -> goal.id().equals(goalEnum.getAdaptationGoalId()))
				.findFirst()
				.orElseThrow(() -> new InvalidGoalIdentifierException(goalEnum.getAdaptationGoalId()));
	}

	/**
	 * Method calls Job Success Ratio Service and retrieved the information if success ratio goal is satisfied
	 *
	 * @return boolean indication if success ratio goal is satisfied
	 */
	public boolean isSuccessRatioMaximized() {
		final boolean clientSuccessRatio = jobSuccessRatioService.evaluateAndUpdateClientJobSuccessRatio();
		final boolean networkSuccessRatio = jobSuccessRatioService.evaluateComponentSuccessRatio();

		return clientSuccessRatio && networkSuccessRatio;
	}

	/**
	 * Mehod calls Traffic Distribution Service and retrieves the information if traffic distribution goal is satisfied
	 *
	 * @return boolean indication if traffic distribution goal is satisfied
	 */
	public boolean isTrafficDistributedEvenly() {
		return trafficDistributionService.evaluateAndUpdateTrafficDistributionGoal();
	}

	/**
	 * Method computes current system quality indicator
	 *
	 * @return quality indicator
	 */
	public double computeSystemIndicator() {
		final double successRatio = jobSuccessRatioService.getLastMeasuredGoalQuality();
		final double backUpUsage = 1 - backUpPowerUsageService.getLastMeasuredGoalQuality();
		final double trafficDistribution = trafficDistributionService.getLastMeasuredGoalQuality();

		return successRatio * getAdaptationGoal(MAXIMIZE_JOB_SUCCESS_RATIO).weight() +
				backUpUsage * getAdaptationGoal(MINIMIZE_USED_BACKUP_POWER).weight() +
				trafficDistribution * getAdaptationGoal(DISTRIBUTE_TRAFFIC_EVENLY).weight();
	}

	/**
	 * Method return current state of adaptation goal qualities
	 *
	 * @return map containing adaptation goal qualities
	 */
	public Map<GoalEnum, Double> getCurrentGoalQualities() {
		final double successRatio = jobSuccessRatioService.getLastMeasuredGoalQuality();
		final double backUpUsage = backUpPowerUsageService.getLastMeasuredGoalQuality();
		final double trafficDistribution = trafficDistributionService.getLastMeasuredGoalQuality();
		return Map.of(
				MAXIMIZE_JOB_SUCCESS_RATIO, successRatio,
				MINIMIZE_USED_BACKUP_POWER, backUpUsage,
				DISTRIBUTE_TRAFFIC_EVENLY, trafficDistribution
		);
	}

	/**
	 * Method verifies if given quality is withing bounds of selected adaptation goal
	 *
	 * @param quality system quality
	 * @return boolean indicating verification result
	 */
	public boolean isQualityInBounds(final double quality, final GoalEnum goalEnum) {
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
			final Map<Integer, Double> qualityMap = getCurrentGoalQualities().entrySet().stream()
					.collect(Collectors.toMap(entry -> entry.getKey().getAdaptationGoalId(), Map.Entry::getValue));
			((ManagingAgentNode) managingAgent.getAgentNode()).updateQualityIndicators(computeSystemIndicator(),
					qualityMap);
		}
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
