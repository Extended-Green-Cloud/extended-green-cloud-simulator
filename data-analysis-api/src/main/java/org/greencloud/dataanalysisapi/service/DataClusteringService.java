package org.greencloud.dataanalysisapi.service;

import java.util.List;
import java.util.Map;

import org.greencloud.dataanalysisapi.domain.ClusteringEncodingResponse;

/**
 * Service with common methods used in data clustering
 */
public interface DataClusteringService {

	/**
	 * Method cluster resources (CPU, Memory and Storage) using K-Means clustering and returns codes for each resource
	 * based on clusters to which they are assigned.
	 *
	 * @param resources     resources to be clustered
	 * @param clusterNumber number of resulting clusters
	 * @return resource encoding
	 * @implNote used method: [<a href="https://www.sciencedirect.com/science/article/pii/S0020025520304588">Intent-based allocation</a>]
	 */
	ClusteringEncodingResponse clusterBasicResourcesAndEncodeDataWithFuzzy(final List<Map<String, Object>> resources,
			final int clusterNumber);

	/**
	 * Method cluster resources (CPU, Memory and Storage) using modified K-Means clustering.
	 *
	 * @param featureList   features that are to be used in clustering
	 * @param resources     resources to be clustered
	 * @param clusterNumber number of resulting clusters
	 * @return clustered resources
	 * @implNote used method: [<a href="https://www.sciencedirect.com/science/article/pii/S1110866519303330">Priority-based allocation</a>]
	 */
	Map<String, List<Map<String, Object>>> clusterResourcesWithModifiedKMeans(
			final List<String> featureList, final List<Map<String, Object>> resources, final int clusterNumber);

	/**
	 * Method clusters job resources using initial type encoding.
	 *
	 * @param jobsResources job resources to be clustered
	 * @param encoding      initial encoding
	 * @return clustered resources
	 */
	Map<String, List<Map<String, Object>>> clusterResourcesBasedOnPredefinedTypeLabels(
			final List<Map<String, Object>> jobsResources, final Map<String, String> encoding);

	/**
	 * Method clusters job resources using initial type encoding.
	 *
	 * @param jobsResources job resources to be clustered
	 * @param encoding      initial encoding
	 * @param clusterNo     number of clusters
	 * @return clustered resources
	 */
	Map<String, List<Map<String, Object>>> clusterResourcesBasedOnPredefinedTypeLabels(
			final List<Map<String, Object>> jobsResources, final Map<String, String> encoding, final int clusterNo);

	/**
	 * Method clamps clusters number accordingly to the data size.
	 *
	 * @param dataSize      size of the data
	 * @param clusterNumber number of clusters
	 * @return final clusters number
	 */
	public int clampClusterNumber(final int dataSize, final int clusterNumber);
}
