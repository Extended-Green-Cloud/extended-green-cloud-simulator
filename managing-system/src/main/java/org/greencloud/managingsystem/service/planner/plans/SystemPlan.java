package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static java.util.Objects.isNull;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.List;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.agent.AgentData;
import com.greencloud.commons.managingsystem.planner.SystemAdaptationActionParameters;

import jade.core.Location;

/**
 * Interface used to differentiate plans that have to be executed over Jade Framework in which Green Cloud network
 * resides as opposed to plans executed on particular agents.
 */
public abstract class SystemPlan extends AbstractPlan {

	/**
	 * Default abstract constructor
	 *
	 * @param actionEnum    type of adaptation action
	 * @param managingAgent managing agent executing the action
	 */
	protected SystemPlan(AdaptationActionEnum actionEnum, ManagingAgent managingAgent) {
		super(actionEnum, managingAgent);
	}

	public SystemAdaptationActionParameters getSystemAdaptationActionParameters() {
		return (SystemAdaptationActionParameters) this.actionParameters;
	}

	protected List<AgentData> getLastServerData() {
		return managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD);
	}

	protected List<AgentData> getServerData() {
		return managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypes(List.of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD);
	}

	protected Location findTargetLocation(String candidateCloudNetwork) {
		if (managingAgent.getContainersLocations() == null) {
			managingAgent.setContainersLocations(managingAgent.findContainersLocations());
		}
		var cloudNetworkContainer = managingAgent.getContainerLocations(candidateCloudNetwork);
		return isNull(cloudNetworkContainer)
				? managingAgent.getContainerLocations("Main-Container")
				: cloudNetworkContainer;
	}
}
