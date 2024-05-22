package org.greencloud.managingsystem.service.analyzer;

import static org.greencloud.commons.utils.math.MathOperations.computeKendallTau;
import static java.lang.Math.sqrt;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.AGGREGATION_SIZE;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.COMPUTE_ADAPTATION_ACTION_QUALITY_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.GOAL_QUALITY_ABOVE_THRESHOLD_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.GOAL_QUALITY_BELOW_THRESHOLD_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.GOAL_STATISTIC_ANALYSIS_RESULT_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.GOAL_TREND_FOUND_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.GOAL_TREND_NOT_FOUND_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.NO_ACTIONS_AVAILABLE_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.SYSTEM_QUALITY_INDICATOR_NOT_VIOLATED_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.SYSTEM_QUALITY_INDICATOR_VIOLATED_LOG;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.commons.exception.DatabaseConnectionNotAvailable;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.types.GoalType;
import com.database.knowledge.domain.systemquality.SystemQuality;
import com.google.common.annotations.VisibleForTesting;

/**
 * Service containing methods analyzing the current state of the system
 */
public class AnalyzerService extends AbstractManagingService {

	private static final Logger logger = LoggerFactory.getLogger(AnalyzerService.class);

	public AnalyzerService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	/**
	 * Method is used to trigger the system analysis for the given adaptation goal
	 *
	 * @param violatedGoal adaptation goal for which the system analysis is triggered
	 */
	public void trigger(final GoalType violatedGoal) {
		final String adaptationInfo =
				managingAgent.monitor().computeSystemIndicator() > managingAgent.getSystemQualityThreshold() ?
						SYSTEM_QUALITY_INDICATOR_VIOLATED_LOG :
						SYSTEM_QUALITY_INDICATOR_NOT_VIOLATED_LOG;
		logger.info(adaptationInfo);

		if (shouldAdaptationBeTriggered(violatedGoal)) {
			final List<AdaptationAction> availableActions = getAdaptationActionsForGoal(violatedGoal);

			if (availableActions.isEmpty()) {
				logger.info(NO_ACTIONS_AVAILABLE_LOG);
				return;
			}

			logger.info(COMPUTE_ADAPTATION_ACTION_QUALITY_LOG);
			final Map<AdaptationAction, Double> actionsQualityMap = availableActions.stream()
					.collect(toMap(action -> action, this::computeQualityOfAdaptationAction));

			managingAgent.plan().trigger(actionsQualityMap, violatedGoal);
		}
	}

	@VisibleForTesting
	protected List<AdaptationAction> getAdaptationActionsForGoal(final GoalType violatedGoal) {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			return managingAgent.getAgentNode().getDatabaseClient().readAdaptationActions().stream()
					.filter(action -> action.getGoal().equals(violatedGoal))
					.toList();
		}
		throw new DatabaseConnectionNotAvailable("Agent node doesn't exist - couldn't retrieve adaptation actions");
	}

	@VisibleForTesting
	protected double computeQualityOfAdaptationAction(final AdaptationAction action) {
		return action.getActionResultDifferences().entrySet().stream()
				.mapToDouble(result ->
						managingAgent.monitor().getAdaptationGoal(result.getKey()).weight() * result.getValue())
				.sum();
	}

	private boolean shouldAdaptationBeTriggered(final GoalType goal) {
		final double systemQualityForGoal = managingAgent.monitor().getLastMeasuredGoalQualities().get(goal);

		if (!managingAgent.monitor().isQualityInBounds(systemQualityForGoal, goal)) {
			logger.info(GOAL_QUALITY_BELOW_THRESHOLD_LOG, goal);
			return true;
		}
		logger.info(GOAL_QUALITY_ABOVE_THRESHOLD_LOG, goal);

		final List<SystemQuality> systemQualities = managingAgent.getAgentNode().getDatabaseClient()
				.readSystemQualityData(goal.getAdaptationGoalId(), AGGREGATION_SIZE);
		final boolean shouldIncrease = managingAgent.monitor().getAdaptationGoal(goal).isAboveThreshold();
		final int testResult = testAdaptationQualitiesForTrend(systemQualities);

		if ((testResult < 0 && shouldIncrease) || (testResult > 0 && !shouldIncrease)) {
			logger.info(GOAL_TREND_FOUND_LOG);
			return true;
		}
		logger.info(GOAL_TREND_NOT_FOUND_LOG);
		return false;
	}

	/**
	 * Method uses two-tailed p-value test with:
	 * - null hypothesis equal to no correlation (no trend)
	 * - alternate hypothesis equal to existence of some correlation (trend)
	 *
	 * Method uses z-score as a statistics is approximated to a standard normal.
	 * Test params: CI = 95%, p-value < 0.05 speaks for rejection
	 *
	 * @return int indicating the result of the test:
	 * - 1 - rejection H0 and trend existence (trend for increasing value),
	 * - -1 - rejection H0 and trend existence (trend for decreasing value),
	 * - 0 - trend is unknown
	 */
	@VisibleForTesting
	protected int testAdaptationQualitiesForTrend(final List<SystemQuality> systemQualities) {
		final List<Instant> qualityInstants = systemQualities.stream().map(SystemQuality::timestamp).toList();
		final List<Double> qualityValues = systemQualities.stream().map(SystemQuality::quality).toList();

		if (qualityInstants.size() < 4 || qualityValues.stream().allMatch(qualityValues.get(0)::equals)) {
			return 0;
		}

		final double tau = computeKendallTau(qualityInstants, qualityValues);
		final int N = qualityInstants.size();
		final double zScore = (3 * tau * sqrt(N * (double) (N - 1))) / (sqrt(2 * (double) (2 * N + 5)));
		final double probability = new NormalDistribution().cumulativeProbability(zScore);
		logger.info(GOAL_STATISTIC_ANALYSIS_RESULT_LOG, tau);

		final boolean isRejected = (zScore > 0 ? 1 - probability : probability) < 0.05;
		return !isRejected ? 0 : (tau > 0) ? 1 : -1;
	}
}
