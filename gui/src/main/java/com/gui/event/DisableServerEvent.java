package com.gui.event;

import static com.gui.event.domain.EventTypeEnum.DISABLE_SERVER_EVENT;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.exception.IncorrectMessageContentException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gui.agents.egcs.EGCSNode;
import com.gui.message.SwitchServerOnOffGUIMessage;

import lombok.Getter;

/**
 * Event simulating server disabling
 */
@Getter
public class DisableServerEvent extends AbstractEvent {

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 * @param agentName      name of the agent for which the event is executed
	 */
	protected DisableServerEvent(final Instant occurrenceTime, final String agentName) {
		super(DISABLE_SERVER_EVENT, occurrenceTime, agentName);
	}

	public DisableServerEvent(SwitchServerOnOffGUIMessage switchServerOnOffGUIMessage) {
		this(switchServerOnOffGUIMessage.getEventData().getOccurrenceTime(),
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
			return mapper.readValue(message, SwitchServerOnOffGUIMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public void trigger(final Map<String, EGCSNode> agentNodes) {
		agentNodes.get(agentName).addEvent(this);
	}
}
