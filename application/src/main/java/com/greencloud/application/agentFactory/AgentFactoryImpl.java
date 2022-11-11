package com.greencloud.application.agentFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.monitoring.MonitoringAgent;
import com.greencloud.application.agents.server.ServerAgent;

public class AgentFactoryImpl implements AgentFactory{

    @Override
    public ServerAgent createServerAgent() {
        return null;
    }

    @Override
    public GreenEnergyAgent createGreenEnergyAgent() {
        return null;
    }

    @Override
    public MonitoringAgent createMonitoringAgent() {
        return null;
    }
}
