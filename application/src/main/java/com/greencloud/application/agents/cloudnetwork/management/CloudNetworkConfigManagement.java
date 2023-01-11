package com.greencloud.application.agents.cloudnetwork.management;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.agent.cloudnetwork.ImmutableCloudNetworkMonitoringData;
import com.greencloud.commons.job.PowerJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;

import jade.core.AID;

import static com.greencloud.application.agents.cloudnetwork.management.logs.CloudNetworkManagementLog.SAVED_MONITORING_DATA_LOG;
import static com.greencloud.application.utils.JobUtils.isJobStarted;

public class CloudNetworkConfigManagement {

	private static final Logger logger = LoggerFactory.getLogger(CloudNetworkConfigManagement.class);

	private final CloudNetworkAgent cloudNetworkAgent;
	private Map<AID, Integer> weightsForServersMap;

	public CloudNetworkConfigManagement(CloudNetworkAgent cloudNetworkAgent) {
		this.cloudNetworkAgent = cloudNetworkAgent;
		this.weightsForServersMap = new HashMap<>();
	}

	/**
	 * Method returns the map where key is the owned server and value is the (weight / sum of weights) * 100
	 *
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
	 *
	 * @return map where key is the owned server and value is the weight
	 */
	public Map<AID, Integer> getWeightsForServersMap() {
		return weightsForServersMap;
	}

	/**
	 * method sets the map with servers and their weights for choice
	 *
	 * @param weightsForServersMap value of the map
	 */
	public void setWeightsForServersMap(Map<AID, Integer> weightsForServersMap) {
		this.weightsForServersMap = weightsForServersMap;
	}

	/**
	 * Method assembles the object that stores monitoring data and saves it in the database
	 */
	public void saveMonitoringData() {
		var maxCapacity = cloudNetworkAgent.getMaximumCapacity();
		var traffic = getCurrentTraffic();

		CloudNetworkMonitoringData cloudNetworkMonitoringData = ImmutableCloudNetworkMonitoringData.builder()
				.currentTraffic(maxCapacity == 0 ? 0 : traffic / maxCapacity)
				.availablePower(maxCapacity - traffic)
				.successRatio(cloudNetworkAgent.manage().getSuccessRatio())
				.build();
		cloudNetworkAgent.writeMonitoringData(DataType.CLOUD_NETWORK_MONITORING, cloudNetworkMonitoringData);
		logger.info(SAVED_MONITORING_DATA_LOG, cloudNetworkAgent.getAID().getName());
	}

	private double getCurrentTraffic() {
		return cloudNetworkAgent.getNetworkJobs()
				.entrySet().stream()
				.filter(entry -> isJobStarted(entry.getValue()))
				.map(Map.Entry::getKey)
				.mapToInt(PowerJob::getPower)
				.sum();
	}
}
