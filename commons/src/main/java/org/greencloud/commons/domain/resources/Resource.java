package org.greencloud.commons.domain.resources;

import static java.lang.Double.parseDouble;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
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

import org.apache.commons.lang3.StringUtils;
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
@Value.Style(underrideHashCode = "hash", underrideEquals = "equalTo")
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
	String getResourceValidator();

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
	 * @param resource resource requirements
	 * @return information if given resource complies with requirements
	 */
	default boolean isSufficient(final Resource resource) {
		// when the validation of given resource can be omitted at this step
		if (StringUtils.isBlank(getResourceValidator())) {
			return true;
		}
		if (getResourceValidator().equals(TAKE_FROM_INITIAL_KNOWLEDGE)) {
			return false;
		}

		final Serializable expression = MVEL.compileExpression(getResourceValidator());
		final Map<String, Object> params = new HashMap<>();
		params.put("requirements", resource);
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
		if (StringUtils.isBlank(getResourceComparator())) {
			return 0;
		}

		final Serializable expression = MVEL.compileExpression(getResourceComparator());
		final Map<String, Object> params = new HashMap<>();
		params.put("resource1", resource1);
		params.put("resource2", resource2);

		return (int) Double.parseDouble(MVEL.executeExpression(expression, params).toString());
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

	/**
	 * Method removes resource characteristic value
	 *
	 * @param resourceToRemove resources to be removed
	 * @return resource after removing required amounts
	 */
	default Resource removeResourceAmounts(final Resource resourceToRemove) {
		if (isNull(resourceToRemove)) {
			return ImmutableResource.copyOf(this);
		}

		final Map<String, ResourceCharacteristic> newCharacteristics = getCharacteristics().entrySet().stream()
				.map(characteristic -> {
					if (!resourceToRemove.getCharacteristics().containsKey(characteristic.getKey())) {
						return characteristic;
					} else {
						final ResourceCharacteristic correspondingCharacteristic =
								resourceToRemove.getCharacteristics().get(characteristic.getKey());
						final Object newValue =
								characteristic.getValue().removeResourceCharacteristic(correspondingCharacteristic);
						final ResourceCharacteristic newCharacteristic =
								ImmutableResourceCharacteristic.copyOf(characteristic.getValue()).withValue(newValue);
						return new AbstractMap.SimpleEntry<>(characteristic.getKey(), newCharacteristic);
					}
				})
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return ImmutableResource.copyOf(this).withCharacteristics(newCharacteristics);
	}

	/**
	 * Method adds resource characteristics
	 *
	 * @param resourceToAdd resources to be added
	 * @return resource after adding required amounts
	 */
	default Resource addResource(final Resource resourceToAdd) {
		if (isNull(resourceToAdd)) {
			return ImmutableResource.copyOf(this);
		}

		final Map<String, ResourceCharacteristic> newCharacteristics = getCharacteristics().entrySet().stream()
				.map(characteristic -> {
					if (!resourceToAdd.getCharacteristics().containsKey(characteristic.getKey())) {
						return characteristic;
					} else {
						final ResourceCharacteristic correspondingCharacteristic =
								resourceToAdd.getCharacteristics().get(characteristic.getKey());
						final Object newValue = characteristic.getValue().addResource(correspondingCharacteristic)
								.getValue();
						final ResourceCharacteristic newCharacteristic =
								ImmutableResourceCharacteristic.copyOf(characteristic.getValue()).withValue(newValue);
						return new AbstractMap.SimpleEntry<>(characteristic.getKey(), newCharacteristic);
					}
				})
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return ImmutableResource.copyOf(this).withCharacteristics(newCharacteristics);
	}

	/**
	 * Method adds characteristics of 2 resources
	 *
	 * @param resource1 first resource to be added
	 * @param resource2 second resource to be added
	 * @return resource after adding required amounts
	 */
	default Resource addResource(final Resource resource1, final Resource resource2) {
		if (isNull(resource1) || isNull(resource2)) {
			return ImmutableResource.copyOf(this);
		}

		final Map<String, ResourceCharacteristic> newCharacteristics = getCharacteristics().entrySet().stream()
				.map(characteristic -> {
					if (!resource1.getCharacteristics().containsKey(characteristic.getKey())
							&& !resource2.getCharacteristics().containsKey(characteristic.getKey())) {
						return new AbstractMap.SimpleEntry<>(characteristic.getKey(),
								getEmptyResource().getCharacteristics().get(characteristic.getKey()));
					} else if (resource1.getCharacteristics().containsKey(characteristic.getKey())
							&& !resource2.getCharacteristics().containsKey(characteristic.getKey())) {
						return new AbstractMap.SimpleEntry<>(characteristic.getKey(),
								resource1.getCharacteristics().get(characteristic.getKey()));
					} else if (!resource1.getCharacteristics().containsKey(characteristic.getKey())
							&& resource2.getCharacteristics().containsKey(characteristic.getKey())) {
						return new AbstractMap.SimpleEntry<>(characteristic.getKey(),
								resource2.getCharacteristics().get(characteristic.getKey()));
					} else {
						final ResourceCharacteristic correspondingCharacteristic1 = resource1.getCharacteristics()
								.get(characteristic.getKey());
						final ResourceCharacteristic correspondingCharacteristic2 = resource2.getCharacteristics()
								.get(characteristic.getKey());
						final Object newValue = characteristic.getValue()
								.addResource(correspondingCharacteristic1, correspondingCharacteristic2).getValue();
						final ResourceCharacteristic newCharacteristic =
								ImmutableResourceCharacteristic.copyOf(characteristic.getValue())
										.withValue(newValue);
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
		h += (h << 5) + Objects.hashCode(getResourceValidator());
		h += (h << 5) + Objects.hashCode(getResourceComparator());
		return h;
	}

	default boolean equalTo(ImmutableResource another) {
		if (this == another)
			return true;
		return another != null
				&& getCharacteristics().equals(another.getCharacteristics())
				&& Objects.equals(getResourceValidator(), another.getResourceValidator())
				&& Objects.equals(getResourceComparator(), another.getResourceComparator());
	}
}
