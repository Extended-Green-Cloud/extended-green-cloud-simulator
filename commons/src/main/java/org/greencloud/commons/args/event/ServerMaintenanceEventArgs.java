package org.greencloud.commons.args.event;

import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.enums.event.EventTypeEnum.CLIENT_CREATION_EVENT;

import java.util.Map;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface containing properties of scenario event that change server resources
 */
@Value.Immutable
@JsonSerialize(as = ImmutableServerMaintenanceEventArgs.class)
@JsonDeserialize(as = ImmutableServerMaintenanceEventArgs.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName("SERVER_MAINTENANCE_EVENT")
public interface ServerMaintenanceEventArgs extends EventArgs {

	/**
	 * @return name of the server agent
	 */
	String getName();

	/**
	 * @return new server resources
	 */
	Map<String, Resource> getResources();

	/**
	 * @return flag indicating if the server should be switched instantly after performing the maintenance
	 */
	@Nullable
	Boolean getSwitchAfter();

	/**
	 * Method verifies the correctness of new client event structure
	 */
	@Override
	@Value.Check
	default void check() {
		EventArgs.super.check();

		if (!getResources().containsKey(CPU)) {
			throw new InvalidScenarioEventStructure("Invalid server resource. Server must define value of CPU!");
		}
	}
}
