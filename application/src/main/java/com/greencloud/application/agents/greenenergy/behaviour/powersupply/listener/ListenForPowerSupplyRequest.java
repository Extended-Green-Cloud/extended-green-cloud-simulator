package com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener;

import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.INCORRECT_WEATHER_DATA_FORMAT_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.NOT_ENOUGH_POWER_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.NO_ENERGY_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.POWER_SUPPLY_PROPOSAL_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.TOO_BAD_WEATHER_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.WEATHER_UNAVAILABLE_FOR_JOB_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.template.PowerSupplyMessageTemplates.POWER_SUPPLY_REQUEST_TEMPLATE;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.RequestWeatherData.createWeatherRequest;
import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.MAX_NUMBER_OF_SERVER_MESSAGES;
import static com.greencloud.application.mapper.JobMapper.mapToServerJob;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareRefuseReply;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.constants.LoggingConstant.MDC_AGENT_NAME;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.PROCESSING;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator.InitiatePowerSupplyOffer;
import com.greencloud.application.domain.weather.MonitoringData;
import com.greencloud.commons.domain.job.EnergyJob;
import com.greencloud.commons.domain.job.ServerJob;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the power supply request coming from parent Server Agent
 */
public class ListenForPowerSupplyRequest extends CyclicBehaviour implements Serializable {

	private static final Logger logger = getLogger(ListenForPowerSupplyRequest.class);

	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviours constructor.
	 *
	 * @param myAgent agent which is executing the behaviour
	 */
	public ListenForPowerSupplyRequest(final Agent myAgent) {
		this.myGreenEnergyAgent = (GreenEnergyAgent) myAgent;
	}

	/**
	 * Method listens for the power CFP coming from the Server.
	 * It analyzes the received message and either rejects it or proceeds with its processing by sending
	 * request to Monitoring Agent for the weather data.
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = myAgent.receive(POWER_SUPPLY_REQUEST_TEMPLATE, MAX_NUMBER_OF_SERVER_MESSAGES);

		if (nonNull(messages)) {
			messages.stream().parallel().forEach(message -> {
				MDC.put(MDC_AGENT_NAME, myAgent.getLocalName());
				final EnergyJob job = readMessageContent(message, EnergyJob.class);

				if (nonNull(job)) {
					if (myGreenEnergyAgent.isHasError()) {
						MDC.put(MDC_JOB_ID, job.getJobId());
						logger.info(NO_ENERGY_LOG, job.getJobId());
						myGreenEnergyAgent.send(prepareRefuseReply(message));
						return;
					}
					final ServerJob serverJob = mapToServerJob(job, message.getSender());
					final String protocol = message.getProtocol();
					final String conversationId = message.getConversationId();

					myGreenEnergyAgent.getServerJobs().put(serverJob, PROCESSING);
					myAgent.addBehaviour(createWeatherRequest(myGreenEnergyAgent, protocol, conversationId,
							getResponseHandler(serverJob, message), getRefuseResponseHandler(serverJob, message),
							serverJob));
				}
			});
		} else {
			block();
		}
	}

	private BiConsumer<MonitoringData, Exception> getResponseHandler(final ServerJob job, final ACLMessage cfp) {
		return (data, error) -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			if (nonNull(error)) {
				logger.info(INCORRECT_WEATHER_DATA_FORMAT_LOG);
				handleRefusal(job, cfp);
				return;
			}

			final Optional<Double> availablePower = myGreenEnergyAgent.power().getAvailableEnergy(job, data, true);
			final String jobId = job.getJobId();
			final double energyForJob = job.getEstimatedEnergy();

			if (availablePower.isEmpty()) {
				logger.info(TOO_BAD_WEATHER_LOG, jobId);
				handleRefusal(job, cfp);
			} else if (energyForJob > availablePower.get()) {
				logger.info(NOT_ENOUGH_POWER_LOG, jobId, energyForJob, availablePower.get());
				handleRefusal(job, cfp);
			} else {
				logger.info(POWER_SUPPLY_PROPOSAL_LOG, jobId);
				final double power = availablePower.get();
				myAgent.addBehaviour(InitiatePowerSupplyOffer.create(myGreenEnergyAgent, job, power, cfp, data));
			}
		};
	}

	private Runnable getRefuseResponseHandler(final ServerJob job, final ACLMessage cfp) {
		return () -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(WEATHER_UNAVAILABLE_FOR_JOB_LOG);
			handleRefusal(job, cfp);
		};
	}

	private void handleRefusal(final ServerJob job, final ACLMessage cfp) {
		myGreenEnergyAgent.manage().removeJob(job);
		myGreenEnergyAgent.send(prepareRefuseReply(cfp));
	}
}
