package org.greencloud.commons.utils.event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.greencloud.commons.args.event.NewClientEventArgs;
import org.greencloud.commons.args.event.NewGreenSourceCreationEventArgs;
import org.greencloud.commons.args.event.NewServerCreationEventArgs;
import org.greencloud.commons.exception.InvalidScenarioEventStructure;

/**
 * Class containing methods used to validate different types of events
 */
public class EventValidator {

	/**
	 * Method used to validate if collection of server events is correct
	 *
	 * @param events collection of new server creation events
	 */
	public static void validateServerEvents(final List<NewServerCreationEventArgs> events) {
		final Set<String> serverNameSet = new HashSet<>();
		events.forEach(server -> {
			if (!serverNameSet.add(server.getName())) {
				throw new InvalidScenarioEventStructure(
						String.format("Servers must have unique names. Duplicated server name: %s", server.getName()));
			}
		});
	}

	/**
	 * Method used to validate if collection of green source events is correct
	 *
	 * @param events collection of new green source creation events
	 */
	public static void validateGreenSourceEvents(final List<NewGreenSourceCreationEventArgs> events) {
		final Set<String> greenSourceNameSet = new HashSet<>();
		events.forEach(greenSource -> {
			if (!greenSourceNameSet.add(greenSource.getName())) {
				throw new InvalidScenarioEventStructure(
						String.format("Green Source must have unique names. Duplicated green source name: %s",
								greenSource.getName()));
			}
		});
	}

	/**
	 * Method validates the collection of client creation events
	 *
	 * @param events collection of new client creation events
	 */
	public static void validateClientCreationEvents(final List<NewClientEventArgs> events) {
		final Set<String> clientNameSet = new HashSet<>();
		final Set<Integer> jobIdSet = new HashSet<>();

		events.forEach(client -> {
			if (!clientNameSet.add(client.getName())) {
				throw new InvalidScenarioEventStructure(
						String.format("Clients must have unique names. Duplicated client name: %s", client.getName()));
			}
			if (!jobIdSet.add(client.getJobId())) {
				throw new InvalidScenarioEventStructure(
						String.format("Specified job ids must be unique. Duplicated job id: %d", client.getJobId()));
			}
		});
	}
}
