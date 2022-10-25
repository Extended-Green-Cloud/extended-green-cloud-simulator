package com.gui.websocket;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GuiWebSocketClient extends WebSocketClient {

	private static final Logger logger = LoggerFactory.getLogger(GuiWebSocketClient.class);

	protected static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

	public GuiWebSocketClient(URI serverUri) {
		super(serverUri);
	}

	public void send(Object message) {
		try {
			if (super.isOpen()) {
				super.send(mapper.writeValueAsString(message));
			}
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		logger.info("Connected to WebSocket server");
	}

	@Override
	public void onMessage(String message) {
		logger.debug("Received message from WebSocket server: {}", message);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		logger.warn("Connection closed by {}, code: {}, reason: {} ", remote ? "remote peer" : "us", code, reason);
	}

	@Override
	public void onError(Exception e) {
		logger.error("WebSocket error! {}", e.getMessage());
	}
}
