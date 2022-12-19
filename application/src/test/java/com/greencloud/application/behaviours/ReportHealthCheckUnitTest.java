package com.greencloud.application.behaviours;

import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.HealthCheck;
import com.greencloud.application.agents.AbstractAgent;

class ReportHealthCheckUnitTest {

	@Mock
	private AbstractAgent mockAgent;

	@BeforeEach
	void init() {
		mockAgent = mock(AbstractAgent.class);
	}

	@Test
	@DisplayName("Test reporting health check")
	void testOnTick() {
		final ReportHealthCheck reportHealthCheck = new ReportHealthCheck(mockAgent);

		reportHealthCheck.onTick();
		verify(mockAgent).writeMonitoringData(HEALTH_CHECK, new HealthCheck(true, mockAgent.getAgentType()));
	}
}
