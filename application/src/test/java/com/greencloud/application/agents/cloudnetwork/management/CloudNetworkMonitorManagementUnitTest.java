package com.greencloud.application.agents.cloudnetwork.management;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import jade.core.AID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CloudNetworkMonitorManagementUnitTest {

    // MOCK OBJECTS
    
    private Map<AID, Integer> MOCK_WEIGHTS_FOR_SERVERS_MAP;
    
    @Mock
    private CloudNetworkAgent mockCloudNetworkAgent;
    
    private CloudNetworkMonitorManagement cloudNetworkMonitorManagement;
    
    @BeforeEach
    void init() {
        MOCK_WEIGHTS_FOR_SERVERS_MAP = initMap();
        when(mockCloudNetworkAgent.getWeightsForServersMap()).thenReturn(MOCK_WEIGHTS_FOR_SERVERS_MAP);
        cloudNetworkMonitorManagement = new CloudNetworkMonitorManagement(mockCloudNetworkAgent);
    }

    private Map<AID, Integer> initMap() {
        Map<AID, Integer> map = new HashMap<>();
        map.put(new AID("1", AID.ISLOCALNAME), 1);
        map.put(new AID("2", AID.ISLOCALNAME), 1);
        map.put(new AID("3", AID.ISLOCALNAME), 3);
        map.put(new AID("4", AID.ISLOCALNAME), 2);
        return map;
    }

    // TESTS

    @Test
    void testGetServerPercentages() {
        Map<AID, Double> serverPercentages = cloudNetworkMonitorManagement.getPercentages();
        assertThat(serverPercentages.get(new AID("1", AID.ISLOCALNAME))).isEqualTo(14);
        assertThat(serverPercentages.get(new AID("2", AID.ISLOCALNAME))).isEqualTo(14);
        assertThat(serverPercentages.get(new AID("3", AID.ISLOCALNAME))).isEqualTo(42);
        assertThat(serverPercentages.get(new AID("4", AID.ISLOCALNAME))).isEqualTo(28);
    }
}
