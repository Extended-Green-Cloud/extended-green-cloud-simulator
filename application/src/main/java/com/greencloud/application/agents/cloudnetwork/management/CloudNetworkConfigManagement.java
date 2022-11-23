package com.greencloud.application.agents.cloudnetwork.management;

import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.cloudnetwork.ImmutableCloudNetworkMonitoringData;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import jade.core.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.greencloud.application.agents.cloudnetwork.management.logs.CloudNetworkManagementLog.SAVED_MONITORING_DATA_LOG;

public class CloudNetworkConfigManagement {

    private static final Logger logger = LoggerFactory.getLogger(CloudNetworkStateManagement.class);

    private final CloudNetworkAgent cloudNetworkAgent;
    private Map<AID, Integer> weightsForServersMap;

    public CloudNetworkConfigManagement(CloudNetworkAgent cloudNetworkAgent) {
        this.cloudNetworkAgent = cloudNetworkAgent;
        this.weightsForServersMap = new HashMap<>();
    }

    /**
     * Method returns the map where key is the owned server and value is the (weight / sum of weights) * 100
     * @return map where key is the owned server and value is the (weight / sum of weights) * 100
     */
    public Map<AID, Double> getPercentages() {
        int sum = getWeightsForServersMap()
                .values()
                .stream()
                .mapToInt(i -> i)
                .sum();
        return weightsForServersMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> ((double) entry.getValue() * 100) / sum));
    }

    /**
     * Method returns the map where key is the owned server and value is the weight
     * @return map where key is the owned server and value is the weight
     */
    public Map<AID, Integer> getWeightsForServersMap() {
        return weightsForServersMap;
    }

    /**
     * method sets the map with servers and their weights for choice
     * @param weightsForServersMap value of the map
     */
    public void setWeightsForServersMap(Map<AID, Integer> weightsForServersMap) {
        this.weightsForServersMap = weightsForServersMap;
    }

    public void saveMonitoringData(){
        CloudNetworkMonitoringData cloudNetworkMonitoringData = ImmutableCloudNetworkMonitoringData.builder()
                .ownedServers(cloudNetworkAgent.getOwnedServers())
                .percentagesForServersMap(getPercentages())
                .jobResultStatistics(cloudNetworkAgent.manage().getJobCounters())
                .networkJobs(cloudNetworkAgent.getNetworkJobs())
                .build();
        cloudNetworkAgent.writeMonitoringData(DataType.DEFAULT, cloudNetworkMonitoringData);
        logger.info(SAVED_MONITORING_DATA_LOG, cloudNetworkAgent.getAID().getName());
    }
}
