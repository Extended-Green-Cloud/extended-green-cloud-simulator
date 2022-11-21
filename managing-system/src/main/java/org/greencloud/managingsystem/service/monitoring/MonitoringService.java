package org.greencloud.managingsystem.service.monitoring;

import static org.greencloud.managingsystem.service.logs.ManagingAgentServiceLog.READ_ADAPTATION_GOALS_LOG;

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

	private final JobSuccessRatioService jobSuccessRatioService;


	public MonitoringService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
		this.jobSuccessRatioService = new JobSuccessRatioService(managingAgent);
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
				.filter(goal -> goal.id().equals(goalEnum.adaptationGoalId))
				.findFirst()
				.orElseThrow(() -> new InvalidGoalIdentifierException(goalEnum.adaptationGoalId));
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
}
