package com.greencloud.factory;

import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.WIND;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_IDLE_POWER;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_MAX_POWER;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_PRICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgentFactoryUnitTest {

	AgentFactory factory = new AgentFactoryImpl();

	@BeforeEach
	void init() {
		factory = new AgentFactoryImpl();
		AgentFactoryImpl.reset();
	}

	@Test
	void testCreateTemplateServerDefaultValues() {
		ServerArgs result = factory.createServerAgent("OwnerCna1", null, null, null, null, null);

		Assertions.assertThat(result.getName()).isEqualTo("ExtraServer1");
		assertThat(result.getMaxPower()).isEqualTo(TEMPLATE_SERVER_MAX_POWER);
		assertThat(result.getIdlePower()).isEqualTo(TEMPLATE_SERVER_IDLE_POWER);
		assertThat(result.getPrice()).isEqualTo(TEMPLATE_SERVER_PRICE.doubleValue());
		assertThat(result.getOwnerCloudNetwork()).isEqualTo("OwnerCna1");
		assertThat(result.getJobProcessingLimit()).isEqualTo(20);
	}

	@Test
	void testCreateTemplateGreenSourceDefaultValues() {
		GreenEnergyArgs result = factory.createGreenEnergyAgent("monitoring1",
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
		assertThat(result.getPricePerPowerUnit()).isEqualTo(10L);
		assertThat(result.getEnergyType()).isEqualTo(WIND);
	}

	@Test
	void testGenerateCorrectNames() {
		ServerArgs result1 = factory.createServerAgent("1", null, null, 10, null, null);
		ServerArgs result2 = factory.createServerAgent("1", null, null, null, null, null);
		MonitoringArgs result3 = factory.createMonitoringAgent();

		Assertions.assertThat(result1.getName()).isEqualTo("ExtraServer1");
		Assertions.assertThat(result2.getName()).isEqualTo("ExtraServer2");
		Assertions.assertThat(result3.getName()).isEqualTo("ExtraMonitoring1");
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
		MonitoringArgs result = factory.createMonitoringAgent();

		Assertions.assertThat(result.getName()).isEqualTo("ExtraMonitoring1");
	}

	@Test
	void testCreatingServerCustomValues() {
		ServerArgs result = factory.createServerAgent("OwnerCna1", null, 150, 25, 10, null);

		Assertions.assertThat(result.getName()).isEqualTo("ExtraServer1");
		assertThat(result.getMaxPower()).isEqualTo(150);
		assertThat(result.getIdlePower()).isEqualTo(25);
		assertThat(result.getOwnerCloudNetwork()).isEqualTo("OwnerCna1");
		assertThat(result.getPrice()).isEqualTo(10);
	}
}
