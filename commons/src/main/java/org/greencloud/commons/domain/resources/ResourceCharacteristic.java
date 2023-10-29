package org.greencloud.commons.domain.resources;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.resource.ResourceConverterConstants.commonConverters;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

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
	String getResourceBooker();

	/**
	 * Method reserve amount resource characteristic for given job
	 *
	 * @param requiredCharacteristic amount of the given resource that should be reserved
	 * @return resource amount after reserving it for job
	 */
	default Object reserveResourceCharacteristic(final ResourceCharacteristic requiredCharacteristic) {
		// when resource characteristic cannot be reserved
		if (isNull(getResourceBooker())) {
			return getValue();
		}

		final Serializable expression = MVEL.compileExpression(getResourceBooker());
		final Map<String, Object> params = new HashMap<>();
		final Object amountToReserve = requiredCharacteristic.convertToCommonUnit();
		final Object ownedAmount = convertToCommonUnit();

		params.put("amountToReserve", amountToReserve);
		params.put("ownedAmount", ownedAmount);

		final Object newAmount = MVEL.executeExpression(expression, params);
		return convertFromCommonUnit(newAmount);
	}

	/**
	 * Method converts a given characteristic to common unit
	 */
	default Object convertToCommonUnit() {
		// if conversion is not necessary
		if (isNull(getToCommonUnitConverter())) {
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
		if (isNull(getFromCommonUnitConverter())) {
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
		if(!(nonNull(getToCommonUnitConverter()) && nonNull(getFromCommonUnitConverter())
						|| (isNull(getToCommonUnitConverter()) && isNull(getFromCommonUnitConverter())))) {
			throw new InvalidParameterException("Either none or both converters must be specified.");
		}
	}
}
