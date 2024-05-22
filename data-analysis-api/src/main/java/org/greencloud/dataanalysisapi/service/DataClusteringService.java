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
	ClusteringEncodingResponse clusterBasicResourcesAndEncodeDataWithKMeans(final List<Map<String, Object>> resources,
			final int clusterNumber);
}
