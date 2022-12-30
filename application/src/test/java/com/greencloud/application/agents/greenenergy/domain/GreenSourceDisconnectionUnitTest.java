package com.greencloud.application.agents.greenenergy.domain;

import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class GreenSourceDisconnectionUnitTest {

	@Test
	@DisplayName("Test reset")
	void testReset() {
		var greenSourceDisconnection = new GreenSourceDisconnection();
		var mockMessage = new ACLMessage(REQUEST);
		var mockServer = new AID("test_server", AID.ISGUID);

		greenSourceDisconnection.setBeingDisconnected(true);
		greenSourceDisconnection.setServerToBeDisconnected(mockServer);
		greenSourceDisconnection.setOriginalAdaptationMessage(mockMessage);

		assertThat(greenSourceDisconnection.isBeingDisconnected()).isTrue();
		assertThat(greenSourceDisconnection.getServerToBeDisconnected()).isEqualTo(mockServer);
		assertThat(greenSourceDisconnection.getOriginalAdaptationMessage()).isEqualTo(mockMessage);

		greenSourceDisconnection.reset();

		assertThat(greenSourceDisconnection.isBeingDisconnected()).isFalse();
		assertThat(greenSourceDisconnection.getServerToBeDisconnected()).isNull();
		assertThat(greenSourceDisconnection.getOriginalAdaptationMessage()).isNull();
	}

	@Test
	@DisplayName("Test is being disconnected from server")
	void testIsBeingDisconnectedFromServer() {
		var greenSourceDisconnection = new GreenSourceDisconnection();
		assertThat(greenSourceDisconnection.isBeingDisconnectedFromServer()).isFalse();

		greenSourceDisconnection.setBeingDisconnected(true);
		assertThat(greenSourceDisconnection.isBeingDisconnectedFromServer()).isFalse();

		greenSourceDisconnection.setServerToBeDisconnected(new AID("test_server", AID.ISGUID));
		assertThat(greenSourceDisconnection.isBeingDisconnectedFromServer()).isTrue();
	}
}
