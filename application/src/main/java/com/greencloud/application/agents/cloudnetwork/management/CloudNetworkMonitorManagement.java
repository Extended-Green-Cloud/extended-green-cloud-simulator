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

    /**
     * Method returns the map where key is the owned server and value is the (weight / sum of weights) * 100
     * @return map where key is the owned server and value is the (weight / sum of weights) * 100
     */
    public Map<AID, Double> getPercentages() {
        int sum = cloudNetworkAgent
                .getWeightsForServersMap()
                .values()
                .stream()
                .mapToInt(i -> i)
                .sum();
        Map<AID, Double> percentages = new HashMap<>();
        for (Map.Entry<AID, Integer> entry : cloudNetworkAgent.getWeightsForServersMap().entrySet()) {
            percentages.put(entry.getKey(), (double) ((entry.getValue() * 100) / sum));
        }
        return percentages;
    }
}
