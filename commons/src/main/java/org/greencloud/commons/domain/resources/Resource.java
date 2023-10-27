package org.greencloud.commons.domain.resources;

import static java.lang.Double.parseDouble;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.constants.resource.ResourceAdditionConstants.ADD_BY_AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.TAKE_FROM_INITIAL_KNOWLEDGE;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.getDefaultEmptyResource;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.mvel2.MVEL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.errorprone.annotations.Var;

/**
 * Class describing a single hardware resource
 */
@JsonSerialize(as = ImmutableResource.class)
@JsonDeserialize(as = ImmutableResource.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Style(underrideHashCode = "hash")
@Value.Immutable(prehash = true)
public interface Resource {

	/**
	 * @return characteristics of a given resource
	 */
	Map<String, ResourceCharacteristic> getCharacteristics();

	/**
	 * @return resource representation when it is fully occupied
	 */
	@Value.Default
	default Resource getEmptyResource() {
		return getDefaultEmptyResource(this);
	}

	/**
	 * @return validation function that verifies if the characteristics of a given resource are sufficient
	 */
	@Nullable
	String getSufficiencyValidator();

	/**
	 * @return function used to add resources
	 */
	@Nullable
	String getResourceAddition();

	/**
	 * @return function used to compare the same resources
	 */
	@Nullable
	String getResourceComparator();

	/**
	 * Method returns (if available) amount of the given resource. If the amount is not among resource characteristics,
	 * then value 0 is returned.
	 */
	@JsonIgnore
	default Double getAmount() {
		return getCharacteristics().containsKey(AMOUNT) ?
				parseDouble(getCharacteristics().get(AMOUNT).getValue().toString()) :
				-1;
	}

	/**
	 * Method returns (if available) amount of the given resource represented in common unit.
	 * If the amount is not among resource characteristics, then value 0 is returned.
	 */
	@JsonIgnore
	default Double getAmountInCommonUnit() {
		return getCharacteristics().containsKey(AMOUNT) ?
				parseDouble(getCharacteristics().get(AMOUNT).convertToCommonUnit().toString()) :
				-1;
	}

	/**
	 * Method verifies if the given resource is sufficient to fulfill upcoming resource requirements
	 *
	 * @param resources resource requirements
	 * @return information if given resource complies with requirements
	 */
	default boolean isSufficient(final Map<String, Resource> resources) {
		// when the validation of given resource can be omitted at this step
		if (isNull(getSufficiencyValidator())) {
			return true;
		}
		if (getSufficiencyValidator().equals(TAKE_FROM_INITIAL_KNOWLEDGE)) {
			return false;
		}

		final Serializable expression = MVEL.compileExpression(getSufficiencyValidator());
		final Map<String, Object> params = new HashMap<>();
		params.put("requirements", resources);
		params.put("resource", this);

		return (boolean) MVEL.executeExpression(expression, params);
	}

	/**
	 * Method compares two resources
	 *
	 * @param resource1 first resource to compare
	 * @param resource2 second resource to compare
	 * @return 0 if both resources are the same, -1 if this resource is less than another resource and 1 otherwise
	 */
	default int compareResource(final Resource resource1, final Resource resource2) {
		// when resources are non-comparable
		if (isNull(getResourceComparator())) {
			return 0;
		}

		final Serializable expression = MVEL.compileExpression(getResourceComparator());
		final Map<String, Object> params = new HashMap<>();
		params.put("resource1", resource1);
		params.put("resource2", resource2);

		return (int) Double.parseDouble(MVEL.executeExpression(expression, params).toString());
	}

	/**
	 * Method adds current resource to the resource of the same type passed as parameter
	 *
	 * @param resource resource to add
	 * @return incremented resource
	 * @apiNote mostly used to sum resources of custom structure
	 */
	default Resource addResource(final Resource resource) {
		// when none of the resource values are incremental
		if (isNull(getResourceAddition()) || isNull(resource)) {
			return this;
		}
		if (getResourceAddition().equals(ADD_BY_AMOUNT)) {
			return addResourceAmounts(this, resource);
		}

		final Serializable expression = MVEL.compileExpression(getResourceAddition());
		final Map<String, Object> params = new HashMap<>();
		params.put("currentResource", this);
		params.put("resourceToAdd", resource);

		return (Resource) MVEL.executeExpression(expression, params);
	}

	/**
	 * Method adds two resources passed as parameters
	 *
	 * @param resource1 resource 1 to add
	 * @param resource2 resource 2 to add
	 * @return incremented resource
	 * @apiNote mostly used to sum resources of custom structure
	 */
	default Resource addResource(final Resource resource1, final Resource resource2) {
		// when none of the resource values are incremental
		if (isNull(getResourceAddition())) {
			return this;
		}
		if (getResourceAddition().equals(ADD_BY_AMOUNT)) {
			return addResourceAmounts(resource1, resource2);
		}

		final Serializable expression = MVEL.compileExpression(getResourceAddition());
		final Map<String, Object> params = new HashMap<>();
		params.put("currentResource", resource1);
		params.put("resourceToAdd", resource2);

		return (Resource) MVEL.executeExpression(expression, params);
	}

	/**
	 * Method adds resource amount to the amount of the resource passed as parameter
	 *
	 * @param resource1 resource 1 to add
	 * @param resource2 resource 1 to add
	 * @return incremented resource
	 * @apiNote mostly used to sum resources specified only by amounts
	 */
	default Resource addResourceAmounts(final Resource resource1, final Resource resource2) {
		final Object commonCurrent = resource1.getCharacteristics().get(AMOUNT).convertToCommonUnit();
		final Object commonOther = resource2.getCharacteristics().get(AMOUNT).convertToCommonUnit();
		final Object newAmount =
				Double.parseDouble(commonCurrent.toString()) + Double.parseDouble(commonOther.toString());
		final Object convertedAmount = getCharacteristics().get(AMOUNT).convertFromCommonUnit(newAmount);
		final ResourceCharacteristic newAmountChar =
				ImmutableResourceCharacteristic.copyOf(getCharacteristics().get(AMOUNT)).withValue(convertedAmount);

		final Map<String, ResourceCharacteristic> newCharacteristicMap = new HashMap<>(getCharacteristics());
		newCharacteristicMap.replace(AMOUNT, newAmountChar);

		return ImmutableResource.copyOf(this).withCharacteristics(newCharacteristicMap);
	}

	/**
	 * Method reserves resource characteristics
	 *
	 * @param resourceToReserve resources to be reserved
	 * @return resource after reserving required amounts
	 */
	default Resource reserveResource(final Resource resourceToReserve) {
		if (isNull(resourceToReserve)) {
			return ImmutableResource.copyOf(this);
		}

		final Map<String, ResourceCharacteristic> newCharacteristics = getCharacteristics().entrySet().stream()
				.map(characteristic -> {
					if (!resourceToReserve.getCharacteristics().containsKey(characteristic.getKey())) {
						return characteristic;
					} else {
						final ResourceCharacteristic correspondingCharacteristic =
								resourceToReserve.getCharacteristics().get(characteristic.getKey());
						final Object newValue =
								characteristic.getValue().reserveResourceCharacteristic(correspondingCharacteristic);
						final ResourceCharacteristic newCharacteristic =
								ImmutableResourceCharacteristic.copyOf(characteristic.getValue()).withValue(newValue);
						return new AbstractMap.SimpleEntry<>(characteristic.getKey(), newCharacteristic);
					}
				})
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return ImmutableResource.copyOf(this).withCharacteristics(newCharacteristics);
	}

	@Value.Check
	default void check() {
		if (getCharacteristics().containsKey(AMOUNT) && !(getCharacteristics().get(AMOUNT)
				.getValue() instanceof Number)) {
			throw new InvalidParameterException("The \"amount\" resource characteristic must be a number!");
		}
	}

	default int hash() {
		@Var int h = 5381;
		h += (h << 5) + getCharacteristics().hashCode();
		h += (h << 5) + Objects.hashCode(getSufficiencyValidator());
		h += (h << 5) + Objects.hashCode(getResourceAddition());
		h += (h << 5) + Objects.hashCode(getResourceComparator());
		return h;
	}
}
