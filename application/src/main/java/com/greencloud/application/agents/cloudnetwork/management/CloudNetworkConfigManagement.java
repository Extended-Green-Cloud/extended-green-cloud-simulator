package com.greencloud.application.agents.cloudnetwork.management;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;

import jade.core.AID;

public class CloudNetworkConfigManagement {

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

	public void setWeightsForServersMap(Map<AID, Integer> weightsForServersMap) {
		this.weightsForServersMap = weightsForServersMap;
	}
}
