package org.greencloud.gui.websocket;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom websocket server
 */
public class GuiWebSocketServer extends WebSocketServer {

	private static final Logger logger = LoggerFactory.getLogger(GuiWebSocketServer.class);

	public GuiWebSocketServer() {
		super(new InetSocketAddress(8080));
	}

	@Override
	public void onOpen(final WebSocket conn, final ClientHandshake handshake) {
		conn.send("Welcoming message!");
	}

	@Override
	public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		logger.warn("Connection closed by {}, code: {}, reason: {} ", remote ? "remote peer" : "us", code, reason);
	}

	@Override
	public void onMessage(final WebSocket conn, final String message) {
		getConnections().stream()
				.filter(connection -> !connection.equals(conn))
				.forEach(connection -> connection.send(message));
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		logger.error("WebSocket error! {}", ex.getMessage());
		conn.close();
	}

	@Override
	public void onStart() {
		logger.info("Starting websocket server!");
	}
}
