package com.greencloud.application.agents.cloudnetwork.behaviour.df;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;

@ExtendWith(MockitoExtension.class)
class NetworkChangeListenerTest {

	@Mock
	CloudNetworkAgent cloudNetworkAgent;

	@Test
	void shouldCorrectlyHandleNewlyAddedServers() {
		// TODO
	}

	@Test
	void shouldCorrectlyHandleRemovedServers() {
		// TODO
	}
}
