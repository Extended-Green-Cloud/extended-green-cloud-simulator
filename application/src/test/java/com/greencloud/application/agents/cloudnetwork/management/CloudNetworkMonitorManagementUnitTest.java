package com.greencloud.application.agents.cloudnetwork.management;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import jade.core.AID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CloudNetworkMonitorManagementUnitTest {

    // MOCK OBJECTS
    @Mock
    private Map<AID, Integer> MOCK_WEIGHTS_FOR_SERVERS_MAP;

    @Mock
    private CloudNetworkAgent mockCloudNetworkAgent;

    private CloudNetworkMonitorManagement cloudNetworkMonitorManagement;

    @BeforeEach
    void init() {
        mockCloudNetworkAgent = mock(CloudNetworkAgent.class);
        MOCK_WEIGHTS_FOR_SERVERS_MAP = initMap();
        doReturn(MOCK_WEIGHTS_FOR_SERVERS_MAP).when(mockCloudNetworkAgent).getWeightsForServersMap();

        cloudNetworkMonitorManagement = new CloudNetworkMonitorManagement(mockCloudNetworkAgent);
        doReturn(cloudNetworkMonitorManagement).when(mockCloudNetworkAgent).manageMonitoring();
    }
    // TESTS

    @Test
    void testGetServerPercentages() {
        Map<AID, Double> serverPercentages = mockCloudNetworkAgent.manageMonitoring().getPercentages();
        assertThat(serverPercentages.get(new AID("1", AID.ISGUID))).isEqualTo(1.0 * 100/7);
        assertThat(serverPercentages.get(new AID("2", AID.ISGUID))).isEqualTo(1.0 * 100/7);
        assertThat(serverPercentages.get(new AID("3", AID.ISGUID))).isEqualTo(3.0 * 100/7);
        assertThat(serverPercentages.get(new AID("4", AID.ISGUID))).isEqualTo(2.0 * 100/7);
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
