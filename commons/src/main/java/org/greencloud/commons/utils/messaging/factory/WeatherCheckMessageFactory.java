package org.greencloud.commons.utils.messaging.factory;

import static java.lang.Integer.valueOf;
import static org.greencloud.commons.utils.job.JobUtils.getTimetableOfJobs;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.util.Objects.nonNull;

import org.greencloud.commons.domain.agent.ImmutableGreenSourceForecastData;
import org.greencloud.commons.domain.agent.ImmutableGreenSourceWeatherData;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.jrba.utils.messages.MessageBuilder;

import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating messages for weather checking
 */
public class WeatherCheckMessageFactory {

	/**
	 * Method prepares the request message sent to monitoring agent to retrieve weather data
	 *
	 * @param props          properties of green energy agent
	 * @param conversationId identifier of the conversation
	 * @param protocol       message protocol
	 * @param job            (optional) server job
	 * @return REQUEST ACLMessage
	 */
	public static ACLMessage prepareWeatherCheckRequest(final GreenEnergyAgentProps props, final ServerJob job,
			final String conversationId, final String protocol, final Integer ruleSet) {
		return MessageBuilder.builder(ruleSet, REQUEST)
				.withObjectContent(createMessageContent(job, props))
				.withReceivers(props.getMonitoringAgent())
				.withMessageProtocol(protocol)
				.withConversationId(conversationId)
				.build();
	}

	/**
	 * Method prepares the response to the given weather request sent by the Server Agent
	 *
	 * @param monitoringData monitoring data
	 * @param request        original server request
	 * @return REQUEST ACLMessage
	 */
	public static ACLMessage prepareWeatherDataResponse(final MonitoringData monitoringData, final ACLMessage request) {
		final ACLMessage response = request.createReply();
		return MessageBuilder.builder(valueOf(response.getOntology()))
				.copy(response)
				.withConversationId(request.getConversationId())
				.withPerformative(INFORM)
				.withObjectContent(monitoringData, error -> {
					error.printStackTrace();
					response.setPerformative(REFUSE);
				})
				.build();
	}

	private static Object createMessageContent(final ServerJob job, final GreenEnergyAgentProps props) {
		final boolean requestForJobInProcessing = nonNull(job);
		return requestForJobInProcessing ?
				new ImmutableGreenSourceForecastData(props.getLocation(),
						getTimetableOfJobs(job, props.getServerJobs()), job.getJobId()) :
				new ImmutableGreenSourceWeatherData(props.getLocation(), props.getWeatherPredictionError());
	}
}
