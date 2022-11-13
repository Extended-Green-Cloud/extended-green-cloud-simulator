package com.greencloud.application.agents.client.behaviour.df;

import static com.greencloud.application.yellowpages.domain.DFServiceConstants.CNA_SERVICE_TYPE;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SCHEDULER_SERVICE_TYPE;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.yellowpages.YellowPagesService;
import com.gui.controller.GuiController;

import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;

@ExtendWith(MockitoExtension.class)
class FindCloudNetworkAgentsUnitTest {

	@Mock
	private ClientAgent clientAgent;
	@Mock
	private GuiController guiController;
	private FindSchedulerAgent testedBehaviour;

	@BeforeEach
	void init() {
		testedBehaviour = new FindSchedulerAgent();
		SequentialBehaviour parentBehaviour = new SequentialBehaviour();
		parentBehaviour.addSubBehaviour(testedBehaviour);
	}

	@Test
	void shouldDeleteAgentWhenNoSchedulerAgentsFound() {
		// given
		testedBehaviour.setAgent(clientAgent);

		// when
		try (var yellowPagesService = mockStatic(YellowPagesService.class)) {
			yellowPagesService.when(() -> YellowPagesService.search(clientAgent, SCHEDULER_SERVICE_TYPE))
					.thenReturn(emptyList());

			testedBehaviour.onStart();
			testedBehaviour.action();
		}

		// then
		verify(clientAgent).doDelete();
	}

	@Test
	void shouldAnnounceWhenCNAgentsWereFound() {
		// given
		testedBehaviour.setAgent(clientAgent);
		when(clientAgent.getGuiController()).thenReturn(guiController);

		// when
		try (var yellowPagesService = mockStatic(YellowPagesService.class)) {
			yellowPagesService.when(() -> YellowPagesService.search(clientAgent, SCHEDULER_SERVICE_TYPE))
					.thenReturn(List.of(new AID("test", true)));

			testedBehaviour.onStart();
			testedBehaviour.action();
		}

		// then
		verify(clientAgent).announce();
	}
}
