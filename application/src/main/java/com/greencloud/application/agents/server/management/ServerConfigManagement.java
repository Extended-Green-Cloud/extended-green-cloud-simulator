package com.greencloud.application.agents.server.management;

import static com.greencloud.application.utils.JobUtils.getJobById;
import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.GreenSourceData;
import com.greencloud.application.utils.TimeUtils;

import jade.core.AID;

/**
 * Class containing current server configuration
 */
public class ServerConfigManagement implements Serializable {

	private final ServerAgent serverAgent;

	protected Map<AID, Integer> weightsForGreenSourcesMap;
	protected double pricePerHour;
	protected int jobProcessingLimit;

	public ServerConfigManagement(ServerAgent serverAgent) {
		this.serverAgent = serverAgent;
		this.weightsForGreenSourcesMap = new HashMap<>();
	}

	/**
	 * Method returns the map where key is the owned green source and value is the (weight / sum of weights) * 100
	 *
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
				.collect(toMap(Map.Entry::getKey, entry -> ((double) entry.getValue() * 100) / sum));
	}

	/**
	 * Method returns the map where key is the owned green source and value is the weight
	 *
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

	/**
	 * Method calculates the price for executing the job by given green source and server
	 *
	 * @param greenSourceData green source executing the job
	 * @return full price
	 */
	public double calculateServicePrice(final GreenSourceData greenSourceData) {
		var job = getJobById(greenSourceData.getJobId(), serverAgent.getServerJobs());
		var powerCost = job.getPower() * greenSourceData.getPricePerPowerUnit();
		var computingCost =
				TimeUtils.differenceInHours(job.getStartTime(), job.getEndTime()) * serverAgent.manageConfig()
						.getPricePerHour();
		return powerCost + computingCost;
	}

	/**
	 * Method connects new green sources to the server agent
	 *
	 * @param newGreenSources list of green sources to connect to the server
	 */
	public void connectNewGreenSourcesToServer(final List<AID> newGreenSources) {
		Map<AID, Boolean> greenSourceWithState = newGreenSources.stream().collect(toMap(gs -> gs, gs -> true));
		serverAgent.getOwnedGreenSources().putAll(greenSourceWithState);
		assignWeightsToNewGreenSources(newGreenSources);
	}

	/**
	 * Method retrieves the current price per hour for the job execution service
	 *
	 * @return double price
	 */
	public double getPricePerHour() {
		return pricePerHour;
	}

	/**
	 * Method sets new price per hour for the job execution service
	 *
	 * @param pricePerHour new price
	 */
	public void setPricePerHour(double pricePerHour) {
		this.pricePerHour = pricePerHour;
	}

	/**
	 * Method retrieves current limit of jobs that can be processed at the same time
	 *
	 * @return integer job processing limit
	 */
	public int getJobProcessingLimit() {
		return jobProcessingLimit;
	}

	/**
	 * Method sets current limit of jobs that can be processed at the same time
	 *
	 * @param jobProcessingLimit new limit
	 */
	public void setJobProcessingLimit(int jobProcessingLimit) {
		this.jobProcessingLimit = jobProcessingLimit;
	}

	private void assignWeightsToNewGreenSources(final List<AID> newGreenSources) {
		final int weight = weightsForGreenSourcesMap.isEmpty() ? 1 : getMaximumValueForConfiguration();
		newGreenSources.forEach(greenSource -> weightsForGreenSourcesMap.putIfAbsent(greenSource, weight));
	}

	private int getMaximumValueForConfiguration() {
		return weightsForGreenSourcesMap.values().stream()
				.max(Integer::compare)
				.orElseThrow();
	}

}
