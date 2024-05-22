package org.greencloud.commons.domain.agent;

import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Class describing a resources of given component instance
 */
@JsonSerialize(as = ImmutableServerResources.class)
@JsonDeserialize(as = ImmutableServerResources.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface ServerResources {

	/**
	 * @return resources owned by a given server
	 */
	Map<String, Resource> getResources();
}
