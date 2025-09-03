package org.greencloud.managingsystem.service.monitoring.goalservices;

import static com.database.knowledge.types.GoalType.MAXIMIZE_JOB_SUCCESS_RATIO;
import static org.greencloud.commons.constants.MonitoringConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.FAILED;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.NETWORK_AGENT_DATA_TYPES;

import java.util.List;

import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.managingsystem.agents.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;

/**
 * Service containing methods connected with monitoring system success ratio
 */
public class JobSuccessRatioService extends AbstractGoalService {

	private static final LogBuilder log = constructLogBuilder(JobSuccessRatioService.class);

	public JobSuccessRatioService(ManagingAgent managingAgent) {
		super(managingAgent, MAXIMIZE_JOB_SUCCESS_RATIO);
	}

	@Override
	public boolean evaluateAndUpdate() {
		log.messageInfo("Reading success ratio for clients from the database...");

		final double currentSuccessRatio = computeCurrentGoalQuality();
		final double aggregatedSuccessRatio = computeCurrentGoalQuality(
				managingAgent.getProperties().getConfig().getQualityGoalsEvaluationTimeFrame());

		if (currentSuccessRatio == DATA_NOT_AVAILABLE_INDICATOR
				|| aggregatedSuccessRatio == DATA_NOT_AVAILABLE_INDICATOR) {
			log.messageInfo("There is no client success ratio data available yet.");
			return true;
		}

		log.messageInfo("Current job success ratio: {}, Aggregated job success ratio: {}.", currentSuccessRatio,
				aggregatedSuccessRatio);
		updateGoalQuality(currentSuccessRatio);
		return false;
	}

	@Override
	public double computeCurrentGoalQuality(final int time) {
		final List<ClientMonitoringData> clientsData = readClientMonitoringData(time);

		if (clientsData.isEmpty()) {
			return DATA_NOT_AVAILABLE_INDICATOR;
		}

		final long allCount = clientsData.size();
		final long failCount = clientsData.stream()
				.filter(data -> data.getIsFinished() && data.getCurrentJobStatus().equals(FAILED))
				.count();

		return 1 - ((double) failCount / allCount);
	}

	/**
	 * Method computes the aggregated success ratio of individual network components
	 *
	 * @return boolean indicating current state of job success ratio for components
	 */
	public boolean evaluateComponentSuccessRatio() {
		log.messageInfo("Reading success ratio for individual components from the database...");

		final List<AgentData> componentsData = managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(NETWORK_AGENT_DATA_TYPES,
						managingAgent.getProperties().getConfig().getQualityGoalsEvaluationTimeFrame());

		if (componentsData.isEmpty()) {
			log.messageInfo("There is no regional manager success ratio data available yet.");
			return true;
		}
		return componentsData.stream().allMatch(this::verifySuccessRatioForComponent);
	}

	private boolean verifySuccessRatioForComponent(final AgentData component) {
		final double successRatio = ((NetworkComponentMonitoringData) component.monitoringData()).getSuccessRatio();
		boolean result = successRatio == DATA_NOT_AVAILABLE_INDICATOR || managingAgent.monitor()
				.isQualityInBounds(successRatio, MAXIMIZE_JOB_SUCCESS_RATIO);

		if (!result) {
			final String name = component.aid().split("@")[0];
			log.messageInfo(
					"Component {} does not satisfy job success ratio threshold! Job success ratio for component: {}.",
					name, successRatio);
		}

		return result;
	}
}
