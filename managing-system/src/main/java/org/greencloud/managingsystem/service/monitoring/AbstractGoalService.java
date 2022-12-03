package org.greencloud.managingsystem.service.monitoring;

import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.Objects;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;

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
		return aggregatedGoalQuality.get();
	}

	/**
	 * Read current goal quality for a custom measuring time
	 *
	 * @param time time window defined in seconds for which goal quality should be read
	 * @return current goal quality
	 */
	public abstract double readCurrentGoalQuality(int time);

	/**
	 * Read current goal quality with default measuring time defined in
	 * MONITOR_SYSTEM_DATA_TIME_PERIOD constant
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
}
