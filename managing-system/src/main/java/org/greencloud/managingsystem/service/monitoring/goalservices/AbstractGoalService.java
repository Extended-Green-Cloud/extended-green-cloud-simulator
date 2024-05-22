package org.greencloud.managingsystem.service.monitoring.goalservices;

import static com.database.knowledge.types.DataType.CLIENT_MONITORING;
import static java.util.List.of;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.List;
import java.util.Objects;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.types.GoalType;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * Abstract adaptation goal service that evaluates goal's fulfillment
 */
public abstract class AbstractGoalService extends AbstractManagingService {

	protected final AtomicDouble currentGoalQuality;
	protected final GoalType goal;

	/**
	 * Default constructor
	 *
	 * @param managingAgent agent using the service to monitor the system
	 */
	protected AbstractGoalService(AbstractManagingAgent managingAgent, GoalType goal) {
		super(managingAgent);
		this.currentGoalQuality = new AtomicDouble(0);
		this.goal = goal;
	}

	/**
	 * Method retrieves lastly measured goal quality
	 *
	 * @return double goal quality
	 */
	public double readLastMeasuredGoalQuality() {
		return currentGoalQuality.get();
	}

	/**
	 * Method updates the value of the goal quality according to current data and evaluates if the
	 * goal is fulfilled.
	 *
	 * @return boolean value indicating if the goal is fulfilled
	 */
	public abstract boolean evaluateAndUpdate();

	/**
	 * Method recalculates the quality of a given goal based on data stored over a specified period of time.
	 *
	 * @param time number of seconds used in retrieving last data records
	 * @return current goal quality for the given period
	 */
	public abstract double computeCurrentGoalQuality(int time);

	/**
	 * Method recalculates the quality of a given goal based on data stored over a default period of time
	 * specified by MONITOR_SYSTEM_DATA_TIME_PERIOD constant.
	 *
	 * @return current goal quality for the given period
	 */
	public double computeCurrentGoalQuality() {
		return computeCurrentGoalQuality(MONITOR_SYSTEM_DATA_TIME_PERIOD);
	}

	protected void updateGoalQuality(Double currentGoalQuality) {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			managingAgent.getAgentNode().getDatabaseClient()
					.writeSystemQualityData(goal.getAdaptationGoalId(), currentGoalQuality);
		}
		this.currentGoalQuality.set(currentGoalQuality);
	}

	protected List<ClientMonitoringData> readClientMonitoringData(final int time) {
		return managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(of(CLIENT_MONITORING), time)
				.stream()
				.map(AgentData::monitoringData)
				.map(ClientMonitoringData.class::cast)
				.toList();
	}
}
