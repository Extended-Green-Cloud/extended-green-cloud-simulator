package com.greencloud.application.agents.server.behaviour.df;

import static com.greencloud.application.yellowpages.YellowPagesService.decodeSubscription;
import static jade.lang.acl.ACLMessage.INFORM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.management.ServerConfigManagement;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class SubscribeGreenSourceServiceUnitTest {

	private static final String mockJadeAddress = "192.168.56.1:6996/JADE";
	private static final String mockServerName = "test_server";
	private static final String mockServerFullName = mockServerName + "@" + mockJadeAddress;

	private static final String mockServiceType = "GS";
	private static final String mockServiceName = "power-supplier";

	@Mock
	private ServerAgent mockServerAgent;
	@Mock
	private ServerConfigManagement mockConfigManagement;
	@Mock
	private AID mockGreenSource;

	private SubscribeGreenSourceService subscribeGreenSourceService;

	@BeforeEach
	void init() {
		var mockDF = new AID("test_df", AID.ISGUID);

		mockGreenSource = mock(AID.class);
		doReturn("test_gs@" + mockJadeAddress).when(mockGreenSource).getName();

		mockServerAgent = spy(ServerAgent.class);
		mockConfigManagement = spy(new ServerConfigManagement(mockServerAgent));

		doReturn(mockServerName).when(mockServerAgent).getName();
		doReturn(mockDF).when(mockServerAgent).getDefaultDF();
		doReturn(mockConfigManagement).when(mockServerAgent).manageConfig();

		subscribeGreenSourceService = SubscribeGreenSourceService.create(mockServerAgent);
	}

	@Test
	@DisplayName("Test receiving new Green Source information from DF - no green sources added yet")
	void testHandleInformForNoGreenSources() {
		doReturn(new HashMap<AID, Boolean>()).when(mockServerAgent).getOwnedGreenSources();

		subscribeGreenSourceService.handleInform(prepareDFMessage());

		verify(mockServerAgent).getOwnedGreenSources();
		verify(mockServerAgent).manageConfig();

		assertThat(mockServerAgent.getOwnedGreenSources().keySet())
				.as("Set size should be equal to 1")
				.hasSize(1)
				.as("Set should contain correct green source")
				.allMatch(agent -> agent.getName().equals(mockGreenSource.getName()));
	}

	@Test
	@DisplayName("Test receiving new Green Source information from DF - green sources present")
	void testHandleInformForSomeGreenSourcesPresent() {
		var mockGS1 = mock(AID.class);
		var mockGS2 = mock(AID.class);

		doReturn("test_old_gs1@" + mockJadeAddress).when(mockGS1).getName();
		doReturn("test_old_gs2@" + mockJadeAddress).when(mockGS2).getName();

		doReturn(new HashMap<>(Map.of(mockGS1, true, mockGS2, true))).when(mockServerAgent).getOwnedGreenSources();
		mockConfigManagement.setWeightsForGreenSourcesMap(new HashMap<>(Map.of(mockGS1, 5, mockGS2, 8)));

		subscribeGreenSourceService.handleInform(prepareDFMessage());

		verify(mockServerAgent).getOwnedGreenSources();
		verify(mockServerAgent).manageConfig();

		assertThat(mockServerAgent.getOwnedGreenSources().keySet())
				.as("Set size should be equal to 3")
				.hasSize(3)
				.as("Set should contain correct green sources")
				.map(AID::getName)
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(mockGS1.getName(), mockGS2.getName(), mockGreenSource.getName());

		var greenSourceWeight = mockServerAgent.manageConfig().getWeightsForGreenSourcesMap().entrySet().stream()
				.filter(entry -> entry.getKey().getName().equals(mockGreenSource.getName()))
				.findFirst().orElseThrow().getValue();

		assertThat(greenSourceWeight)
				.as("Weight of the new green source should be equal to 8")
				.isEqualTo(8);
	}

	@Test
	@DisplayName("Test receiving Green Source information from DF - green sources duplicate")
	void testHandleInformForGreenSourceDuplicate() {
		var mockGS1 = mock(AID.class);
		doReturn("test_old_gs1@" + mockJadeAddress).when(mockGS1).getName();
		var mockDuplicatedGS = decodeSubscription(prepareDFMessage());

		doReturn(new HashMap<>(Map.of(mockGS1, true, mockDuplicatedGS.get(0), true))).when(mockServerAgent)
				.getOwnedGreenSources();
		doReturn(new HashMap<>(Map.of(mockGS1, 5, mockGreenSource, 8))).when(mockConfigManagement)
				.getWeightsForGreenSourcesMap();

		subscribeGreenSourceService.handleInform(prepareDFMessage());

		verify(mockServerAgent).getOwnedGreenSources();
		verify(mockServerAgent).manageConfig();

		assertThat(mockServerAgent.getOwnedGreenSources().keySet())
				.as("Set size should be equal to 2")
				.hasSize(2)
				.as("Set should contain correct green sources")
				.map(AID::getName)
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(mockGS1.getName(), mockGreenSource.getName());
	}

	private ACLMessage prepareDFMessage() {
		final ACLMessage aclMessage = new ACLMessage(INFORM);
		aclMessage.setContent(prepareDFMessageContent());
		return aclMessage;
	}

	private String prepareDFMessageContent() {
		var mockAddress = ":addresses (sequence http://Test-Address/acc)) ";

		var iotaPrefix = "((= (iota ?x (result (action ";
		var mockDFDescription = "(agent-identifier " + ":name df@" + mockJadeAddress + " " + mockAddress;
		var mockSearch = "(search "
				+ "(df-agent-description :services "
				+ "(set (service-description :type " + mockServiceType + " :ownership " + mockServerFullName + "))) "
				+ "(search-constraints :max-results -1))) ?x)) ";

		var mockResult = "(sequence "
				+ "(df-agent-description "
				+ ":name (agent-identifier :name " + mockGreenSource.getName() + " " + mockAddress
				+ ":services (set (service-description "
				+ ":name " + mockServiceName + " "
				+ ":type " + mockServiceType + " "
				+ ":ownership " + mockServerFullName
				+ "))))))";

		return String.join("", iotaPrefix, mockDFDescription, mockSearch, mockResult);
	}
}
