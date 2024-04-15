package org.greencloud.gui.event;

import static org.greencloud.commons.enums.event.EventTypeEnum.DISABLE_SERVER_EVENT;
import static org.jrba.utils.mapper.JsonMapper.getMapper;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.messages.SwitchServerOnOffGUIMessage;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.environment.domain.ExternalEvent;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

/**
 * Event simulating server disabling
 */
@Getter
@SuppressWarnings("rawtypes")
public class DisableServerEvent extends ExternalEvent {

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 * @param agentName      name of the agent for which the event is executed
	 */
	public DisableServerEvent(final Instant occurrenceTime, final String agentName) {
		super(agentName, DISABLE_SERVER_EVENT, occurrenceTime);
	}

	public DisableServerEvent(SwitchServerOnOffGUIMessage switchServerOnOffGUIMessage) {
		this(switchServerOnOffGUIMessage.getData().getOccurrenceTime(),
				switchServerOnOffGUIMessage.getAgentName());
	}

	/**
	 * Method creates event from the given message
	 *
	 * @param message received message
	 * @return PowerShortageEvent
	 */
	public static DisableServerEvent create(String message) {
		final SwitchServerOnOffGUIMessage powerShortageMessage = readDisableServerMessage(message);
		return new DisableServerEvent(powerShortageMessage);
	}

	private static SwitchServerOnOffGUIMessage readDisableServerMessage(String message) {
		try {
			return getMapper().readValue(message, SwitchServerOnOffGUIMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public <T extends AgentNode> void trigger(final Map<String, T> agentNodes) {
		agentNodes.get(agentName).addEvent(this);
	}
}
