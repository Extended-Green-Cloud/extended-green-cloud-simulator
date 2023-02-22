package org.greencloud.managingsystem.agent.behaviour.executor;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static java.time.Instant.parse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.monitoring.goalservices.JobSuccessRatioService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.AddServerPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.ManagingAgentNode;

@ExtendWith(MockitoExtension.class)
class VerifyAdaptationActionResultUnitTest {

	private static final Instant ACTION_TIMESTAMP = parse("2022-01-01T10:30:00.000Z");
	private static final AdaptationAction ACTION = getAdaptationAction(ADD_SERVER);
	private static final Double INITIAL_QUALITY = 0.1;
	private static final Double RESULT_QUALITY = 0.2;

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

	AbstractPlan adaptationPlan;
	VerifyAdaptationActionResult verifyAdaptationActionResult;

	@BeforeEach
	void init() {
		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when(managingAgentNode.getDatabaseClient()).thenReturn(database);
		when(managingAgent.monitor()).thenReturn(monitoringService);
		when(database.readAdaptationAction(ACTION.getActionId())).thenReturn(ACTION);
		adaptationPlan = new AddServerPlan(managingAgent);
	}

	@Test
	void shouldCorrectlyProcessAfterWakeUp() {
		// given
		verifyAdaptationActionResult = new VerifyAdaptationActionResult(managingAgent, ACTION_TIMESTAMP,
				adaptationPlan.getAdaptationActionEnum(), null, INITIAL_QUALITY, adaptationPlan.enablePlanAction(), 5);
		when(monitoringService.getGoalService(any())).thenReturn(jobSuccessRatioService);
		when(jobSuccessRatioService.computeCurrentGoalQuality(anyInt())).thenReturn(RESULT_QUALITY);

		// when
		verifyAdaptationActionResult.onWake();

		// then
		verify(database).updateAdaptationAction(ACTION.getActionId(), Map.of(MAXIMIZE_JOB_SUCCESS_RATIO, 0.1,
				MINIMIZE_USED_BACKUP_POWER, 0.1, DISTRIBUTE_TRAFFIC_EVENLY, 0.1));
		verify(database).setAdaptationActionAvailability(ACTION.getActionId(), true);
	}
}
