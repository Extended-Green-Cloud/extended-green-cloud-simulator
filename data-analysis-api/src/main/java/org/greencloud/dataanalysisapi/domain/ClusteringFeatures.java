package org.greencloud.dataanalysisapi.domain;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object specifying the features used in clustering
 */
@JsonSerialize(as = ImmutableClusteringFeatures.class)
@JsonDeserialize(as = ImmutableClusteringFeatures.class)
@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ClusteringFeatures {

	/**
	 * @return list of all features used in clustering
	 */
	@JsonProperty("all_features")
	List<String> getAllFeatures();

	/**
	 * @return optional list of categorical features
	 */
	@Nullable
	@JsonProperty("categorical_features")
	List<String> getCategoricalFeatures();
}
