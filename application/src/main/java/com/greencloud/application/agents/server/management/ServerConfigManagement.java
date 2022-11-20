package com.greencloud.application.agents.server.management;

import com.greencloud.application.agents.server.ServerAgent;
import jade.core.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerConfigManagement {

    private static final Logger logger = LoggerFactory.getLogger(ServerStateManagement.class);
    private final ServerAgent serverAgent;

    protected Map<AID, Integer> weightsForGreenSourcesMap;
    public ServerConfigManagement(ServerAgent serverAgent) {
        this.serverAgent = serverAgent;
        this.weightsForGreenSourcesMap = new HashMap<>();
    }

    /**
     * Method returns the map where key is the owned green source and value is the (weight / sum of weights) * 100
     * @return map where key is the owned green source and value is the (weight / sum of weights) * 100
     */
    public Map<AID, Double> getPercentages() {
        int sum = getWeightsForGreenSourcesMap()
                .values()
                .stream()
                .mapToInt(i -> i)
                .sum();
        return weightsForGreenSourcesMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> ((double) entry.getValue() * 100) / sum));
    }

    /**
     * Method returns the map where key is the owned green source and value is the weight
     * @return map where key is the owned green source and value is the weight
     */
    public Map<AID, Integer> getWeightsForGreenSourcesMap() {
        return weightsForGreenSourcesMap;
    }

    /**
     * Method sets the map where key is the owned green source and value is the weight
     */
    public void setWeightsForGreenSourcesMap(Map<AID, Integer> weightsForGreenSourcesMap) {
        this.weightsForGreenSourcesMap = weightsForGreenSourcesMap;
    }
}
