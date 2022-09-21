package com.greencloud.application.domain;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



/**
 * Object storing data passed by the Cloud Network
 */
@JsonSerialize(as = ImmutableCloudNetworkData.class)
@JsonDeserialize(as = ImmutableCloudNetworkData.class)
@Immutable
public interface CloudNetworkData {

	/**
	 * @return power used by the Cloud Network
	 */
	int getInUsePower();

	/**
	 * @return number of currently running jobs
	 */
	int getJobsCount();

}
