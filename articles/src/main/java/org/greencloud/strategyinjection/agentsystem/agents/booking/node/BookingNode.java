package org.greencloud.strategyinjection.agentsystem.agents.booking.node;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.gui.websocket.GuiWebSocketClient;
import org.greencloud.strategyinjection.agentsystem.agents.booking.props.BookingProps;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingNode extends AgentNode<BookingProps> {

	private ConcurrentLinkedQueue<Pair<String, Object>> clientEvents;
	private AtomicInteger latestId;

	public BookingNode(final String name) {
		super(name, "BOOKING");
		this.clientEvents = new ConcurrentLinkedQueue<>();
		this.latestId = new AtomicInteger(0);
		connectSocket(null);
	}

	/**
	 * Method sends message to client informing about restaurant response
	 *
	 * @param messageToClient message content
	 */
	public void passRestaurantMessageToClient(final String messageToClient) {
		mainWebSocket.send(messageToClient);
	}

	@Override
	public GuiWebSocketClient initializeSocket(final String url) {
		return new BookingWebsocketClient(this);
	}

	@Override
	public void updateGUI(final BookingProps props) {
		// nothing should happen here
	}

	@Override
	public void saveMonitoringData(final BookingProps props) {
		// nothing should happen here
	}
}
