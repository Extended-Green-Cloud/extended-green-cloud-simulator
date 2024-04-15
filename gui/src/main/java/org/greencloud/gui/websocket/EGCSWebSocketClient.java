package org.greencloud.gui.websocket;

import static org.greencloud.commons.constants.TimeConstants.SECONDS_PER_HOUR;

import java.net.URI;
import java.time.Instant;

import org.greencloud.gui.messages.ImmutableReportSystemStartTimeMessage;
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
}
