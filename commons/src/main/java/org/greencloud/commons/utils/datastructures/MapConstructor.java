package org.greencloud.commons.utils.datastructures;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
}
