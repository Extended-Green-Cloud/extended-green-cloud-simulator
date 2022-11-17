package com.greencloud.application.agents.server.management;

import com.greencloud.application.agents.server.ServerAgent;
import jade.core.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ServerMonitorManagement {

    private static final Logger logger = LoggerFactory.getLogger(ServerStateManagement.class);
    private final ServerAgent serverAgent;
    public ServerMonitorManagement(ServerAgent serverAgent) {
        this.serverAgent = serverAgent;
    }

    /**
     * Method returns the map where key is the owned green source and value is the (weight / sum of weights) * 100
     * @return map where key is the owned green source and value is the (weight / sum of weights) * 100
     */
    public Map<AID, Integer> getPercentages() {
        int sum = serverAgent
                .getWeightsForGreenSourcesMap()
                .values()
                .stream()
                .mapToInt(i -> i)
                .sum();
        Map<AID, Integer> percentages = new HashMap<>();
        for (Map.Entry<AID, Integer> entry : serverAgent.getWeightsForGreenSourcesMap().entrySet()) {
            percentages.put(entry.getKey(), (entry.getValue() * 100) / sum);
        }
        return percentages;
    }
}
