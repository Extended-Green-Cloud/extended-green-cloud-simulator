package com.greencloud.application.agentFactory;

import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.greenenergy.ImmutableGreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.ImmutableMonitoringAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ImmutableServerAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import java.util.Objects;

import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.*;


public class AgentFactoryImpl implements AgentFactory{

    static private int serverAgentsCreated = 0;
    static private int monitoringAgentsCreated = 0;
    static private int greenEnergyAgentsCreated = 0;

    public AgentFactoryImpl() {

    }
    @Override
    public ServerAgentArgs createServerAgent(String ownerCNA, String maximumCapacity, String price) {

        if(Objects.isNull(ownerCNA)) {
            throw new IllegalArgumentException("ownerCna should not be null");
        }

        serverAgentsCreated += 1;
        String serverAgentName = "ExtraServer" + serverAgentsCreated;

        return ImmutableServerAgentArgs.builder()
                .name(serverAgentName)
                .ownerCloudNetwork(ownerCNA)
                .maximumCapacity(Objects.isNull(maximumCapacity) ? TEMPLATE_SERVER_MAXIMUM_CAPACITY : maximumCapacity)
                .price(Objects.isNull(price) ? TEMPLATE_SERVER_PRICE : price)
                .build();
    }

    @Override
    public GreenEnergyAgentArgs createGreenEnergyAgent(
            String monitoringAgentName,
            String ownerServerName,
            String latitude,
            String longitude,
            String maximumCapacity,
            String pricePerPowerUnit,
            String energyType) {

        if(Objects.isNull(monitoringAgentName) || Objects.isNull(ownerServerName)) {
            throw new IllegalArgumentException("monitoringAgentName and ownerServerName should not be null");
        }

        greenEnergyAgentsCreated += 1;
        String greenEnergyAgentName = "ExtraGreenEnergy" + greenEnergyAgentsCreated;
        return ImmutableGreenEnergyAgentArgs.builder()
                .name(greenEnergyAgentName)
                .monitoringAgent(monitoringAgentName)
                .ownerSever(ownerServerName)
                .latitude(Objects.isNull(latitude) ? TEMPLATE_GREEN_ENERGY_LATITUDE : latitude)
                .longitude(Objects.isNull(longitude) ? TEMPLATE_GREEN_ENERGY_LONGITUDE : longitude)
                .maximumCapacity(Objects.isNull(maximumCapacity) ? TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY : maximumCapacity)
                .pricePerPowerUnit(Objects.isNull(pricePerPowerUnit) ? TEMPLATE_GREEN_ENERGY_PRICE : pricePerPowerUnit)
                .energyType(Objects.isNull(energyType) ? TEMPLATE_GREEN_ENERGY_TYPE : energyType)
                .build();
    }

    @Override
    public MonitoringAgentArgs createMonitoringAgent() {
        monitoringAgentsCreated += 1;
        String monitoringAgentName = "ExtraMonitoring" + monitoringAgentsCreated;
        return ImmutableMonitoringAgentArgs.builder()
                .name(monitoringAgentName)
                .build();
    }

}
