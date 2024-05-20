package com.greencloud.connector.factory.constants;

import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.FROM_GI_TO_BYTE_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.TO_GI_FROM_BYTE_CONVERTER;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.MEMORY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.STORAGE;
import static org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum.WIND;

import java.util.Map;

import org.greencloud.commons.domain.location.ImmutableLocation;
import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.domain.resources.ResourceCharacteristic;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;

/**
 * Class stores constants used to run default agent controllers.
 */
public class AgentTemplatesConstants {

	// SERVER TEMPLATE CONSTANTS
	/**
	 * Default maximal server power
	 */
	public static final Integer TEMPLATE_SERVER_MAX_POWER = 200;
	/**
	 * Default idle server power
	 */
	public static final Integer TEMPLATE_SERVER_IDLE_POWER = 30;
	/**
	 * Default server execution price
	 */
	public static final Double TEMPLATE_SERVER_PRICE = 20D;
	/**
	 * Default server job processing limit
	 */
	public static final Integer TEMPLATE_SERVER_JOB_LIMIT = 20;

	// GREEN ENERGY TEMPLATE CONSTANTS
	/**
	 * Default green source location
	 */
	public static final Location TEMPLATE_GREEN_ENERGY_LOCATION =
			ImmutableLocation.builder().latitude(50D).longitude(20D).build();
	/**
	 * Default green source execution price
	 */
	public static final Long TEMPLATE_GREEN_ENERGY_PRICE = 10L;
	/**
	 * Default green source maximal capacity
	 */
	public static final Long TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY = 200L;
	/**
	 * Default green source energy type
	 */
	public static final GreenEnergySourceTypeEnum TEMPLATE_GREEN_ENERGY_TYPE = WIND;

	// TEMPLATES FOR HANDLER FUNCTION
	/**
	 * Default method used for resource addition
	 */
	public static final String TEMPLATE_ADDITION = "return resource1 + resource2;";
	/**
	 * Default method used for resource booking
	 */
	public static final String TEMPLATE_BOOKER = "return ownedAmount - amountToReserve;";
	/**
	 * Default method used for resource removal
	 */
	public static final String TEMPLATE_REMOVER = "return ownedAmount - amountToRemove;";
	/**
	 * Default method used for resource comparison
	 */
	public static final String TEMPLATE_COMPARATOR = """
			import java.lang.Math;
			return Math.signum(resource1.getAmountInCommonUnit() - resource2.getAmountInCommonUnit());
			""";
	/**
	 * Default method used for resource validation
	 */
	public static final String TEMPLATE_VALIDATOR = """
			requirements.getCharacteristics().containsKey(\"amount\") &&
			resource.getAmountInCommonUnit() >= requirements.getAmountInCommonUnit();
			""";

	// RESOURCE TEMPLATES
	/**
	 * Default cpu resource characteristics
	 */
	public static final ResourceCharacteristic CPU_CHARACTERISTIC = ImmutableResourceCharacteristic.builder()
			.value(20D)
			.unit("cores")
			.resourceCharacteristicAddition(TEMPLATE_ADDITION)
			.resourceCharacteristicReservation(TEMPLATE_BOOKER)
			.resourceCharacteristicSubtraction(TEMPLATE_REMOVER)
			.build();
	/**
	 * Default memory resource characteristics
	 */
	public static final ResourceCharacteristic MEMORY_CHARACTERISTIC = ImmutableResourceCharacteristic.builder()
			.value(200)
			.unit("Gi")
			.toCommonUnitConverter(FROM_GI_TO_BYTE_CONVERTER)
			.fromCommonUnitConverter(TO_GI_FROM_BYTE_CONVERTER)
			.resourceCharacteristicAddition(TEMPLATE_ADDITION)
			.resourceCharacteristicReservation(TEMPLATE_BOOKER)
			.resourceCharacteristicSubtraction(TEMPLATE_REMOVER)
			.build();
	/**
	 * Default storage resource characteristics
	 */
	public static final ResourceCharacteristic STORAGE_CHARACTERISTIC = ImmutableResourceCharacteristic.builder()
			.value(1000)
			.unit("Gi")
			.toCommonUnitConverter(FROM_GI_TO_BYTE_CONVERTER)
			.fromCommonUnitConverter(TO_GI_FROM_BYTE_CONVERTER)
			.resourceCharacteristicAddition(TEMPLATE_ADDITION)
			.resourceCharacteristicReservation(TEMPLATE_BOOKER)
			.resourceCharacteristicSubtraction(TEMPLATE_REMOVER)
			.build();
	/**
	 * Default server resources
	 */
	public static final Map<String, Resource> TEMPLATE_SERVER_RESOURCES = Map.of(
			CPU, ImmutableResource.builder()
					.putCharacteristics(AMOUNT, CPU_CHARACTERISTIC)
					.resourceComparator(TEMPLATE_COMPARATOR)
					.resourceValidator(TEMPLATE_VALIDATOR)
					.build(),
			MEMORY, ImmutableResource.builder()
					.putCharacteristics(AMOUNT, MEMORY_CHARACTERISTIC)
					.resourceComparator(TEMPLATE_COMPARATOR)
					.resourceValidator(TEMPLATE_VALIDATOR)
					.build(),
			STORAGE, ImmutableResource.builder()
					.putCharacteristics(AMOUNT, STORAGE_CHARACTERISTIC)
					.resourceComparator(TEMPLATE_COMPARATOR)
					.resourceValidator(TEMPLATE_VALIDATOR)
					.build()
	);
}
