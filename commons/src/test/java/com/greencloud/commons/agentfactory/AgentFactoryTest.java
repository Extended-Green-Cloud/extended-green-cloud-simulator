package com.greencloud.commons.agentfactory;

import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY;
import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_SERVER_MAXIMUM_CAPACITY;
import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_SERVER_PRICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.greencloud.commons.agent.greenenergy.GreenEnergySourceTypeEnum;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgentFactoryTest {

	AgentFactory factory = new AgentFactoryImpl();

	@BeforeEach
	void init() {
		factory = new AgentFactoryImpl();
		AgentFactoryImpl.reset();
	}

	@Test
	void testCreateTemplateServerDefaultValues() {
		ServerAgentArgs result = factory.createServerAgent("OwnerCna1", null, null, null);

		assertThat(result.getName()).isEqualTo("ExtraServer1");
		assertThat(result.getMaximumCapacity()).isEqualTo(TEMPLATE_SERVER_MAXIMUM_CAPACITY);
		assertThat(result.getPrice()).isEqualTo(TEMPLATE_SERVER_PRICE);
		assertThat(result.getOwnerCloudNetwork()).isEqualTo("OwnerCna1");
		assertThat(result.getJobProcessingLimit()).isEqualTo("20");
	}

	@Test
	void testCreateTemplateGreenSourceDefaultValues() {
		GreenEnergyAgentArgs result = factory.createGreenEnergyAgent("monitoring1",
				"server1",
				null,
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
		assertThat(result.getEnergyType()).isEqualTo("WIND");
	}

	@Test
	void testGenerateCorrectNames() {
		ServerAgentArgs result1 = factory.createServerAgent("1", null, null, 10);
		ServerAgentArgs result2 = factory.createServerAgent("1", null, null, null);
		MonitoringAgentArgs result3 = factory.createMonitoringAgent();

		assertThat(result1.getName()).isEqualTo("ExtraServer1");
		assertThat(result2.getName()).isEqualTo("ExtraServer2");
		assertThat(result3.getName()).isEqualTo("ExtraMonitoring1");
	}

	@Test
	void testCreatingGreenSourceNullParameters() {
		Exception exception = assertThrows(IllegalArgumentException.class, () ->
				factory.createGreenEnergyAgent(null
						, "testServer"
						, 52
						, 52
						, 200
						, 1
						, 0.0
						, GreenEnergySourceTypeEnum.SOLAR));

		assertThat(exception.getMessage()).isEqualTo("monitoringAgentName and ownerServerName should not be null");
	}

	@Test
	void testCreatingMonitoringAgent() {
		MonitoringAgentArgs result = factory.createMonitoringAgent();

		assertThat(result.getName()).isEqualTo("ExtraMonitoring1");
	}

	@Test
	void testCreatingServerCustomValues() {
		ServerAgentArgs result = factory.createServerAgent("OwnerCna1", 150, 25, 10);

		assertThat(result.getName()).isEqualTo("ExtraServer1");
		assertThat(result.getMaximumCapacity()).isEqualTo("150");
		assertThat(result.getPrice()).isEqualTo("25");
		assertThat(result.getOwnerCloudNetwork()).isEqualTo("OwnerCna1");
		assertThat(result.getJobProcessingLimit()).isEqualTo("10");
	}
}
