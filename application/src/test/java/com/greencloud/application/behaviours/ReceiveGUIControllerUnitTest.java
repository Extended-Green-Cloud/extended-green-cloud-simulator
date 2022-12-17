package com.greencloud.application.behaviours;

import static jade.wrapper.AgentController.ASYNC;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.monitoring.MonitoringAgent;
import com.gui.agents.MonitoringAgentNode;
import com.gui.controller.GuiControllerImpl;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;

class ReceiveGUIControllerUnitTest {

	@Mock
	private AbstractAgent mockAgent;

	@BeforeEach
	void init() {
		mockAgent = spy(new MonitoringAgent());
	}

	@Test
	@DisplayName("Test receiving agent object")
	void testAction() throws InterruptedException {
		var testBehaviour = mock(Behaviour.class);
		var testObject1 = new MonitoringAgentNode("test_agent", "test_gs");
		var testObject2 = new GuiControllerImpl();

		final ReceiveGUIController receiveGUIController = new ReceiveGUIController(mockAgent, singletonList(testBehaviour));

		mockAgent.putO2AObject(testObject1, ASYNC);

		receiveGUIController.action();
		verify(mockAgent).setAgentNode(testObject1);

		mockAgent.putO2AObject(testObject2, ASYNC);

		receiveGUIController.action();
		verify(mockAgent).setGuiController(testObject2);
		verify(mockAgent).addBehaviour(argThat(behaviour -> {
			var parallelBehaviour = (ParallelBehaviour) behaviour;
			Iterable<Behaviour> subBehaviourIt = () -> parallelBehaviour.getChildren().iterator();
			return StreamSupport.stream(subBehaviourIt.spliterator(), false).anyMatch(el -> el.equals(testBehaviour))
					&& parallelBehaviour.getChildren().size() == 3;
		}));
	}
}
