package com.greencloud.application.agents.server.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greencloud.application.agents.server.ServerAgent;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
class ServerAdaptationManagementUnitTest {

	@Mock
	ServerAgent serverAgent;
	@Mock
	ServerConfigManagement configManagement;

	ServerAdaptationManagement serverAdaptationManagement;

	@BeforeEach
	void init() {
		serverAdaptationManagement = new ServerAdaptationManagement(serverAgent);
		when(serverAgent.manageConfig()).thenReturn(configManagement);
	}

	@ParameterizedTest
	@MethodSource("weightsMapProvider")
	void shouldCorrectlyAdjustWeights(Map<AID, Integer> oldMap, String greenSource, Map<AID, Integer> newMap,
			boolean expectedResult, int updatedMap) {
		// given
		when(configManagement.getWeightsForGreenSourcesMap()).thenReturn(oldMap);

		// when
		var result = serverAdaptationManagement.changeGreenSourceWeights(greenSource);

		// then
		assertThat(result).isEqualTo(expectedResult);
		verify(configManagement, times(updatedMap)).setWeightsForGreenSourcesMap(newMap);
	}

	private static Stream<Arguments> weightsMapProvider() {
		return Stream.of(
				arguments(
						Map.of(aid("gs1"), 1, aid("gs2"), 1, aid("gs3"), 1), "gs3",
						Map.of(aid("gs1"), 2, aid("gs2"), 2, aid("gs3"), 1), true, 1
				),
				arguments(
						Map.of(aid("gs1"), 1, aid("gs2"), 1, aid("gs3"), 1), "gs4",
						Map.of(aid("gs1"), 1, aid("gs2"), 1, aid("gs3"), 1), false, 0
				),
				arguments(
						Map.of(aid("gs1"), 1), "gs1", Map.of(aid("gs1"), 1), true, 1
				)
		);
	}

	private static AID aid(String greenSourceName) {
		return new AID(greenSourceName, AID.ISGUID);
	}
}
