package com.greencloud.application.agents.cloudnetwork.management;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import jade.core.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CloudNetworkMonitorManagement {

    private static final Logger logger = LoggerFactory.getLogger(CloudNetworkStateManagement.class);

    private final CloudNetworkAgent cloudNetworkAgent;

    public CloudNetworkMonitorManagement(CloudNetworkAgent cloudNetworkAgent) {
        this.cloudNetworkAgent = cloudNetworkAgent;
    }

    public Map<AID, Integer> getPercentages() {
        int sum = cloudNetworkAgent
                .getWeightsForServersMap()
                .values()
                .stream()
                .mapToInt(i -> i)
                .sum();
        Map<AID, Integer> percentages = new HashMap<>();
        for (Map.Entry<AID, Integer> entry : cloudNetworkAgent.getWeightsForServersMap().entrySet()) {
            percentages.put(entry.getKey(), (entry.getValue() / sum) * 100);
        }
        return percentages;
    }
}
