package com.greencloud.application.agents.server.management;

import com.greencloud.application.agents.server.ServerAgent;
import jade.core.AID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ServerConfigManagementTest {

    //MOCK OBJECTS
    @Mock
    private Map<AID, Integer> MOCK_WEIGHTS_FOR_GREEN_SOURCES_MAP;
    @Mock
    private ServerAgent mockServerAgent;
    private ServerConfigManagement serverConfigManagement;

    @BeforeEach
    void init() {
        mockServerAgent = mock(ServerAgent.class);
        MOCK_WEIGHTS_FOR_GREEN_SOURCES_MAP = initMap();

        serverConfigManagement = new ServerConfigManagement(mockServerAgent);
        serverConfigManagement.setWeightsForGreenSourcesMap(MOCK_WEIGHTS_FOR_GREEN_SOURCES_MAP);
        doReturn(serverConfigManagement).when(mockServerAgent).manageConfig();
    }

    //TESTS
    @Test
    public void testGetGreenSourcePercentages() {
        Map<AID, Double> greenSourcePercentages = mockServerAgent.manageConfig().getPercentages();
        assertThat(greenSourcePercentages.get(new AID("1", AID.ISGUID))).isEqualTo(1.0 * 100/7);
        assertThat(greenSourcePercentages.get(new AID("2", AID.ISGUID))).isEqualTo(1.0 * 100/7);
        assertThat(greenSourcePercentages.get(new AID("3", AID.ISGUID))).isEqualTo(3.0 * 100/7);
        assertThat(greenSourcePercentages.get(new AID("4", AID.ISGUID))).isEqualTo(2.0 * 100/7);
    }

    private Map<AID, Integer> initMap() {
        Map<AID, Integer> map = new HashMap<>();
        map.put(new AID("1", AID.ISGUID), 1);
        map.put(new AID("2", AID.ISGUID), 1);
        map.put(new AID("3", AID.ISGUID), 3);
        map.put(new AID("4", AID.ISGUID), 2);
        return map;
    }
}
