package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.PROPOSE;
import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.GreenSourceData;
import org.greencloud.commons.domain.agent.ImmutableGreenSourceData;
import org.greencloud.commons.domain.agent.ImmutableServerData;
import org.greencloud.commons.domain.agent.ImmutableServerResources;
import org.greencloud.commons.domain.agent.ServerData;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.utils.job.JobUtils;
import org.greencloud.commons.utils.messaging.MessageBuilder;

import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating the messages related with offers
 */
public class OfferMessageFactory {

	/**
	 * Method used in making the proposal message containing the offer made by the Server Agent
	 *
	 * @param agentProps   properties of Server Agent
	 * @param servicePrice cost of executing the given job
	 * @param jobId        unique identifier of the job of interest
	 * @param cnaMessage   reply message as which the job offer is to be sent
	 * @return PROPOSE ACLMessage
	 */
	public static ACLMessage prepareServerJobOffer(final ServerAgentProps agentProps, final double servicePrice,
			final String jobId, final ACLMessage cnaMessage, final Integer ruleSet) {
		final ClientJob job = requireNonNull(JobUtils.getJobById(jobId, agentProps.getServerJobs()));
		final double powerConsumption = agentProps.getPowerConsumption(job.getStartTime(), job.getEndTime());
		final ServerResources serverResources = ImmutableServerResources.builder().resources(agentProps.resources())
				.price(agentProps.getPricePerHour()).build();
		final ServerData jobOffer = new ImmutableServerData(servicePrice, powerConsumption, jobId, serverResources);

		return MessageBuilder.builder(ruleSet)
				.copy(cnaMessage.createReply())
				.withPerformative(PROPOSE)
				.withObjectContent(jobOffer)
				.build();
	}

	/**
	 * Method used in making the proposal message containing the offer made by the Green Energy Agent
	 *
	 * @param costForJob            cost for execution of given job
	 * @param averageAvailablePower power available during job execution
	 * @param predictionError       error associated with power calculations
	 * @param jobId                 unique identifier of the job of interest
	 * @param message               reply message as which the power supply offer is to be sent
	 * @return PROPOSE ACLMessage
	 */
	public static ACLMessage prepareGreenEnergyPowerSupplyOffer(final double costForJob,
			final double averageAvailablePower, final double predictionError, final String jobId,
			final ACLMessage message) {
		final GreenSourceData responseData = ImmutableGreenSourceData.builder()
				.priceForEnergySupply(costForJob)
				.availablePowerInTime(averageAvailablePower)
				.powerPredictionError(predictionError)
				.jobId(jobId)
				.build();
		return prepareReply(message, responseData, PROPOSE);
	}
}
