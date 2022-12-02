package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static java.util.Collections.singletonList;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_AGGREGATED_PERIOD;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.NETWORK_AGENT_DATA_TYPES;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_SUCCESS_RATIO_CLIENTS_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_SUCCESS_RATIO_CLIENT_NO_DATA_YET_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_SUCCESS_RATIO_COMPONENTS_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_SUCCESS_RATIO_NETWORK_DATA_YET_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.SUCCESS_RATIO_CLIENT_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.SUCCESS_RATIO_UNSATISFIED_COMPONENT_LOG;

import java.util.List;

import com.greencloud.commons.job.ClientJobStatusEnum;
import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;

/**
 * Service containing methods connected with monitoring system success ratio
 */
public class JobSuccessRatioService extends AbstractGoalService {

	private static final Logger logger = LoggerFactory.getLogger(JobSuccessRatioService.class);

	private static final GoalEnum GOAL = MAXIMIZE_JOB_SUCCESS_RATIO;

	public JobSuccessRatioService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	/**
	 * Method evaluates the job success ratio for overall job execution (aggregated and at the current moment)
	 *
	 * @param time time used to retrieve current system data
	 * @return boolean indicating if the analyzer should be triggered
	 */
	public boolean evaluateAndUpdateClientJobSuccessRatio(final int time) {
		logger.info(READ_SUCCESS_RATIO_CLIENTS_LOG);
		final double currentSuccessRatio = readCurrentGoalQuality(time);
		final double aggregatedSuccessRatio = readCurrentGoalQuality(MONITOR_SYSTEM_DATA_AGGREGATED_PERIOD);

		if (currentSuccessRatio == DATA_NOT_AVAILABLE_INDICATOR
				|| aggregatedSuccessRatio == DATA_NOT_AVAILABLE_INDICATOR) {
			logger.info(READ_SUCCESS_RATIO_CLIENT_NO_DATA_YET_LOG);
			return true;
		}

		logger.info(SUCCESS_RATIO_CLIENT_LOG, currentSuccessRatio, aggregatedSuccessRatio);
		updateGoalQuality(GOAL, currentSuccessRatio);
		aggregatedGoalQuality.set(aggregatedSuccessRatio);
		return false;
	}

	/**
	 * Method evaluates the job success ratio for overall job execution (aggregated and at the current moment)
	 * for the default time period.
	 *
	 * @return boolean indicating if the analyzer should be triggered
	 */
	public boolean evaluateAndUpdateClientJobSuccessRatio() {
		return evaluateAndUpdateClientJobSuccessRatio(MONITOR_SYSTEM_DATA_TIME_PERIOD);
	}

	/**
	 * Method computes the aggregated success ratio of individual network components
	 *
	 * @return boolean indicating current state of job success ratio for components
	 */
	public boolean evaluateComponentSuccessRatio() {
		logger.info(READ_SUCCESS_RATIO_COMPONENTS_LOG);
		final List<AgentData> componentsData = managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypes(NETWORK_AGENT_DATA_TYPES, MONITOR_SYSTEM_DATA_AGGREGATED_PERIOD);

		if (componentsData.isEmpty()) {
			logger.info(READ_SUCCESS_RATIO_NETWORK_DATA_YET_LOG);
			return true;
		}
		return componentsData.stream().allMatch(this::verifySuccessRatioForComponent);
	}

	@Override
	public double getLastMeasuredGoalQuality() {
		return currentGoalQuality.get();
	}

	private boolean verifySuccessRatioForComponent(final AgentData component) {
		final double successRatio = ((NetworkComponentMonitoringData) component.monitoringData()).getSuccessRatio();
		boolean result = successRatio == DATA_NOT_AVAILABLE_INDICATOR || managingAgent.monitor()
				.isQualityInBounds(successRatio, MAXIMIZE_JOB_SUCCESS_RATIO);

		if (!result) {
			final String name = component.aid().split("@")[0];
			logger.info(SUCCESS_RATIO_UNSATISFIED_COMPONENT_LOG, name, successRatio);
		}

		return result;
	}

	@Override
	public double readCurrentGoalQuality(final int time) {
		final List<ClientMonitoringData> clientsData = managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypes(singletonList(CLIENT_MONITORING), time)
				.stream()
				.map(AgentData::monitoringData)
				.map(ClientMonitoringData.class::cast)
				.toList();

		if (clientsData.isEmpty()) {
			return DATA_NOT_AVAILABLE_INDICATOR;
		}

		final long allCount = clientsData.size();
		final long failCount = clientsData.stream()
				.filter(data -> data.getIsFinished() && data.getCurrentJobStatus().equals(ClientJobStatusEnum.FAILED))
				.count();
		return 1 - ((double) failCount / allCount);
	}
}
