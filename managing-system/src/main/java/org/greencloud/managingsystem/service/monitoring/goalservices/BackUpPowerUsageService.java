package org.greencloud.managingsystem.service.monitoring.goalservices;

import static com.database.knowledge.types.GoalType.MINIMIZE_USED_BACKUP_POWER;
import static org.greencloud.commons.constants.MonitoringConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.ON_BACK_UP;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;

import java.util.List;

import org.greencloud.commons.enums.job.JobClientStatusEnum;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.managingsystem.agents.ManagingAgent;

import com.database.knowledge.domain.agent.client.ClientMonitoringData;

/**
 * Service containing methods connected with monitoring system's usage of backUp power
 */
public class BackUpPowerUsageService extends AbstractGoalService {

	private static final LogBuilder log = constructLogBuilder(BackUpPowerUsageService.class);

	public BackUpPowerUsageService(ManagingAgent managingAgent) {
		super(managingAgent, MINIMIZE_USED_BACKUP_POWER);
	}

	@Override
	public boolean evaluateAndUpdate() {
		log.messageInfo("Reading backup power quality from the database.");

		double currentBackupPowerQuality = computeCurrentGoalQuality();
		double aggregatedBackupPowerQuality = computeCurrentGoalQuality(
				managingAgent.getProperties().getConfig().getQualityGoalsEvaluationTimeFrame());

		if (currentBackupPowerQuality == DATA_NOT_AVAILABLE_INDICATOR
				|| aggregatedBackupPowerQuality == DATA_NOT_AVAILABLE_INDICATOR) {
			log.messageInfo("There is no backup power quality data available yet.");
			return true;
		}

		log.messageInfo("Back up power quality ratio: current {}; aggregated: {}.", currentBackupPowerQuality,
				aggregatedBackupPowerQuality);
		updateGoalQuality(currentBackupPowerQuality);
		return false;
	}

	@Override
	public double computeCurrentGoalQuality(int time) {
		final List<ClientMonitoringData> clientsData = readClientMonitoringData(time);

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

	private double getPowerByType(List<ClientMonitoringData> clientsData, JobClientStatusEnum status) {
		return clientsData.stream()
				.map(ClientMonitoringData::getJobStatusDurationMap)
				.map(map -> map.containsKey(status) ? map.get(status) : 0)
				.mapToDouble(Long::doubleValue)
				.sum();
	}
}
