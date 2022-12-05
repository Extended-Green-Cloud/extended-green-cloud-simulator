package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY;
import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.greencloud.commons.agent.AgentType.SCHEDULER;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_HEALTH_PERIOD;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.google.common.annotations.VisibleForTesting;
import jade.core.AID;
import org.greencloud.managingsystem.agent.ManagingAgent;

import java.util.List;

/**
 * Class containing adaptation plan which realizes the action of increasing the job scheduling priority with respect
 * to deadline
 */
public class IncreaseDeadlinePriorityPlan extends AbstractPlan {

	public IncreaseDeadlinePriorityPlan(ManagingAgent managingAgent) {
		super(INCREASE_DEADLINE_PRIORITY, managingAgent);
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. The Scheduler Agent is alive
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		final List<AgentData> queryResult =
				managingAgent.getAgentNode().getDatabaseClient()
						.readMonitoringDataForDataTypes(List.of(HEALTH_CHECK),
								MONITOR_SYSTEM_DATA_HEALTH_PERIOD);
		boolean schedulerAgentAlive = isSchedulerAlive(queryResult);
		if(schedulerAgentAlive) {
			targetAgent = new AID(getTargetScheduler(queryResult), AID.ISGUID);
		}
		return schedulerAgentAlive;
	}

	@Override
	public AbstractPlan constructAdaptationPlan() {
		return this;
	}

	@VisibleForTesting
	boolean isSchedulerAlive(List<AgentData> agentDataList) {
		return agentDataList.stream()
				.anyMatch(agentData -> {
					var healthData = ((HealthCheck) agentData.monitoringData());
					return healthData.alive() && healthData.agentType().equals(SCHEDULER);
				});
	}

	@VisibleForTesting
	String getTargetScheduler(List<AgentData> agentDataList) {
		return agentDataList.stream()
				.filter(agentData -> {
					var healthData = ((HealthCheck) agentData.monitoringData());
					return healthData.alive() && healthData.agentType().equals(SCHEDULER);
				})
				.map(AgentData::aid)
				.findFirst()
				.orElse(null);
	}
}
