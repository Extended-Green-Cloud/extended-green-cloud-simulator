package com.greencloud.factory.constants;

import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.FROM_GI_TO_BYTE_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.TO_GI_FROM_BYTE_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.MEMORY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.STORAGE;
import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.WIND;

import java.util.Map;

import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;

public class AgentTemplatesConstants {

	// SERVER TEMPLATE CONSTANTS
	public static final Integer TEMPLATE_SERVER_MAX_POWER = 200;
	public static final Integer TEMPLATE_SERVER_IDLE_POWER = 30;
	public static final Double TEMPLATE_SERVER_PRICE = 20D;
	public static final Map<String, Resource> TEMPLATE_SERVER_RESOURCES = Map.of(
			CPU, ImmutableResource.builder()
					.putCharacteristics(AMOUNT, ImmutableResourceCharacteristic.builder()
							.value(20D)
							.unit("cores")
							.build())
					.build(),
			MEMORY, ImmutableResource.builder()
					.putCharacteristics(AMOUNT, ImmutableResourceCharacteristic.builder()
							.value(20)
							.unit("Gi")
							.toCommonUnitConverter(FROM_GI_TO_BYTE_CONVERTER)
							.fromCommonUnitConverter(TO_GI_FROM_BYTE_CONVERTER)
							.build())
					.build(),
			STORAGE, ImmutableResource.builder()
					.putCharacteristics(AMOUNT, ImmutableResourceCharacteristic.builder()
							.value(32)
							.unit("Gi")
							.toCommonUnitConverter(FROM_GI_TO_BYTE_CONVERTER)
							.fromCommonUnitConverter(TO_GI_FROM_BYTE_CONVERTER)
							.build())
					.build()
	);
	public static final Integer TEMPLATE_SERVER_JOB_LIMIT = 20;

	// GREEN ENERGY TEMPLATE CONSTANTS
	public static final String TEMPLATE_GREEN_ENERGY_LATITUDE = "50";
	public static final String TEMPLATE_GREEN_ENERGY_LONGITUDE = "20";
	public static final Long TEMPLATE_GREEN_ENERGY_PRICE = 10L;
	public static final Long TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY = 200L;
	public static final GreenEnergySourceTypeEnum TEMPLATE_GREEN_ENERGY_TYPE = WIND;
}
