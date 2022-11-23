package org.greencloud.managingsystem.service.analyzer;

import static com.database.knowledge.domain.action.AdaptationActionEnum.RECONFIGURE;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.data.Offset;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.goal.AdaptationGoal;

class AnalyzerServiceUnitTest {

	@Mock
	private ManagingAgent managingAgent;

	private AnalyzerService analyzerService;

	private static Stream<Arguments> parametersForQualityCalculationTest() {
		final AdaptationAction action1 =
				new AdaptationAction(1, "test_action1", RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO,
						Map.of(MAXIMIZE_JOB_SUCCESS_RATIO, 0.3, MINIMIZE_USED_BACKUP_POWER, 0.5,
								DISTRIBUTE_TRAFFIC_EVENLY, 0.4), true, 1);
		final AdaptationAction action2 =
				new AdaptationAction(2, "test_action1", RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO,
						Map.of(MAXIMIZE_JOB_SUCCESS_RATIO, 0.7, MINIMIZE_USED_BACKUP_POWER, 0.4,
								DISTRIBUTE_TRAFFIC_EVENLY, 0.3), true, 1);

		return Stream.of(
				Arguments.of(action1, 0.34),
				Arguments.of(action2, 0.59)
		);
	}

	@BeforeEach
	void init() {
		var successRatio = new AdaptationGoal(1, "Maximize job success ratio", 0.5, true, 0.7);
		var backupPower = new AdaptationGoal(2, "Minimize used backup power", 0.6, true, 0.1);
		var traffic = new AdaptationGoal(3, "Distribute traffic evenly", 0.7, false, 0.2);

		managingAgent = mock((ManagingAgent.class));

		analyzerService = new AnalyzerService(managingAgent);
		doReturn(List.of(successRatio, backupPower, traffic)).when(managingAgent).getAdaptationGoalList();
		doReturn(new MonitoringService(managingAgent)).when(managingAgent).monitor();
	}

	@ParameterizedTest
	@MethodSource("parametersForQualityCalculationTest")
	@DisplayName("Test computing quality of adaptation action")
	void testComputeQualityOfAdaptationAction(AdaptationAction action, double result) {
		assertThat(analyzerService.computeQualityOfAdaptationAction(action)).isCloseTo(result, Offset.offset(0.015));
	}
}
