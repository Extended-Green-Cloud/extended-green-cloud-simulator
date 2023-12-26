package org.greencloud.commons.args.event;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface containing properties of scenario event that enables (switches on) a given server
 */
@Value.Immutable
@JsonSerialize(as = ImmutableEnableServerEventArgs.class)
@JsonDeserialize(as = ImmutableEnableServerEventArgs.class)
@JsonTypeName("ENABLE_SERVER_EVENT")
public interface EnableServerEventArgs extends EventArgs {

	/**
	 * @return name of the new server agent that is to be enabled
	 */
	String getName();

}
