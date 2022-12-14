package com.greencloud.application.agents.server;

import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.greenenergy.AbstractGreenEnergyAgent;
import com.greencloud.application.agents.server.management.ServerConfigManagement;
import com.greencloud.application.domain.GreenSourceData;
import com.greencloud.application.domain.ImmutableGreenSourceData;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class AbstractServerAgentUnitTest {

	@Mock
	private ServerAgent abstractServerAgent;
	@Mock
	private ServerConfigManagement configManagement;
	@Mock
	private AID greenSource1;
	@Mock
	private AID greenSource2;

	@BeforeEach
	void init() {
		abstractServerAgent = spy(ServerAgent.class);
		configManagement = mock(ServerConfigManagement.class);

		greenSource1 = mock(AID.class);
		greenSource2 = mock(AID.class);

		var percentageMap = Map.of(greenSource1, 1, greenSource2, 1);

		doReturn(percentageMap).when(configManagement).getWeightsForGreenSourcesMap();
		doReturn(configManagement).when(abstractServerAgent).manageConfig();
		doCallRealMethod().when(abstractServerAgent).chooseGreenSourceToExecuteJob(anyList());
	}

	@Test
	@DisplayName("Test choosing green source for job execution error difference")
	void testChooseGreenSourceToExecuteJobErrorDifference() throws JsonProcessingException {
		final GreenSourceData data1 = ImmutableGreenSourceData.builder()
				.availablePowerInTime(10)
				.pricePerPowerUnit(10)
				.powerPredictionError(0.02)
				.jobId("1")
				.build();
		final GreenSourceData data2 = ImmutableGreenSourceData.builder()
				.availablePowerInTime(10)
				.pricePerPowerUnit(10)
				.powerPredictionError(0.04)
				.jobId("2")
				.build();

		final ACLMessage offer1 = new ACLMessage(ACLMessage.PROPOSE);
		final ACLMessage offer2 = new ACLMessage(ACLMessage.PROPOSE);

		offer1.setContent(getMapper().writeValueAsString(data1));
		offer1.setSender(greenSource1);
		offer2.setContent(getMapper().writeValueAsString(data2));
		offer2.setSender(greenSource2);

		var result = abstractServerAgent.chooseGreenSourceToExecuteJob(List.of(offer1, offer2));

		assertThat(result).isEqualTo(offer1);
	}

	@Test
	@DisplayName("Test choosing green source for job execution price difference")
	void testChooseGreenSourceToExecuteJobPriceDifference() throws JsonProcessingException {
		final GreenSourceData data1 = ImmutableGreenSourceData.builder()
				.availablePowerInTime(10)
				.pricePerPowerUnit(20)
				.powerPredictionError(0.02)
				.jobId("1")
				.build();
		final GreenSourceData data2 = ImmutableGreenSourceData.builder()
				.availablePowerInTime(10)
				.pricePerPowerUnit(10)
				.powerPredictionError(0.02)
				.jobId("2")
				.build();

		final ACLMessage offer1 = new ACLMessage(ACLMessage.PROPOSE);
		final ACLMessage offer2 = new ACLMessage(ACLMessage.PROPOSE);

		offer1.setContent(getMapper().writeValueAsString(data1));
		offer1.setSender(greenSource1);
		offer2.setContent(getMapper().writeValueAsString(data2));
		offer2.setSender(greenSource2);

		var result = abstractServerAgent.chooseGreenSourceToExecuteJob(List.of(offer1, offer2));

		assertThat(result).isEqualTo(offer2);
	}

	@Test
	@DisplayName("Test choosing green source for job execution power difference")
	void testChooseGreenSourceToExecuteJobPowerDifference() throws JsonProcessingException {
		final GreenSourceData data1 = ImmutableGreenSourceData.builder()
				.availablePowerInTime(30)
				.pricePerPowerUnit(10)
				.powerPredictionError(0.02)
				.jobId("1")
				.build();
		final GreenSourceData data2 = ImmutableGreenSourceData.builder()
				.availablePowerInTime(10)
				.pricePerPowerUnit(10)
				.powerPredictionError(0.02)
				.jobId("2")
				.build();

		final ACLMessage offer1 = new ACLMessage(ACLMessage.PROPOSE);
		final ACLMessage offer2 = new ACLMessage(ACLMessage.PROPOSE);

		offer1.setContent(getMapper().writeValueAsString(data1));
		offer1.setSender(greenSource1);
		offer2.setContent(getMapper().writeValueAsString(data2));
		offer2.setSender(greenSource2);

		var result = abstractServerAgent.chooseGreenSourceToExecuteJob(List.of(offer1, offer2));

		assertThat(result).isEqualTo(offer2);
	}
}
