package com.greencloud.application.agentFactory;

import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import jade.junit.jupiter.JadeExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@ExtendWith(JadeExtension.class)
@MockitoSettings(strictness = LENIENT)
@Disabled
public class AgentFactoryTest {

    AgentFactory factory = new AgentFactoryImpl();

    @BeforeEach
    void init() {
        factory = new AgentFactoryImpl();
    }
    @Test
    void shouldCreateTemplateServerDefaultValues() {
        ServerAgentArgs result = factory.createServerAgent("OwnerCna1", null, null);

        assertThat(result.getName()).isEqualTo("ExtraServer1");
        assertThat(result.getMaximumCapacity()).isEqualTo(TEMPLATE_SERVER_MAXIMUM_CAPACITY);
        assertThat(result.getPrice()).isEqualTo(TEMPLATE_SERVER_PRICE);
        assertThat(result.getOwnerCloudNetwork()).isEqualTo("OwnerCna1");
    }

    @Test
    void shouldCreateTemplateGreenSourceDefaultValues() {
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
        assertThat(result.getPricePerPowerUnit()).isEqualTo("100");
        assertThat(result.getEnergyType()).isEqualTo("SOLAR");
    }

    @Test
    void shouldGenerateCorrectNames() {
        ServerAgentArgs result1 = factory.createServerAgent("1", null, null);
        ServerAgentArgs result2 =  factory.createServerAgent("1", null, null);
        MonitoringAgentArgs result3 = factory.createMonitoringAgent();

        assertThat(result1.getName()).isEqualTo("ExtraServer1");
        assertThat(result2.getName()).isEqualTo("ExtraServer2");
        assertThat(result3.getName()).isEqualTo("ExtraMonitoring1");
    }


}
