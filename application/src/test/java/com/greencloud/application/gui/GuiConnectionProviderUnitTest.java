package com.greencloud.application.gui;

import static com.greencloud.application.gui.GuiConnectionProvider.connectAgentObject;
import static com.greencloud.application.gui.GuiConnectionProvider.connectToGui;
import static jade.wrapper.AgentController.ASYNC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.agents.monitoring.MonitoringAgent;
import com.gui.agents.MonitoringAgentNode;
import com.gui.controller.GuiControllerImpl;

class GuiConnectionProviderUnitTest {

	@Test
	@DisplayName("Test connect to GUI")
	void testConnectToGui() throws InterruptedException {
		var mockAgent = spy(new MonitoringAgent());
		var guiController = new GuiControllerImpl();
		var agentNode = new MonitoringAgentNode("test_agent", "test_gs");

		mockAgent.putO2AObject(guiController, ASYNC);
		mockAgent.putO2AObject(agentNode, ASYNC);

		connectToGui(mockAgent);

		assertThat(mockAgent.getAgentNode()).isEqualTo(agentNode);
		assertThat(mockAgent.getGuiController()).isEqualTo(guiController);
	}

	@Test
	@DisplayName("Test connect agent to object")
	void testConnectAgentObject() {
		var mockAgent = spy(new MonitoringAgent());
		var guiController = new GuiControllerImpl();

		connectAgentObject(mockAgent, 0, guiController);
		assertThat(mockAgent.getGuiController()).isEqualTo(guiController);
	}
}
