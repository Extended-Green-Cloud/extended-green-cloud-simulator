package org.greencloud.gui.event;

import static java.util.Objects.isNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.TimeConstants.SECONDS_PER_HOUR;
import static org.greencloud.commons.enums.event.EventTypeEnum.WEATHER_DROP_EVENT;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.utils.mapper.JsonMapper.getMapper;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.gui.messages.WeatherDropMessage;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.environment.domain.ExternalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

/**
 * Event simulating long-term power shortage which affects given selected region of the system
 */
@Getter
@SuppressWarnings("rawtypes")
public class WeatherDropEvent extends ExternalEvent {

	private static final Logger logger = LoggerFactory.getLogger(WeatherDropEvent.class);

	final long duration;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 * @param duration       duration of weather drop
	 * @param agentName      name of the agent for which event was triggered
	 */
	public WeatherDropEvent(final Instant occurrenceTime, final long duration, final String agentName) {
		super(agentName, WEATHER_DROP_EVENT, occurrenceTime);
		this.duration = duration;
	}

	public WeatherDropEvent(WeatherDropMessage weatherDropMessage) {
		this(getCurrentTime().plusSeconds(SECONDS_PER_HOUR), weatherDropMessage.getData().getDuration(),
				weatherDropMessage.getAgentName());
	}

	/**
	 * Method creates event from the given message
	 *
	 * @param message received message
	 * @return WeatherDropEvent
	 */
	public static WeatherDropEvent create(String message) {
		final WeatherDropMessage weatherDropMessage = readWeatherDropMessage(message);
		return new WeatherDropEvent(weatherDropMessage);
	}

	private static WeatherDropMessage readWeatherDropMessage(String message) {
		try {
			return getMapper().readValue(message, WeatherDropMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public <T extends AgentNode> void trigger(final Map<String, T> agentNodes) {
		final RMANode agentNode = (RMANode) agentNodes.get(agentName);
		final CMANode cmaNode = (CMANode) agentNodes.values().stream()
				.filter(node -> node.getAgentType().equals(CENTRAL_MANAGER.name()))
				.findFirst().orElseThrow();

		if (isNull(agentNode)) {
			logger.error("Agent {} was not found. Weather drop couldn't be triggered", agentName);
			return;
		}

		final List<EGCSNode> greenEnergyNodes = agentNode.getNodeArgs().getServerAgents().stream()
				.map(agentNodes::get)
				.map(ServerNode.class::cast)
				.map(server -> server.getNodeArgs().getGreenEnergyAgents())
				.flatMap(Collection::stream)
				.map(agentNodes::get)
				.map(EGCSNode.class::cast)
				.toList();

		greenEnergyNodes.forEach(node -> node.addEvent(this));
		cmaNode.addEvent(this);
		agentNode.addEvent(this);
	}
}
