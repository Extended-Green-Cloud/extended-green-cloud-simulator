package org.greencloud.managingsystem.service.planner.plans;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.greencloud.commons.managingsystem.planner.ImmutableDisableServerActionParameters;
import jade.core.AID;
import org.greencloud.managingsystem.agent.ManagingAgent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DISABLE_SERVER;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;

public class DisableServerPlan extends AbstractPlan{


    /**
     * Default abstract constructor
     *
     * @param managingAgent managing agent executing the action
     */
    public DisableServerPlan(ManagingAgent managingAgent) {super(DISABLE_SERVER, managingAgent);}

    /**
     * Method verifies if the plan is executable. The plan is executable if:
     * 1. There are some idle servers in the system
     *
     * @return boolean information if the plan is executable in current conditions
     */
    @Override
    public boolean isPlanExecutable() {
        List<AgentData> serverMonitoring = managingAgent.getAgentNode().getDatabaseClient().
                readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING));

        AtomicBoolean idleExists = new AtomicBoolean(false);

        serverMonitoring.stream().forEach(data -> {
			var serverMonitoringData = (ServerMonitoringData) data.monitoringData();
            if(serverMonitoringData.getCurrentTraffic() == 0 && !serverMonitoringData.isDisabled()) {
                idleExists.set(true);
            }
        });
        return idleExists.get();
    }

    @Override
    public AbstractPlan constructAdaptationPlan() {
        List<AgentData> serverMonitoring = managingAgent.getAgentNode().getDatabaseClient()
                .readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING));

        targetAgent = selectTargetAgent(serverMonitoring);

        actionParameters = ImmutableDisableServerActionParameters.builder()
                .build();

        return this;
    }

    protected AID selectTargetAgent(List<AgentData> data) {
        List<AgentData> idleServers = data.stream()
                .filter(monitoring -> ((ServerMonitoringData)monitoring.monitoringData()).getCurrentTraffic() == 0)
                .toList();
        AgentData chosenServerData = Collections.max(idleServers, Comparator.comparing(idleServer -> ((ServerMonitoringData)idleServer.monitoringData()).getCurrentMaximumCapacity()));
        return new AID(chosenServerData.aid(), AID.ISGUID);
    }
}
