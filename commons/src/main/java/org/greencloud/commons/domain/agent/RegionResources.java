package org.greencloud.commons.domain.agent;

import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Class describing a resources of given RMA
 */
@JsonSerialize(as = ImmutableRegionResources.class)
@JsonDeserialize(as = ImmutableRegionResources.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface RegionResources {

	/**
	 * @return resources of owned servers
	 */
	Map<String, ServerResources> getServersResources();
}
