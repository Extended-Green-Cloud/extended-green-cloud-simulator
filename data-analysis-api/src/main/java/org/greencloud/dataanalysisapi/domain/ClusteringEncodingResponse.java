package org.greencloud.dataanalysisapi.domain;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object specifying the response received after clustering
 */
@JsonSerialize(as = ImmutableClusteringEncodingResponse.class)
@JsonDeserialize(as = ImmutableClusteringEncodingResponse.class)
@Value.Immutable
public interface ClusteringEncodingResponse {

	/**
	 * @return resources assigned to clusters
	 */
	Map<String, List<Map<String, Object>>> getClustering();

	/**
	 * @return clusters encoding
	 */
	Map<String, String> getEncoding();
}
