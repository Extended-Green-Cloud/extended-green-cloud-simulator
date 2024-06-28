package org.greencloud.gui.websocket;

import static org.greencloud.commons.constants.TimeConstants.SECONDS_PER_HOUR;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.greencloud.gui.messages.ImmutableReportSystemStartTimeMessage;
import org.greencloud.gui.messages.ImmutableStrategyAllocationParametersMessage;
import org.jrba.environment.websocket.GuiWebSocketClient;

public class EGCSWebSocketClient extends GuiWebSocketClient {

	public EGCSWebSocketClient(URI serverUri) {
		super(serverUri);
	}

	/**
	 * Method sends the information about simulation start time to a given Websocket server
	 *
	 * @param time time when the simulation has started
	 */
	public void reportSystemStartTime(final Instant time) {
		this.send(ImmutableReportSystemStartTimeMessage.builder()
				.time(time.toEpochMilli())
				.secondsPerHour(SECONDS_PER_HOUR)
				.build());
	}

	/**
	 * Method sends the information about strategy parameters.
	 *
	 * @param resourceAllocationStrategy name of the resource allocation algorithm.
	 * @param prioritizationStrategy     name of the prioritization algorithm.
	 * @param appliedModifications       name of applied modifications.
	 * @param numberOfAllocationSteps    number of steps of allocation algorithm
	 */
	public void reportStrategyParameters(final String resourceAllocationStrategy,
			final String prioritizationStrategy,
			final List<String> appliedModifications,
			final int numberOfAllocationSteps) {
		this.send(ImmutableStrategyAllocationParametersMessage.builder()
				.allocationName(resourceAllocationStrategy)
				.prioritizationName(prioritizationStrategy)
				.allocationStepsNo(numberOfAllocationSteps)
				.modificationList(appliedModifications)
				.build());
	}

}
