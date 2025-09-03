package org.greencloud.managingsystem.service.analyzer;

import static java.lang.Math.sqrt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.math.MathOperations.computeKendallTau;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.greencloud.commons.exception.DatabaseConnectionNotAvailable;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.managingsystem.agents.ManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.systemquality.SystemQuality;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.database.knowledge.types.GoalType;
import com.google.common.annotations.VisibleForTesting;

/**
 * Service containing methods analyzing the current state of the system
 */
public class AnalyzerService extends AbstractManagingService {

	private static final LogBuilder log = constructLogBuilder(AnalyzerService.class);

	public AnalyzerService(final ManagingAgent managingAgent) {
		super(managingAgent);
	}

	/**
	 * Method is used to trigger the system analysis for the given adaptation goal
	 *
	 * @param violatedGoal adaptation goal for which the system analysis is triggered
	 */
	public void trigger(final GoalType violatedGoal) {
		logAdaptationInformation();

		if (!shouldAdaptationBeTriggered(violatedGoal)) {
			// break the loop if the severity of violation is negligible
			return;
		}
		final List<AdaptationAction> availableActions = getAdaptationActionsForGoal(violatedGoal);

		if (availableActions.isEmpty()) {
			log.messageInfo("There are no adaptation actions available! Adaptation not possible. "
					+ "Consider adding adaptation actions.");
			return;
		}

		log.messageInfo("Computing qualities of available adaptation actions...");
		final Map<AdaptationAction, Double> actionsQualityMap = availableActions.stream()
				.collect(toMap(action -> action, this::computeQualityOfAdaptationAction));
		managingAgent.plan().trigger(actionsQualityMap, violatedGoal);
	}

	@VisibleForTesting
	protected List<AdaptationAction> getAdaptationActionsForGoal(final GoalType violatedGoal) {
		return ofNullable(managingAgent.getDatabase())
				.map(TimescaleDatabase::readAdaptationActions)
				.map(actions -> actions.stream().filter(action -> action.getGoal().equals(violatedGoal)).toList())
				.orElseThrow(() -> new DatabaseConnectionNotAvailable(
						"Agent node doesn't exist - couldn't retrieve adaptation actions"));
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
			log.messageInfo("Goal {} quality has violated given threshold! Analyzing adaptation actions.", goal);
			return true;
		}
		log.messageInfo("Goal {} quality comply with the given threshold. Performing the trend analysis.", goal);

		final int windowSize = managingAgent.getProperties().getConfig().getQualityGoalsWindowSize();
		final List<SystemQuality> systemState = managingAgent.getDatabase()
				.readSystemQualityData(goal.getAdaptationGoalId(), windowSize);
		final boolean shouldIncrease = managingAgent.monitor().getAdaptationGoal(goal).isAboveThreshold();
		final int testResult = testAdaptationQualitiesForTrend(systemState);

		if ((testResult < 0 && shouldIncrease) || (testResult > 0 && !shouldIncrease)) {
			log.messageInfo("The negative trend was found in goal quality. Adapting the system in advance.");
			return true;
		}
		log.messageInfo("The goal is not tending to violate the threshold. Finishing analysis");
		return false;
	}

	/**
	 * Method uses two-tailed p-value test with:
	 * - null hypothesis equal to no correlation (no trend)
	 * - alternate hypothesis equal to existence of some correlation (trend)
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
		log.messageInfo("The analysis has finished. Correlation rank: {}", tau);

		final boolean isRejected = (zScore > 0 ? 1 - probability : probability) < 0.05;
		return !isRejected ? 0 : (tau > 0) ? 1 : -1;
	}

	private void logAdaptationInformation() {
		final double systemQuality = managingAgent.monitor().computeSystemIndicator();
		final double acceptableQuality = managingAgent.getProperties().getConfig().getSystemQualityThreshold();
		final String adaptationInfo = systemQuality > acceptableQuality ?
				"Overall system quality has dropped below a desired threshold! Analyzing system to increase overall quality..." :
				"System quality fulfills the desired threshold. Analyzing system to optimize overall quality...";
		log.messageInfo(adaptationInfo);
	}
}
