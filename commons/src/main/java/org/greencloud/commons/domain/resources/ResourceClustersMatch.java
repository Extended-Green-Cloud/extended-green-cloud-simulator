package org.greencloud.commons.domain.resources;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Match between job and resource clusters
 */
@JsonSerialize(as = ImmutableResourceClustersMatch.class)
@JsonDeserialize(as = ImmutableResourceClustersMatch.class)
@Value.Immutable
public interface ResourceClustersMatch {

	/**
	 * @return index of cluster with executor resources
	 */
	String getExecutorClusterIdx();

	/**
	 * @return index of cluster with job resources
	 */
	String getJobClusterIdx();
}
