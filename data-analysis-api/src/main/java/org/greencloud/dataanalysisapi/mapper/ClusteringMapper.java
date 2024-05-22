package org.greencloud.dataanalysisapi.mapper;

import java.util.List;
import java.util.Map;

import org.greencloud.dataanalysisapi.domain.ClusteringConfiguration;
import org.greencloud.dataanalysisapi.domain.ClusteringFeatures;
import org.greencloud.dataanalysisapi.domain.ClusteringMethod;
import org.greencloud.dataanalysisapi.domain.ClusteringParameters;
import org.greencloud.dataanalysisapi.domain.ClusteringRequest;
import org.greencloud.dataanalysisapi.domain.ImmutableClusteringConfiguration;
import org.greencloud.dataanalysisapi.domain.ImmutableClusteringRequest;

/**
 * Class with methods used to map clustering parameters
 */
@SuppressWarnings("unchecked")
public class ClusteringMapper {

	/**
	 * Method maps individual configuration properties to ClusteringConfiguration.
	 *
	 * @param clusteringFeatures   features used in clustering
	 * @param clusteringMethod     methods used in clustering
	 * @param clusteringParameters parameters used in clustering
	 * @return ClusteringConfiguration
	 */
	public static ClusteringConfiguration mapToClusteringConfig(final ClusteringFeatures clusteringFeatures,
			final ClusteringMethod clusteringMethod,
			final ClusteringParameters clusteringParameters) {
		return ImmutableClusteringConfiguration.builder()
				.features(clusteringFeatures)
				.method(clusteringMethod)
				.parameters(clusteringParameters)
				.build();
	}

	/**
	 * Method maps data and configuration to ClusteringRequest object.
	 *
	 * @param data          data to be clustered
	 * @param configuration clustering configuration
	 * @return ClusteringRequest
	 */
	public static ClusteringRequest mapToClusteringRequest(final List<Map<String, Object>> data,
			final ClusteringConfiguration configuration) {
		return ImmutableClusteringRequest.builder().configuration(configuration).data(data).build();
	}
}
