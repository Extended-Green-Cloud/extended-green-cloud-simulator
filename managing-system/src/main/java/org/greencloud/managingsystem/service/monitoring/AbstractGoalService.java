package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static java.util.List.of;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.List;
import java.util.Objects;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import com.google.common.util.concurrent.AtomicDouble;

public abstract class AbstractGoalService extends AbstractManagingService {

	protected final AtomicDouble aggregatedGoalQuality;
	protected final AtomicDouble currentGoalQuality;

	/**
	 * Default constructor
	 *
	 * @param managingAgent agent using the service to monitor the system
	 */
	protected AbstractGoalService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
		this.aggregatedGoalQuality = new AtomicDouble(0);
		this.currentGoalQuality = new AtomicDouble(0);
	}

	public double getLastMeasuredGoalQuality() {
		return currentGoalQuality.get();
	}

	public abstract boolean evaluateAndUpdate();

	/**
	 * Read current goal quality for a custom measuring time.
	 *
	 * @param time time window defined in seconds for which goal quality should be read
	 * @return current goal quality
	 */
	public abstract double readCurrentGoalQuality(int time);

	/**
	 * Read current goal quality with default measuring time defined in
	 * MONITOR_SYSTEM_DATA_TIME_PERIOD constant.
	 *
	 * @return current goal quality for the given period
	 */
	public double readCurrentGoalQuality() {
		return readCurrentGoalQuality(MONITOR_SYSTEM_DATA_TIME_PERIOD);
	}

	protected void updateGoalQuality(GoalEnum goalEnum, Double currentGoalQuality) {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			managingAgent.getAgentNode().getDatabaseClient()
					.writeSystemQualityData(goalEnum.getAdaptationGoalId(), currentGoalQuality);
		}
		this.currentGoalQuality.set(currentGoalQuality);
	}

	/**
	 * Reads Client Agent Monitoring data for the given time span.
	 *
	 * @param time seconds defining the time span as difference from {@link java.time.Instant}.now()
	 * @return read monitoring data
	 */
	protected List<ClientMonitoringData> readClientMonitoringData(final int time) {
		return managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypes(of(CLIENT_MONITORING), time)
				.stream()
				.map(AgentData::monitoringData)
				.map(ClientMonitoringData.class::cast)
				.toList();
	}
}
