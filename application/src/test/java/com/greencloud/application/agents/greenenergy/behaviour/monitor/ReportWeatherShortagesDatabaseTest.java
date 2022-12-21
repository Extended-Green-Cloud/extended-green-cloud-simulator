package com.greencloud.application.agents.greenenergy.behaviour.monitor;

import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.quality.Strictness.LENIENT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyStateManagement;
import com.gui.agents.GreenEnergyAgentNode;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ReportWeatherShortagesDatabaseTest {

	@Mock
	private GreenEnergyAgent greenEnergyAgent;
	@Mock
	private GreenEnergyAgentNode greenEnergyAgentNode;
	@Mock
	private GreenEnergyStateManagement management;

	private ReportWeatherShortages reportWeatherShortages;

	private TimescaleDatabase database;

	@BeforeEach
	void init() {
		database = new TimescaleDatabase();
		database.initDatabase();

		var mockAID = mock(AID.class);
		doReturn("test_aid").when(mockAID).getName();

		greenEnergyAgentNode = spy(GreenEnergyAgentNode.class);
		greenEnergyAgent = spy(GreenEnergyAgent.class);
		greenEnergyAgent.setAgentNode(greenEnergyAgentNode);

		doReturn(mockAID).when(greenEnergyAgent).getAID();
		doReturn(database).when(greenEnergyAgentNode).getDatabaseClient();

		management = spy(new GreenEnergyStateManagement(greenEnergyAgent));
		doReturn(management).when(greenEnergyAgent).manage();

		reportWeatherShortages = new ReportWeatherShortages(greenEnergyAgent);
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	@DisplayName("Test on tick when power shortage count is not empty")
	void testTickPowerShortageNonEmpty() {
		management.getWeatherShortagesCounter().set(10);
		management.getShortagesAccumulator().set(10);
		clearInvocations(management);

		reportWeatherShortages.onTick();

		verify(greenEnergyAgent, times(4)).manage();
		verify(management, times(2)).getWeatherShortagesCounter();
		verify(management, times(2)).getShortagesAccumulator();

		var result = database.readLastMonitoringDataForDataTypes(singletonList(WEATHER_SHORTAGES), 10);

		assertThat(management.getWeatherShortagesCounter().get()).isZero();
		assertThat(result).as("Data has one record").hasSize(1);
		assertThat(result.get(0))
				.as("Data has correct content")
				.matches(data -> data.aid().contains("test_aid")
						&& ((WeatherShortages) data.monitoringData()).weatherShortagesNumber() == 10);
	}

	@Test
	@DisplayName("Test on tick when power shortage count is empty")
	void testTickPowerShortageEmpty() {
		management.getWeatherShortagesCounter().set(0);
		management.getShortagesAccumulator().set(0);
		clearInvocations(management);

		reportWeatherShortages.onTick();

		verify(greenEnergyAgent, times(2)).manage();
		verify(management).getWeatherShortagesCounter();
		verifyNoInteractions(greenEnergyAgentNode);

		assertThat(management.getWeatherShortagesCounter().get()).isZero();

	}

}
