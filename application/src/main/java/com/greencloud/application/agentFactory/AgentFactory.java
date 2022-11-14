package com.greencloud.application.agentFactory;

import com.greencloud.application.agents.greenenergy.domain.GreenEnergySourceTypeEnum;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;

public interface AgentFactory {
    /**
     * Method creates new server agent args that can be used to initialize new agent
     * @param ownerCNA - required argument specifying owner CNA
     * @param maximumCapacity - optional argument specifying server's maximum capacity
     * @param price - optional argument specifying server's price
     * @return newly created server agent args
     */
    ServerAgentArgs createServerAgent(String ownerCNA, Integer maximumCapacity, Integer price);

    /**
     * Method creates new green energy agent args that can be used to initialize new agent
     * @param monitoringAgentName required argument specifying monitoring agent name
     * @param ownerServerName required argument specifying owner server name
     * @param latitude optional argument specifying latitude
     * @param longitude optional argument specifying longitude
     * @param maximumCapacity optional argument specifying maximumCapacity
     * @param pricePerPowerUnit optional argument specifying price per power unit
     * @param energyType optional argument specifying energy type
     * @return newly green energy agent args
     */
    GreenEnergyAgentArgs createGreenEnergyAgent(String monitoringAgentName,
                                                String ownerServerName,
                                                Integer latitude,
                                                Integer longitude,
                                                Integer maximumCapacity,
                                                Integer pricePerPowerUnit,
                                                GreenEnergySourceTypeEnum energyType);

    /**
     * Method creates new monitoring agent args that can be used to initialize new agent
     * @return newly created monitoring agent args
     */
    MonitoringAgentArgs createMonitoringAgent();
}
