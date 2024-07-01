package org.greencloud.commons.utils.datastructures;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Method contains constructors of common maps
 */
public class MapConstructor {

	/**
	 * Method constructs a map in which all keys are assigned an initial boolean value.
	 *
	 * @param keys         set of keys
	 * @param initialValue initial boolean value
	 * @return boolean map
	 */
	public static Map<String, AtomicBoolean> constructBooleanMap(final Set<String> keys, final boolean initialValue) {
		return keys.stream().collect(toMap(key -> key, entry -> new AtomicBoolean(initialValue)));
	}

	/**
	 * Method constructs a map in which all keys are assigned an empty list containing values of the same type
	 * as the keys.
	 *
	 * @param keys set of keys
	 * @return list map
	 */
	public static <T> Map<T, List<T>> constructListMap(final Collection<T> keys) {
		return keys.stream().collect(toMap(key -> key, entry -> emptyList()));
	}

	/**
	 * Method constructs a map in which all keys are assigned an empty map of values and keys of the same type.
	 *
	 * @param keys set of keys
	 * @return list map
	 */
	public static <T> Map<T, Map<T, T>> constructMapWithMap(final Collection<T> keys) {
		return keys.stream().collect(toMap(key -> key, entry -> emptyMap()));
	}

	/**
	 * Method constructs a map in which all keys are assigned an atomic of initial value.
	 *
	 * @param initialMap map which is to be converted to atomic map
	 * @return list map
	 */
	public static <T, E> Map<T, AtomicReference<E>> constructAtomicMap(final Map<T, E> initialMap) {
		return initialMap.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> new AtomicReference<>(entry.getValue())));
	}

}
