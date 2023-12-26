package org.greencloud.commons.args.event;

import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface containing properties of scenario event that generates new server in Regional Manager
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNewServerCreationEventArgs.class)
@JsonDeserialize(as = ImmutableNewServerCreationEventArgs.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName("SERVER_CREATION_EVENT")
public interface NewServerCreationEventArgs extends EventArgs {

	/**
	 * @return name of newly created agent
	 */
	String getName();

	/**
	 * @return name of regional manager to which the server is to be attached to
	 */
	String getRegionalManager();

	/**
	 * @return maximal power of server
	 */
	Double getMaxPower();

	/**
	 * @return idle power of server
	 */
	Double getIdlePower();

	/**
	 * @return resources that a new server has
	 */
	Map<String, Resource> getResources();

	/**
	 * @return maximal number of jobs that can be processed at once
	 */
	Long getJobProcessingLimit();

	/**
	 * @return price of job execution
	 */
	Double getPrice();
}
