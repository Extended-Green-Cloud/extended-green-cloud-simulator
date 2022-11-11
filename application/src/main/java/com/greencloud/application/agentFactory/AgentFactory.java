package com.greencloud.application.agentFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.monitoring.MonitoringAgent;
import com.greencloud.application.agents.server.ServerAgent;

public interface AgentFactory {
    /**
     * Method create new server agent that can be connected to the network
     * @return newly created server agent
     */
    ServerAgent createServerAgent();

    /**
     * Method create new server agent that can be connected to the network
     * @return newly created server agent
     */
    GreenEnergyAgent createGreenEnergyAgent();

    /**
     * Method create new server agent that can be connected to the network
     * @return newly created server agent
     */
    MonitoringAgent createMonitoringAgent();
}
