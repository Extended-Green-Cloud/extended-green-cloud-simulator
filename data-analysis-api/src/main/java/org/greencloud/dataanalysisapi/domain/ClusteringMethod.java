package org.greencloud.dataanalysisapi.domain;

import java.util.List;

import javax.annotation.Nullable;

import org.greencloud.dataanalysisapi.enums.ClusteringMethodEnum;
import org.greencloud.dataanalysisapi.enums.DimensionalityReductionEnum;
import org.greencloud.dataanalysisapi.enums.ValidationMetricsEnum;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object specifying the methods used in clustering
 */
@JsonSerialize(as = ImmutableClusteringMethod.class)
@JsonDeserialize(as = ImmutableClusteringMethod.class)
@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ClusteringMethod {

	/**
	 * @return name of clustering method
	 */
	@Nullable
	ClusteringMethodEnum getClustering();

	/**
	 * @return optional list of validation metrics used
	 */
	@Nullable
	List<ValidationMetricsEnum> getValidation();

	/**
	 * @return name of dimensionality reduction method
	 */
	@Nullable
	@JsonProperty("dimensionality_reduction")
	DimensionalityReductionEnum getDimensionalityReduction();
}
