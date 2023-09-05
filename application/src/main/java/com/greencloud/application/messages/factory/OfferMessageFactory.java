package com.greencloud.application.messages.factory;

import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static jade.lang.acl.ACLMessage.PROPOSE;
import static java.util.Objects.isNull;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.agent.GreenSourceData;
import com.greencloud.application.domain.agent.ImmutableGreenSourceData;
import com.greencloud.application.domain.agent.ImmutableServerData;
import com.greencloud.application.domain.agent.ServerData;
import com.greencloud.application.exception.JobNotFoundException;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.message.MessageBuilder;

import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating the messages related with offers
 */
public class OfferMessageFactory {

	/**
	 * Method used in making the proposal message containing the offer made by the Server Agent
	 *
	 * @param serverAgent  server which is making the job offer proposal
	 * @param servicePrice cost of executing the given job
	 * @param jobId        unique identifier of the job of interest
	 * @param cnaMessage   reply message as which the job offer is to be sent
	 * @return PROPOSE ACLMessage
	 */
	public static ACLMessage prepareServerJobOffer(final ServerAgent serverAgent, final double servicePrice,
			final String jobId, final ACLMessage cnaMessage) {
		final ClientJob job = getJobById(jobId, serverAgent.getServerJobs());

		if (isNull(job)) {
			throw new JobNotFoundException();
		}
		final double powerConsumption = serverAgent.resources().getPowerConsumption(job.getStartTime(), job.getEndTime());
		final ServerData jobOffer = new ImmutableServerData(servicePrice, powerConsumption, jobId);

		return MessageBuilder.builder()
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
