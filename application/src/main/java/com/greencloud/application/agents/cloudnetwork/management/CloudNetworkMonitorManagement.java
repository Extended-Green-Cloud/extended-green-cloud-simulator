package com.greencloud.application.agents.cloudnetwork.management;

import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.domain.monitoring.CloudNetworkMonitoringData;
import com.greencloud.application.domain.monitoring.ImmutableCloudNetworkMonitoringData;
import jade.core.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CloudNetworkMonitorManagement {

    private static final Logger logger = LoggerFactory.getLogger(CloudNetworkStateManagement.class);
    private final CloudNetworkAgent cloudNetworkAgent;
    private final TimescaleDatabase timescaleDatabase;

    public CloudNetworkMonitorManagement(CloudNetworkAgent cloudNetworkAgent) {
        this.cloudNetworkAgent = cloudNetworkAgent;
        this.timescaleDatabase = new TimescaleDatabase();
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
            percentages.put(entry.getKey(), ((double)entry.getValue() * 100)/ sum);
        }
        return percentages;
    }

    /**
     * Method assembles the CNA monitoring data and saves it in the database
     */
    public void saveMonitoringData(){
        logger.info("Saving monitoring data for Cloud Network Agent: {}", cloudNetworkAgent.getAID().getName());
        CloudNetworkMonitoringData cloudNetworkMonitoringData = ImmutableCloudNetworkMonitoringData.builder()
                .ownedServers(cloudNetworkAgent.getOwnedServers())
                .percentagesForServersMap(getPercentages())
                .networkJobs(cloudNetworkAgent.getNetworkJobs())
                .build();
        timescaleDatabase.writeMonitoringData(cloudNetworkAgent.getName(), DataType.DEFAULT, cloudNetworkMonitoringData);
    }
}
