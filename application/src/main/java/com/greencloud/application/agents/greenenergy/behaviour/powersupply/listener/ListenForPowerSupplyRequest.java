package com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener;

import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.template.PowerSupplyMessageTemplates.POWER_SUPPLY_REQUEST_TEMPLATE;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static java.util.Objects.nonNull;

import com.greencloud.commons.job.ExecutionJobStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.ListenForNewJobWeatherData;
import com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.RequestWeatherData;
import com.greencloud.commons.job.PowerJob;
import com.greencloud.application.messages.domain.factory.ReplyMessageFactory;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the power supply request coming from parent Server Agent
 */
public class ListenForPowerSupplyRequest extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForPowerSupplyRequest.class);

	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviours constructor.
	 *
	 * @param myAgent agent which is executing the behaviour
	 */
	public ListenForPowerSupplyRequest(Agent myAgent) {
		this.myGreenEnergyAgent = (GreenEnergyAgent) myAgent;
	}

	/**
	 * Method listens for the power CFP coming from the server.
	 * It analyzes the request and either rejects it or proceeds with request processing by sending
	 * another request to Monitoring Agent for the com.greencloud.application.weather data.
	 */
	@Override
	public void action() {
		final ACLMessage cfp = myAgent.receive(POWER_SUPPLY_REQUEST_TEMPLATE);
		if (nonNull(cfp)) {
			final PowerJob job = readJob(cfp);
			if (nonNull(job)) {
				myGreenEnergyAgent.getPowerJobs().put(job, ExecutionJobStatusEnum.PROCESSING);
				requestMonitoringData(cfp, job);
			}
		} else {
			block();
		}
	}

	private PowerJob readJob(ACLMessage callForProposal) {
		try {
			return readMessageContent(callForProposal, PowerJob.class);
		} catch (Exception e) {
			logger.info("I didn't understand the message from the server, refusing the job");
			myAgent.send(ReplyMessageFactory.prepareRefuseReply(callForProposal.createReply()));
		}
		return null;
	}

	private void requestMonitoringData(final ACLMessage cfp, final PowerJob job) {
		var sequentialBehaviour = new SequentialBehaviour();
		sequentialBehaviour.addSubBehaviour(
				new RequestWeatherData(myGreenEnergyAgent, cfp.getProtocol(), cfp.getConversationId(), job));
		sequentialBehaviour.addSubBehaviour(
				new ListenForNewJobWeatherData(myGreenEnergyAgent, cfp, job, sequentialBehaviour));
		myAgent.addBehaviour(sequentialBehaviour);
	}
}
