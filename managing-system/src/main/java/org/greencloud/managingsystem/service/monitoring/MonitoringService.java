package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static org.greencloud.managingsystem.service.logs.ManagingAgentServiceLog.READ_ADAPTATION_GOALS_LOG;

import java.util.Map;
import java.util.Objects;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.exception.InvalidGoalIdentifierException;
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
		final boolean clientSuccessRatio = jobSuccessRatioService.isClientJobSuccessRatioCorrect();
		final boolean networkSuccessRatio = jobSuccessRatioService.isComponentsSuccessRatioCorrect();

		return clientSuccessRatio && networkSuccessRatio;
	}

	/**
	 * Method computes current system quality indicator
	 *
	 * @return quality indicator
	 */
	public double computeSystemIndicator() {
		final double successRatio = jobSuccessRatioService.getJobSuccessRatio();
		final double backUpUsage = backUpPowerUsageService.getBackUpPowerUsage();
		final double trafficDistribution = trafficDistributionService.getAverageTrafficDistribution();

		return successRatio * getAdaptationGoal(MAXIMIZE_JOB_SUCCESS_RATIO).weight() +
				backUpUsage * getAdaptationGoal(MINIMIZE_USED_BACKUP_POWER).weight() +
				trafficDistribution * getAdaptationGoal(DISTRIBUTE_TRAFFIC_EVENLY).weight();
	}

	/**
	 * Method updates system statistics in GUI
	 */
	public void updateSystemStatistics() {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			final double successRatio = jobSuccessRatioService.getJobSuccessRatio();
			final double backUpUsage = backUpPowerUsageService.getBackUpPowerUsage();
			final double trafficDistribution = trafficDistributionService.getAverageTrafficDistribution();
			final Map<Integer, Double> qualityMap = Map.of(
					MAXIMIZE_JOB_SUCCESS_RATIO.getAdaptationGoalId(), successRatio,
					MINIMIZE_USED_BACKUP_POWER.getAdaptationGoalId(), backUpUsage,
					DISTRIBUTE_TRAFFIC_EVENLY.getAdaptationGoalId(), trafficDistribution
			);

			((ManagingAgentNode) managingAgent.getAgentNode()).updateQualityIndicators(computeSystemIndicator(),
					qualityMap);
		}
	}

	public void setJobSuccessRatioService(final JobSuccessRatioService jobSuccessRatioService) {
		this.jobSuccessRatioService = jobSuccessRatioService;
	}

	public void setBackUpPowerUsageService(final BackUpPowerUsageService backUpPowerUsageService) {
		this.backUpPowerUsageService = backUpPowerUsageService;
	}

	public void setTrafficDistributionService(final TrafficDistributionService trafficDistributionService) {
		this.trafficDistributionService = trafficDistributionService;
	}

}
