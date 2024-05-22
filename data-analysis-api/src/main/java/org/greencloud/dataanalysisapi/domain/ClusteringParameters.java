package org.greencloud.dataanalysisapi.domain;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object specifying the parameters applied in clustering
 */
@JsonSerialize(as = ImmutableClusteringParameters.class)
@JsonDeserialize(as = ImmutableClusteringParameters.class)
@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ClusteringParameters {

	/**
	 * @return optional list of clustering parameters
	 */
	@Nullable
	List<Object> getClustering();

	/**
	 * @return optional list of dimensionality reduction parameters
	 */
	@Nullable
	@JsonProperty("dimensionality_reduction")
	List<Object> getValidation();
}
