package org.greencloud.dataanalysisapi.service;

import static java.util.Collections.singletonList;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BASIC_RESOURCES;
import static org.greencloud.dataanalysisapi.enums.ClusteringMethodEnum.K_MEANS;
import static org.greencloud.dataanalysisapi.enums.DimensionalityReductionEnum.PCA;
import static org.greencloud.dataanalysisapi.enums.ValidationMetricsEnum.SILHOUETTE;

import java.util.List;
import java.util.Map;

import org.greencloud.dataanalysisapi.api.DataClusteringApi;
import org.greencloud.dataanalysisapi.domain.ClusteringEncodingResponse;
import org.greencloud.dataanalysisapi.domain.ClusteringFeatures;
import org.greencloud.dataanalysisapi.domain.ClusteringMethod;
import org.greencloud.dataanalysisapi.domain.ClusteringParameters;
import org.greencloud.dataanalysisapi.domain.ImmutableClusteringFeatures;
import org.greencloud.dataanalysisapi.domain.ImmutableClusteringMethod;
import org.greencloud.dataanalysisapi.domain.ImmutableClusteringParameters;

public class DataClusteringServiceImpl implements DataClusteringService {

	private final DataClusteringApi clusteringApi;

	/**
	 * Default constructor.
	 */
	public DataClusteringServiceImpl() {
		this.clusteringApi = new DataClusteringApi();
	}

	@Override
	public ClusteringEncodingResponse clusterBasicResourcesAndEncodeDataWithKMeans(
			final List<Map<String, Object>> resources,
			final int clusterNumber) {
		final int finalClustersNumber = resources.size() > clusterNumber ?
				clusterNumber :
				resources.size() - 1;
		final ClusteringFeatures features = ImmutableClusteringFeatures.builder()
				.allFeatures(BASIC_RESOURCES)
				.build();
		final ClusteringMethod methods = ImmutableClusteringMethod.builder()
				.clustering(K_MEANS)
				.validation(singletonList(SILHOUETTE))
				.dimensionalityReduction(PCA)
				.build();
		final ClusteringParameters parameters = ImmutableClusteringParameters.builder()
				.clustering(List.of(finalClustersNumber))
				.build();

		return clusteringApi.getCodeForResources(resources, features, methods, parameters);
	}
}
