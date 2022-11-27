package org.greencloud.managingsystem.service.executor;

import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.greencloud.commons.job.ClientJobStatusEnum.FINISHED;
import static jade.core.AID.ISGUID;
import static org.greencloud.managingsystem.service.common.TestAdaptationPlanFactory.getTestAdaptationPlan;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.service.common.TestPlanParameters;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
class ExecutorServiceDatabaseTest {

	private static final Integer TEST_VALUE = 1;
	private static final AID TEST_AID = new AID("test", ISGUID);

	@Mock
	ManagingAgent managingAgent;
	@Mock
	ManagingAgentNode abstractAgentNode;
	AbstractPlan adaptationPlan;
	TimescaleDatabase database;
	MonitoringService monitoringService = new MonitoringService(managingAgent);
	ExecutorService executorService = spy(new ExecutorService(managingAgent));

	@BeforeEach
	void init() {
		database = spy(new TimescaleDatabase());
		database.initDatabase();

		monitoringService = spy(new MonitoringService(managingAgent));
		executorService = spy(new ExecutorService(managingAgent));

		when(managingAgent.monitor()).thenReturn(monitoringService);
		when(managingAgent.getAgentNode()).thenReturn(abstractAgentNode);
		when(abstractAgentNode.getDatabaseClient()).thenReturn(database);

		adaptationPlan = getTestAdaptationPlan(managingAgent, TEST_AID, new TestPlanParameters(TEST_VALUE));
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	void shouldCorrectlyExecuteAdaptationAction() {
		// given
		var monitoringData = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FINISHED)
				.isFinished(true)
				.build();
		database.writeMonitoringData("test", CLIENT_MONITORING, monitoringData);

		// when
		executorService.executeAdaptationAction(adaptationPlan);

		// then
		verify(managingAgent).addBehaviour(any(InitiateAdaptationActionRequest.class));
		verify(database).setAdaptationActionAvailability(1, false);
		verify(monitoringService).getGoalService(MAXIMIZE_JOB_SUCCESS_RATIO);
	}

	@Test
	void shouldThrowExceptionWhereNoGoalQualityData() {
		// when & then
		assertThrows(IllegalStateException.class, () -> executorService.executeAdaptationAction(adaptationPlan));
	}
}
