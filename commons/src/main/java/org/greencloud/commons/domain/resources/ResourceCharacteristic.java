package org.greencloud.commons.domain.resources;

import static java.util.Objects.isNull;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.commonConverters;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;
import org.mvel2.MVEL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Class describing a characteristic of given resource
 */
@JsonSerialize(as = ImmutableResourceCharacteristic.class)
@JsonDeserialize(as = ImmutableResourceCharacteristic.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Value.Immutable
public interface ResourceCharacteristic {

	/**
	 * @return value of given resource characteristic
	 */
	Object getValue();

	/**
	 * @return unit in which a given resource is described
	 */
	@Nullable
	String getUnit();

	/**
	 * @return (optional) expression written in EL used to recalculate given characteristic to common unit
	 */
	@Nullable
	String getToCommonUnitConverter();

	/**
	 * @return (optional) expression written in EL used to recalculate given characteristic from common unit
	 */
	@Nullable
	String getFromCommonUnitConverter();

	/**
	 * @return function represented in EL used to book resource characteristic for given task
	 */
	@Nullable
	String getResourceCharacteristicReservation();

	/**
	 * @return function represented in EL used to remove resource characteristic permanently
	 */
	@Nullable
	String getResourceCharacteristicSubtraction();

	/**
	 * @return function used to add resources
	 */
	@Nullable
	String getResourceCharacteristicAddition();

	/**
	 * Method reserve amount resource characteristic for given job
	 *
	 * @param requiredCharacteristic amount of the given resource that should be reserved
	 * @return resource amount after reserving it for job
	 */
	default Object reserveResourceCharacteristic(final ResourceCharacteristic requiredCharacteristic) {
		// when resource characteristic cannot be reserved
		if (StringUtils.isBlank(getResourceCharacteristicReservation())) {
			return getValue();
		}

		final Serializable expression = MVEL.compileExpression(getResourceCharacteristicReservation());
		final Map<String, Object> params = new HashMap<>();
		final Object amountToReserve = requiredCharacteristic.convertToCommonUnit();
		final Object ownedAmount = convertToCommonUnit();

		params.put("amountToReserve", amountToReserve);
		params.put("ownedAmount", ownedAmount);

		final Object newAmount = MVEL.executeExpression(expression, params);
		return convertFromCommonUnit(newAmount);
	}

	/**
	 * Method removes permanently the given amount of resource characteristic
	 *
	 * @param requiredCharacteristic amount of the given resource that should be removed
	 * @return resource amount after removing its part
	 */
	default Object removeResourceCharacteristic(final ResourceCharacteristic requiredCharacteristic) {
		// when resource characteristic cannot be removed
		if (StringUtils.isBlank(getResourceCharacteristicSubtraction())) {
			return getValue();
		}

		final Serializable expression = MVEL.compileExpression(getResourceCharacteristicSubtraction());
		final Map<String, Object> params = new HashMap<>();
		final Object amountToRemove = requiredCharacteristic.convertToCommonUnit();
		final Object ownedAmount = convertToCommonUnit();

		params.put("amountToRemove", amountToRemove);
		params.put("ownedAmount", ownedAmount);

		final Object newAmount = MVEL.executeExpression(expression, params);
		return convertFromCommonUnit(newAmount);
	}

	/**
	 * Method adds current resource to the resource of the same type passed as parameter
	 *
	 * @param resource resource to add
	 * @return incremented resource
	 * @apiNote mostly used to sum resources of custom structure
	 */
	default ResourceCharacteristic addResource(final ResourceCharacteristic resource) {
		// when none of the resource values are incremental
		if (StringUtils.isBlank(getResourceCharacteristicAddition()) || isNull(resource)) {
			return this;
		}

		final Serializable expression = MVEL.compileExpression(getResourceCharacteristicAddition());
		final Map<String, Object> params = new HashMap<>();
		final Object resource1 = convertToCommonUnit();
		final Object resource2 = resource.convertToCommonUnit();

		params.put("resource1", resource1);
		params.put("resource2", resource2);

		final Object newAmount = MVEL.executeExpression(expression, params);
		final Object result = convertFromCommonUnit(newAmount);

		return ImmutableResourceCharacteristic.copyOf(this).withValue(result);
	}

	/**
	 * Method adds two resources passed as parameters
	 *
	 * @param resource1 resource 1 to add
	 * @param resource2 resource 2 to add
	 * @return incremented resource
	 * @apiNote mostly used to sum resources of custom structure
	 */
	default ResourceCharacteristic addResource(final ResourceCharacteristic resource1,
			final ResourceCharacteristic resource2) {
		// when none of the resource values are incremental
		if (StringUtils.isBlank(getResourceCharacteristicAddition())) {
			return this;
		}

		final Serializable expression = MVEL.compileExpression(getResourceCharacteristicAddition());
		final Map<String, Object> params = new HashMap<>();
		final Object resource1Common = resource1.convertToCommonUnit();
		final Object resource2Common = resource2.convertToCommonUnit();

		params.put("resource1", resource1Common);
		params.put("resource2", resource2Common);

		final Object newAmount = MVEL.executeExpression(expression, params);
		final Object result = convertFromCommonUnit(newAmount);

		return ImmutableResourceCharacteristic.copyOf(this).withValue(result);
	}

	/**
	 * Method converts a given characteristic to common unit
	 */
	default Object convertToCommonUnit() {
		// if conversion is not necessary
		if (StringUtils.isBlank(getToCommonUnitConverter())) {
			return getValue();
		}

		final Serializable expression = commonConverters.containsKey(getToCommonUnitConverter()) ?
				MVEL.compileExpression(commonConverters.get(getToCommonUnitConverter())) :
				MVEL.compileExpression(getToCommonUnitConverter());
		final Map<String, Object> params = new HashMap<>();
		params.put("value", getValue());
		params.put("unit", getUnit());

		return MVEL.executeExpression(expression, params);
	}

	/**
	 * Method converts a given characteristic to common unit
	 */
	default Object convertFromCommonUnit(final Object value) {
		// if conversion is not necessary
		if (StringUtils.isBlank(getFromCommonUnitConverter())) {
			return value;
		}

		final Serializable expression = commonConverters.containsKey(getFromCommonUnitConverter()) ?
				MVEL.compileExpression(commonConverters.get(getFromCommonUnitConverter())) :
				MVEL.compileExpression(getFromCommonUnitConverter());
		final Map<String, Object> params = new HashMap<>();
		params.put("value", value);
		params.put("unit", getUnit());

		return MVEL.executeExpression(expression, params);
	}

	@Value.Check
	default void check() {
		if (!(!StringUtils.isBlank(getToCommonUnitConverter()) && !StringUtils.isBlank(getFromCommonUnitConverter())
				|| (StringUtils.isBlank(getToCommonUnitConverter()) && StringUtils.isBlank(
				getFromCommonUnitConverter())))) {
			throw new InvalidParameterException("Either none or both converters must be specified.");
		}
	}
}
