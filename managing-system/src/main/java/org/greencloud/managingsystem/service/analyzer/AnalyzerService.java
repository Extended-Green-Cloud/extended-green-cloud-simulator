package org.greencloud.managingsystem.service.analyzer;

import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.COMPUTE_ADAPTATION_ACTION_QUALITY_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.NO_ACTIONS_AVAILABLE_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.SYSTEM_QUALITY_INDICATOR_NOT_VIOLATED_LOG;
import static org.greencloud.managingsystem.service.analyzer.logs.ManagingAgentAnalyzerLog.SYSTEM_QUALITY_INDICATOR_VIOLATED_LOG;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.exception.DatabaseConnectionNotAvailable;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.goal.GoalEnum;

/**
 * Service containing methods analyzing the current state of the system
 */
public class AnalyzerService extends AbstractManagingService {

	private static final Logger logger = LoggerFactory.getLogger(AnalyzerService.class);

	public AnalyzerService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	/**
	 * Method is used to trigger the system analysis from the perspective of the violated adaptation goal
	 *
	 * @param violatedGoal adaptation goal which threshold was violated
	 */
	public void trigger(final GoalEnum violatedGoal) {
		final String adaptationInfo =
				managingAgent.monitor().computeSystemIndicator() > managingAgent.getSystemQualityThreshold() ?
						SYSTEM_QUALITY_INDICATOR_VIOLATED_LOG :
						SYSTEM_QUALITY_INDICATOR_NOT_VIOLATED_LOG;
		logger.info(adaptationInfo);
		final List<AdaptationAction> availableActions = getAdaptationActionsForGoal(violatedGoal);
		if (availableActions.isEmpty()) {
			logger.info(NO_ACTIONS_AVAILABLE_LOG);
			return;
		}
		logger.info(COMPUTE_ADAPTATION_ACTION_QUALITY_LOG);
		final Map<AdaptationAction, Double> actionsQualityMap = availableActions.stream()
				.collect(Collectors.toMap(action -> action, this::computeQualityOfAdaptationAction));

		managingAgent.plan().trigger(actionsQualityMap);
	}

	private List<AdaptationAction> getAdaptationActionsForGoal(final GoalEnum violatedGoal) {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			return managingAgent.getAgentNode().getDatabaseClient().readAdaptationActions()
					.stream()
					.filter(action -> action.getGoal().equals(violatedGoal))
					.toList();
		}
		throw new DatabaseConnectionNotAvailable("Couldn't retrieve adaptation actions");
	}

	private double computeQualityOfAdaptationAction(final AdaptationAction action) {
		return action.getActionResults().entrySet()
				.stream()
				.mapToDouble(result ->
						managingAgent.monitor().getAdaptationGoal(result.getKey()).weight() * result.getValue())
				.sum();
	}
}
