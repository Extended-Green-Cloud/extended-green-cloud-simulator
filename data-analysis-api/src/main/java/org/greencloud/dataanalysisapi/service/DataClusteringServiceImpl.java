package org.greencloud.dataanalysisapi.service;

import static java.lang.Integer.max;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BASIC_RESOURCES;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.TYPE;
import static org.greencloud.dataanalysisapi.enums.ClusteringMethodEnum.FUZZY_C_MEANS;
import static org.greencloud.dataanalysisapi.enums.ClusteringMethodEnum.MODIFIED_K_MEANS;
import static org.greencloud.dataanalysisapi.enums.DimensionalityReductionEnum.NONE;
import static org.greencloud.dataanalysisapi.enums.DimensionalityReductionEnum.PCA;
import static org.greencloud.dataanalysisapi.enums.ValidationMetricsEnum.SILHOUETTE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.dataanalysisapi.api.DataClusteringApi;
import org.greencloud.dataanalysisapi.domain.ClusteringEncodingResponse;
import org.greencloud.dataanalysisapi.domain.ClusteringFeatures;
import org.greencloud.dataanalysisapi.domain.ClusteringMethod;
import org.greencloud.dataanalysisapi.domain.ClusteringParameters;
import org.greencloud.dataanalysisapi.domain.ImmutableClusteringEncodingResponse;
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
	public ClusteringEncodingResponse clusterBasicResourcesAndEncodeDataWithFuzzy(
			final List<Map<String, Object>> resources,
			final int clusterNumber) {
		final int finalClustersNumber = clampClusterNumber(resources.size(), clusterNumber);

		if (finalClustersNumber == 1) {
			final Map<String, List<Map<String, Object>>> clustering = new HashMap<>(Map.of("0", resources));
			final Map<String, String> encoding = Map.of("0", "111");

			return ImmutableClusteringEncodingResponse.builder()
					.clustering(clustering)
					.encoding(encoding)
					.build();
		}
		final ClusteringFeatures features = ImmutableClusteringFeatures.builder()
				.allFeatures(BASIC_RESOURCES)
				.build();
		final ClusteringMethod methods = ImmutableClusteringMethod.builder()
				.clustering(FUZZY_C_MEANS)
				.validation(singletonList(SILHOUETTE))
				.dimensionalityReduction(PCA)
				.build();
		final ClusteringParameters parameters = ImmutableClusteringParameters.builder()
				.clustering(List.of(finalClustersNumber))
				.build();

		return clusteringApi.getCodeForResources(resources, features, methods, parameters);
	}

	@Override
	public Map<String, List<Map<String, Object>>> clusterResourcesWithModifiedKMeans(
			final List<String> featureList, final List<Map<String, Object>> resources, final int clusterNumber) {
		final int finalClustersNumber = clampClusterNumber(resources.size(), clusterNumber);

		if (finalClustersNumber == 1) {
			return new HashMap<>(Map.of("0", resources));
		}

		final ClusteringFeatures features = ImmutableClusteringFeatures.builder()
				.allFeatures(featureList)
				.build();
		final ClusteringMethod methods = ImmutableClusteringMethod.builder()
				.clustering(MODIFIED_K_MEANS)
				.validation(singletonList(SILHOUETTE))
				.dimensionalityReduction(NONE)
				.build();
		final ClusteringParameters parameters = ImmutableClusteringParameters.builder()
				.clustering(List.of(finalClustersNumber))
				.build();

		return clusteringApi.getClusteredResources(resources, features, methods, parameters);
	}

	@Override
	public Map<String, List<Map<String, Object>>> clusterResourcesBasedOnPredefinedTypeLabels(
			final List<Map<String, Object>> jobsResources, final Map<String, String> encoding) {
		return jobsResources.stream()
				.map(jobResources -> Pair.of(encoding.getOrDefault((String) jobResources.get(TYPE), "1"), jobResources))
				.collect(groupingBy(Pair::getKey, mapping(Pair::getValue, toList())));
	}

	@Override
	public Map<String, List<Map<String, Object>>> clusterResourcesBasedOnPredefinedTypeLabels(
			final List<Map<String, Object>> jobsResources,
			final Map<String, String> encoding,
			final int clusterNo) {
		return jobsResources.stream()
				.map(jobResources -> Pair.of(getJobClusterByType(jobResources, encoding, clusterNo), jobResources))
				.collect(groupingBy(pair -> valueOf(pair.getKey()), mapping(Pair::getValue, toList())));
	}

	@Override
	public int clampClusterNumber(final int dataSize, final int clusterNumber) {
		return dataSize > clusterNumber ? clusterNumber : max(1, dataSize - 1);
	}

	private int getJobClusterByType(final Map<String, Object> jobResources, final Map<String, String> encoding,
			final int clusterNo) {
		final int initialCluster = parseInt(encoding.getOrDefault((String) jobResources.get(TYPE), "1"));
		return initialCluster < clusterNo ? initialCluster : clusterNo - 1;
	}
}
