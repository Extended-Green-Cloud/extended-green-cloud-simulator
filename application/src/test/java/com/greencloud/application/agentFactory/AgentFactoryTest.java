package com.greencloud.application.agentFactory;

import com.greencloud.application.agents.greenenergy.domain.GreenEnergySourceTypeEnum;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import jade.junit.jupiter.JadeExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@ExtendWith(JadeExtension.class)
@MockitoSettings(strictness = LENIENT)
public class AgentFactoryTest {

    AgentFactory factory = new AgentFactoryImpl();

    @BeforeEach
    void init() {
        factory = new AgentFactoryImpl();
        AgentFactoryImpl.reset();
    }

    @Test
    void testCreateTemplateServerDefaultValues() {
        ServerAgentArgs result = factory.createServerAgent("OwnerCna1", null, null);

        assertThat(result.getName()).isEqualTo("ExtraServer1");
        assertThat(result.getMaximumCapacity()).isEqualTo(TEMPLATE_SERVER_MAXIMUM_CAPACITY);
        assertThat(result.getPrice()).isEqualTo(TEMPLATE_SERVER_PRICE);
        assertThat(result.getOwnerCloudNetwork()).isEqualTo("OwnerCna1");
    }

    @Test
    void testCreateTemplateGreenSourceDefaultValues() {
        GreenEnergyAgentArgs result = factory.createGreenEnergyAgent("monitoring1",
                "server1",
                null,
                null,
                null,
                null,
                null);

        assertThat(result.getName()).isEqualTo("ExtraGreenEnergy1");
        assertThat(result.getMaximumCapacity()).isEqualTo(TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY);
        assertThat(result.getLatitude()).isEqualTo("50");
        assertThat(result.getLongitude()).isEqualTo("20");
        assertThat(result.getPricePerPowerUnit()).isEqualTo("10");
        assertThat(result.getEnergyType()).isEqualTo("SOLAR");
    }

    @Test
    void testGenerateCorrectNames() {
        ServerAgentArgs result1 = factory.createServerAgent("1", null, null);
        ServerAgentArgs result2 = factory.createServerAgent("1", null, null);
        MonitoringAgentArgs result3 = factory.createMonitoringAgent();

        assertThat(result1.getName()).isEqualTo("ExtraServer1");
        assertThat(result2.getName()).isEqualTo("ExtraServer2");
        assertThat(result3.getName()).isEqualTo("ExtraMonitoring1");
    }

    @Test
    void testCreatingGreenSourceNullParameters() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            factory.createGreenEnergyAgent(null
                    , "testServer"
                    , 52
                    , 52
                    , 200
                    , 1
                    , GreenEnergySourceTypeEnum.SOLAR);
        });

        assertThat(exception.getMessage()).isEqualTo("monitoringAgentName and ownerServerName should not be null");
    }

    @Test
    void testCreatingMonitoringAgent(){
        MonitoringAgentArgs result = factory.createMonitoringAgent();

        assertThat(result.getName()).isEqualTo("ExtraMonitoring1");
    }

    @Test
    void testCreatingServerCustomValues() {
        ServerAgentArgs result = factory.createServerAgent("OwnerCna1", 150, 25);

        assertThat(result.getName()).isEqualTo("ExtraServer1");
        assertThat(result.getMaximumCapacity()).isEqualTo("150");
        assertThat(result.getPrice()).isEqualTo("25");
        assertThat(result.getOwnerCloudNetwork()).isEqualTo("OwnerCna1");
    }
}