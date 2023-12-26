package com.greencloud.connector;

import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.CPU_CHARACTERISTIC;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.MEMORY_CHARACTERISTIC;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.STORAGE_CHARACTERISTIC;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_ADDITION;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_BOOKER;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_COMPARATOR;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_REMOVER;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_IDLE_POWER;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_MAX_POWER;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_PRICE;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_VALIDATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.FROM_CPU_CORES_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.TO_CPU_CORES_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.MEMORY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.STORAGE;
import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.WIND;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.greencloud.gui.messages.domain.ImmutableServerCreator;
import org.greencloud.gui.messages.domain.ServerCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.greencloud.connector.factory.AgentFactory;
import com.greencloud.connector.factory.AgentFactoryImpl;

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
		final ServerArgs result = factory.createDefaultServerAgent("OwnerRMA1");

		assertThat(result.getName()).isEqualTo("ExtraServer1");
		assertThat(result.getMaxPower()).isEqualTo(TEMPLATE_SERVER_MAX_POWER);
		assertThat(result.getIdlePower()).isEqualTo(TEMPLATE_SERVER_IDLE_POWER);
		assertThat(result.getPrice()).isEqualTo(TEMPLATE_SERVER_PRICE.doubleValue());
		assertThat(result.getOwnerRegionalManager()).isEqualTo("OwnerRMA1");
		assertThat(result.getJobProcessingLimit()).isEqualTo(20);
		assertThat(result.getContainerId()).isNull();
		assertThat(result.getResources())
				.containsEntry(CPU, ImmutableResource.builder()
						.putCharacteristics(AMOUNT, CPU_CHARACTERISTIC)
						.resourceComparator(TEMPLATE_COMPARATOR)
						.resourceValidator(TEMPLATE_VALIDATOR)
						.build())
				.containsEntry(MEMORY, ImmutableResource.builder()
						.putCharacteristics(AMOUNT, MEMORY_CHARACTERISTIC)
						.resourceComparator(TEMPLATE_COMPARATOR)
						.resourceValidator(TEMPLATE_VALIDATOR)
						.build())
				.containsEntry(STORAGE, ImmutableResource.builder()
						.putCharacteristics(AMOUNT, STORAGE_CHARACTERISTIC)
						.resourceComparator(TEMPLATE_COMPARATOR)
						.resourceValidator(TEMPLATE_VALIDATOR)
						.build());
	}

	@Test
	void testCreateTemplateServerMixedDefaultValues() {
		final ServerArgs result = factory.createServerAgent("OwnerRMA1", null, 100, 30, 10D, null);

		Assertions.assertThat(result.getName()).isEqualTo("ExtraServer1");
		assertThat(result.getMaxPower()).isEqualTo(100);
		assertThat(result.getIdlePower()).isEqualTo(30);
		assertThat(result.getPrice()).isEqualTo(10D);
		assertThat(result.getOwnerRegionalManager()).isEqualTo("OwnerRMA1");
		assertThat(result.getJobProcessingLimit()).isEqualTo(20);
		assertThat(result.getResources())
				.containsEntry(CPU, ImmutableResource.builder()
						.putCharacteristics(AMOUNT, CPU_CHARACTERISTIC)
						.resourceComparator(TEMPLATE_COMPARATOR)
						.resourceValidator(TEMPLATE_VALIDATOR)
						.build())
				.containsEntry(MEMORY, ImmutableResource.builder()
						.putCharacteristics(AMOUNT, MEMORY_CHARACTERISTIC)
						.resourceComparator(TEMPLATE_COMPARATOR)
						.resourceValidator(TEMPLATE_VALIDATOR)
						.build())
				.containsEntry(STORAGE, ImmutableResource.builder()
						.putCharacteristics(AMOUNT, STORAGE_CHARACTERISTIC)
						.resourceComparator(TEMPLATE_COMPARATOR)
						.resourceValidator(TEMPLATE_VALIDATOR)
						.build());
	}

	@Test
	void testCreateTemplateServerCustomValues() {
		final Map<String, Resource> customResources = Map.of(CPU, getCustomCpuResource());
		final ServerArgs result = factory.createServerAgent("OwnerRMA1", customResources, 150, 10, 15D, 30);

		assertThat(result.getName()).isEqualTo("ExtraServer1");
		assertThat(result.getMaxPower()).isEqualTo(150);
		assertThat(result.getIdlePower()).isEqualTo(10);
		assertThat(result.getPrice()).isEqualTo(15D);
		assertThat(result.getOwnerRegionalManager()).isEqualTo("OwnerRMA1");
		assertThat(result.getJobProcessingLimit()).isEqualTo(30);
		assertThat(result.getResources()).containsEntry(CPU, getCustomCpuResource());
	}

	@Test
	void testCreateTemplateServerFromServerCreator() {
		final ServerCreator serverCreator = ImmutableServerCreator.builder()
				.name("ServerTest")
				.idlePower(20D)
				.maxPower(100D)
				.regionalManager("TestOwner")
				.isFinished(false)
				.occurrenceTime(Instant.now())
				.jobProcessingLimit(10L)
				.price(20D)
				.putResources(CPU, getCustomCpuResource())
				.build();
		final ServerArgs result = factory.createServerAgent(serverCreator);

		assertThat(result.getName()).isEqualTo("ServerTest");
		assertThat(result.getMaxPower()).isEqualTo(100);
		assertThat(result.getIdlePower()).isEqualTo(20);
		assertThat(result.getPrice()).isEqualTo(20D);
		assertThat(result.getOwnerRegionalManager()).isEqualTo("TestOwner");
		assertThat(result.getJobProcessingLimit()).isEqualTo(10);
		assertThat(result.getResources()).containsEntry(CPU, getCustomCpuResource());

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
						, null
						, 52
						, 52
						, 200
						, 1
						, 0.0
						, GreenEnergySourceTypeEnum.SOLAR));
		assertThat(exception.getMessage()).isEqualTo("Name of monitoring agent and owner server must be specified");
	}

	@Test
	void testCreatingMonitoringAgent() {
		MonitoringArgs result = factory.createMonitoringAgent();

		Assertions.assertThat(result.getName()).isEqualTo("ExtraMonitoring1");
	}

	@Test
	void testCreatingServerCustomValues() {
		ServerArgs result = factory.createServerAgent("OwnerRMA1", null, 150, 25, 10D, null);

		Assertions.assertThat(result.getName()).isEqualTo("ExtraServer1");
		assertThat(result.getMaxPower()).isEqualTo(150);
		assertThat(result.getIdlePower()).isEqualTo(25);
		assertThat(result.getOwnerRegionalManager()).isEqualTo("OwnerRMA1");
		assertThat(result.getPrice()).isEqualTo(10);
	}

	private Resource getCustomCpuResource() {
		return ImmutableResource.builder()
				.putCharacteristics("amount", ImmutableResourceCharacteristic.builder()
						.value(10)
						.unit("millicores")
						.toCommonUnitConverter(TO_CPU_CORES_CONVERTER)
						.fromCommonUnitConverter(FROM_CPU_CORES_CONVERTER)
						.resourceCharacteristicAddition(TEMPLATE_ADDITION)
						.resourceCharacteristicReservation(TEMPLATE_BOOKER)
						.resourceCharacteristicSubtraction(TEMPLATE_REMOVER)
						.build())
				.resourceComparator(TEMPLATE_COMPARATOR)
				.resourceValidator(TEMPLATE_VALIDATOR)
				.build();
	}
}
