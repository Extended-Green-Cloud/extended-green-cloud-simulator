package org.greencloud.commons.domain.agent;

import org.greencloud.commons.domain.job.extended.JobWithPrice;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object storing the data passed by the Server Agent
 */
@JsonSerialize(as = ImmutableServerData.class)
@JsonDeserialize(as = ImmutableServerData.class)
@Value.Immutable
@ImmutableConfig
public interface ServerData extends JobWithPrice {

	/**
	 * @return power consumption in given server
	 */
	double getPowerConsumption();

}
