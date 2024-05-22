package org.greencloud.dataanalysisapi.domain;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object specifying the configuration of requested clustering
 */
@JsonSerialize(as = ImmutableClusteringConfiguration.class)
@JsonDeserialize(as = ImmutableClusteringConfiguration.class)
@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ClusteringConfiguration {

	/**
	 * @return optional name of clustering
	 */
	@Nullable
	String getName();

	/**
	 * @return features used in clustering
	 */
	ClusteringFeatures getFeatures();

	/**
	 * @return names of methods used in clustering
	 */
	ClusteringMethod getMethod();

	/**
	 * @return parameters applied in clustering methods
	 */
	ClusteringParameters getParameters();
}
