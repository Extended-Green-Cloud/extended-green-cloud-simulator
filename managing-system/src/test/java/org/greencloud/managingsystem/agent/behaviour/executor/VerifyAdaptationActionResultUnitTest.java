package org.greencloud.managingsystem.agent.behaviour.executor;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static java.time.Instant.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.assertj.core.data.Offset;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.monitoring.goalservices.BackUpPowerUsageService;
import org.greencloud.managingsystem.service.monitoring.goalservices.JobSuccessRatioService;
import org.greencloud.managingsystem.service.monitoring.goalservices.TrafficDistributionService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.AddServerPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.connector.gui.agents.managing.ManagingAgentNode;

@ExtendWith(MockitoExtension.class)
class VerifyAdaptationActionResultUnitTest {

	private static final Instant ACTION_TIMESTAMP = parse("2022-01-01T10:30:00.000Z");
	private static final AdaptationAction ACTION = getAdaptationAction(ADD_SERVER).get(0);
	private static final Map<GoalEnum, Double> INITIAL_QUALITIES = Map.of(
			MINIMIZE_USED_BACKUP_POWER, 0.1,
			MAXIMIZE_JOB_SUCCESS_RATIO, 0.8,
			DISTRIBUTE_TRAFFIC_EVENLY, 0.5);
	private static final Double RESULT_QUALITY_SUCCESS_RATIO = 0.7;
	private static final Double RESULT_QUALITY_BACK_UP_POWER = 0.2;
	private static final Double RESULT_QUALITY_TRAFFIC = 0.3;
	private static final long DURATION = 7;

	@Mock
	ManagingAgent managingAgent;
	@Mock
	TimescaleDatabase database;
	@Mock
	ManagingAgentNode managingAgentNode;
	@Mock
	MonitoringService monitoringService;
	@Mock
	JobSuccessRatioService jobSuccessRatioService;
	@Mock
	TrafficDistributionService trafficDistributionService;
	@Mock
	BackUpPowerUsageService backUpPowerUsageService;

	AbstractPlan adaptationPlan;
	VerifyAdaptationActionResult verifyAdaptationActionResult;

	@BeforeEach
	void init() {
		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when(managingAgentNode.getDatabaseClient()).thenReturn(database);
		when(managingAgent.monitor()).thenReturn(monitoringService);
		when(database.readAdaptationAction(ACTION.getActionId())).thenReturn(ACTION);
		adaptationPlan = new AddServerPlan(managingAgent, MAXIMIZE_JOB_SUCCESS_RATIO);
	}

	@Test
	void shouldCorrectlyProcessAfterWakeUp() {
		// given
		verifyAdaptationActionResult = new VerifyAdaptationActionResult(managingAgent, ACTION_TIMESTAMP, DURATION,
				ACTION.getActionId(), null, INITIAL_QUALITIES, adaptationPlan.enablePlanAction(),
				5);

		when(monitoringService.getGoalService(MAXIMIZE_JOB_SUCCESS_RATIO)).thenReturn(jobSuccessRatioService);
		when(monitoringService.getGoalService(MINIMIZE_USED_BACKUP_POWER)).thenReturn(backUpPowerUsageService);
		when(monitoringService.getGoalService(DISTRIBUTE_TRAFFIC_EVENLY)).thenReturn(trafficDistributionService);

		when(jobSuccessRatioService.computeCurrentGoalQuality(anyInt())).thenReturn(RESULT_QUALITY_SUCCESS_RATIO);
		when(backUpPowerUsageService.computeCurrentGoalQuality(anyInt())).thenReturn(RESULT_QUALITY_BACK_UP_POWER);
		when(trafficDistributionService.computeCurrentGoalQuality(anyInt())).thenReturn(RESULT_QUALITY_TRAFFIC);

		// when
		verifyAdaptationActionResult.onWake();

		// then
		var expectedMap = Map.of(
				MAXIMIZE_JOB_SUCCESS_RATIO, -0.1,
				MINIMIZE_USED_BACKUP_POWER, 0.1,
				DISTRIBUTE_TRAFFIC_EVENLY, -0.2
		);
		final ArgumentCaptor<Map<GoalEnum, Double>> mapCaptor = forClass(Map.class);

		verify(database).updateAdaptationAction(eq(ACTION.getActionId()), mapCaptor.capture(), eq(7L));
		verify(database).setAdaptationActionAvailability(ACTION.getActionId(), true);

		assertThat(mapCaptor.getValue())
				.allSatisfy((goal, val) -> assertThat(expectedMap.get(goal)).isCloseTo(val, Offset.offset(0.00001)));
	}
}
