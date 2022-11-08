package com.greencloud.commons.args.event.newclient;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.event.EventArgs;

/**
 * Interface containing properties of scenario event that generates new client in Cloud Network
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNewClientEventArgs.class)
@JsonDeserialize(as = ImmutableNewClientEventArgs.class)
@JsonTypeName("NEW_CLIENT_EVENT")
public interface NewClientEventArgs extends EventArgs {

	/**
	 * @return name of the new client agent
	 */
	String getName();

	/**
	 * @return unique job identifier
	 */
	String getJobId();

	/**
	 * @return number of hours after which the job execution should start
	 */
	Integer getStart();

	/**
	 * @return number of hours after which the job execution should finish
	 */
	Integer getEnd();

	/**
	 * @return power required for the job
	 */
	Integer getPower();
}
