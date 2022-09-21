package com.greencloud.application.agents.client;

import static java.time.Instant.parse;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.utils.TimeUtils;

import jade.junit.jupiter.JadeAgent;
import jade.junit.jupiter.JadeExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(JadeExtension.class)
@MockitoSettings(strictness = LENIENT)
class ClientAgentTest {

	private static final Instant NOW = parse("2022-08-15T16:30:00.000Z");

	@JadeAgent(name = "test_agent", type = "com.greencloud.application.agents.client.ClientAgent", arguments = { "15/08/2022 16:30",
			"15/08/2022 18:30", "100", "1" })
	private ClientAgent agent;

	@BeforeAll
	static void init() {
		//AbstractAgent.disableGui();
		TimeUtils.useMockTime(NOW, ZoneId.of("UTC"));
	}

	@Test
	void shouldCreateClientAgentAndKillItWhenNoCloudNetworkAgents() throws InterruptedException {
		// given
		assertThat(agent.getLocalName())
				.as("Created agent should have correct properties")
				.isEqualTo("test_agent");

		// when
		await().atMost(10, SECONDS).until(() -> !agent.isAlive());

		// then
		assertThat(agent.isAlive())
				.as("Agent should be removed when no cloud network com.greencloud.application.agents were found")
				.isFalse();
	}
}
