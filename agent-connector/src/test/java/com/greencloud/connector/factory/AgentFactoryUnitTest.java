package com.greencloud.connector.factory;

import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.CPU_CHARACTERISTIC;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.MEMORY_CHARACTERISTIC;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.STORAGE_CHARACTERISTIC;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_ADDITION;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_BOOKER;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_COMPARATOR;
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

import java.time.Instant;

import org.assertj.core.api.Assertions;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.gui.messages.domain.ImmutableServerCreator;
import org.greencloud.gui.messages.domain.ServerCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
	void testCreatingMonitoringAgent() {
		MonitoringArgs result = factory.createDefaultMonitoringAgent();

		Assertions.assertThat(result.getName()).isEqualTo("ExtraMonitoring1");
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
