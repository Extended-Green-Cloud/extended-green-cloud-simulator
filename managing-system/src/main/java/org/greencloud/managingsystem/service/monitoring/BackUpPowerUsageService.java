package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static com.greencloud.commons.job.ClientJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ClientJobStatusEnum.ON_BACK_UP;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_AGGREGATED_PERIOD;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.BACKUP_POWER_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_BACKUP_POWER_QUALITY_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_BACKUP_POWER_QUALITY_NO_DATA_YET_LOG;

import java.util.List;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import com.greencloud.commons.job.ClientJobStatusEnum;

/**
 * Service containing methods connected with monitoring system's usage of backUp power
 */
public class BackUpPowerUsageService extends AbstractGoalService {

	private static final Logger logger = LoggerFactory.getLogger(BackUpPowerUsageService.class);

	private static final GoalEnum GOAL = MINIMIZE_USED_BACKUP_POWER;

	public BackUpPowerUsageService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	@Override
	public boolean evaluateAndUpdate() {
		logger.info(READ_BACKUP_POWER_QUALITY_LOG);
		double currentBackupPowerQuality = readCurrentGoalQuality();
		double aggregatedBackupPowerQuality = readCurrentGoalQuality(MONITOR_SYSTEM_DATA_AGGREGATED_PERIOD);

		if (currentBackupPowerQuality == DATA_NOT_AVAILABLE_INDICATOR
			|| aggregatedBackupPowerQuality == DATA_NOT_AVAILABLE_INDICATOR) {
			logger.info(READ_BACKUP_POWER_QUALITY_NO_DATA_YET_LOG);
			return true;
		}

		logger.info(BACKUP_POWER_LOG, currentBackupPowerQuality, aggregatedBackupPowerQuality);
		updateGoalQuality(GOAL, currentBackupPowerQuality);
		aggregatedGoalQuality.set(aggregatedBackupPowerQuality);
		return false;
	}

	@Override
	public double readCurrentGoalQuality(int time) {
		List<ClientMonitoringData> clientsData = readClientMonitoringData(time);

		if (clientsData.isEmpty()) {
			return DATA_NOT_AVAILABLE_INDICATOR;
		}

		double backUpPower = getPowerByType(clientsData, ON_BACK_UP);
		double greenPower = getPowerByType(clientsData, IN_PROGRESS);

		if (backUpPower == 0.0 && greenPower == 0.0) {
			return DATA_NOT_AVAILABLE_INDICATOR;

		}

		return backUpPower / (backUpPower + greenPower);
	}

	private double getPowerByType(List<ClientMonitoringData> clientsData, ClientJobStatusEnum status) {
		return clientsData.stream()
				.map(ClientMonitoringData::getJobStatusDurationMap)
				.map(map -> map.get(status))
				.map(Long::doubleValue)
				.mapToDouble(Double::doubleValue)
				.sum();
	}
}
