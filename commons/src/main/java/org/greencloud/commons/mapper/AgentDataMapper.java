package org.greencloud.commons.mapper;

import java.time.Instant;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.domain.agent.ImmutableServerData;
import org.greencloud.commons.domain.agent.ServerData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.energy.EnergyTypeEnum;

/**
 * Class provides set of methods mapping agent data object classes
 */
public class AgentDataMapper {

	/**
	 * @param job                 job allocated by RMA to the server
	 * @param executionEstimation estimated job execution
	 * @param resourceMap         map of server resources
	 * @param powerConsumption    power consumption associated with job execution
	 * @param servicePrice        price of the job execution
	 * @param typeOfEnergy        type of energy on which the job is supposed to be executed
	 * @return ServerData
	 */
	public static ServerData mapToServerData(final ClientJob job,
			final Pair<Instant, Double> executionEstimation,
			final Map<String, Resource> resourceMap,
			final double powerConsumption,
			final double servicePrice,
			final EnergyTypeEnum typeOfEnergy) {
		final Instant startTime = executionEstimation.getKey();
		final Double executionTime = executionEstimation.getValue();

		return ImmutableServerData.builder()
				.jobId(job.getJobId())
				.executionTime(executionTime)
				.estimatedEarliestJobStartTime(startTime)
				.priceForJob(servicePrice)
				.powerConsumption(powerConsumption)
				.availableResources(resourceMap)
				.typeOfEnergy(typeOfEnergy)
				.build();
	}
}
