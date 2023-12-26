package org.greencloud.commons.args.event;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface containing properties of scenario event that disables (switches off) a given server
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDisableServerEventArgs.class)
@JsonDeserialize(as = ImmutableDisableServerEventArgs.class)
@JsonTypeName("DISABLE_SERVER_EVENT")
public interface DisableServerEventArgs extends EventArgs {

	/**
	 * @return name of the new server agent that is to be disabled
	 */
	String getName();

}
