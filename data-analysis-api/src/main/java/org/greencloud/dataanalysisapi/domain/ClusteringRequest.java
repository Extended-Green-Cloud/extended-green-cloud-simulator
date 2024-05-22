package org.greencloud.dataanalysisapi.domain;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object specifying the clustering request
 */
@JsonSerialize(as = ImmutableClusteringRequest.class)
@JsonDeserialize(as = ImmutableClusteringRequest.class)
@Value.Immutable
public interface ClusteringRequest {

	/**
	 * @return clustering configuration
	 */
	ClusteringConfiguration getConfiguration();

	/**
	 * @return data that is to be clustered
	 */
	List<Map<String, Object>> getData();
}
