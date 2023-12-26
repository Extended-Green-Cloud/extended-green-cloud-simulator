package org.greencloud.managingsystem.agent.behaviour.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.timescale.TimescaleDatabase;

class DisableAdaptationActionsDatabaseTest {
	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ManagingAgentNode mockAgentNode;

	private TimescaleDatabase database;

	@BeforeEach
	void init() {
		database = spy(TimescaleDatabase.setUpForTests());
		database.initDatabase();

		mockManagingAgent = spy(ManagingAgent.class);
		mockAgentNode = mock(ManagingAgentNode.class);

		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(database).when(mockAgentNode).getDatabaseClient();
		doNothing().when(mockAgentNode).registerManagingAgent(anyList());
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	@Disabled
	@DisplayName("Test disabling adaptation actions")
	void testAction() {
		var actionsToDisable = List.of("ADD_SERVER", "INCREASE_DEADLINE_PRIORITY", "INCREASE_POWER_PRIORITY");

		final DisableAdaptationActions disableAdaptationActions = new DisableAdaptationActions(mockManagingAgent,
				actionsToDisable);

		disableAdaptationActions.action();

		verify(mockManagingAgent).getAgentNode();
		verify(mockAgentNode).getDatabaseClient();

		assertThat(database.readAdaptationActions())
				.usingRecursiveFieldByFieldElementComparator()
				.allMatch(action -> !actionsToDisable.contains(action.getAction().toString()) ||
						(actionsToDisable.contains(action.getAction().toString()) && !action.getAvailable()));
	}

}
