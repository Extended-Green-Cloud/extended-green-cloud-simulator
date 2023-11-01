package org.greencloud.gui.event;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.event.domain.EventTypeEnum;
import org.greencloud.gui.messages.SwitchServerOnOffGUIMessage;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

/**
 * Event simulating server enabling
 */
@Getter
public class EnableServerEvent extends AbstractEvent {

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 * @param agentName      name of the agent for which the event is executed
	 */
	protected EnableServerEvent(final Instant occurrenceTime, final String agentName) {
		super(EventTypeEnum.ENABLE_SERVER_EVENT, occurrenceTime, agentName);
	}

	public EnableServerEvent(SwitchServerOnOffGUIMessage switchServerOnOffGUIMessage) {
		this(switchServerOnOffGUIMessage.getData().getOccurrenceTime(),
				switchServerOnOffGUIMessage.getAgentName());
	}

	/**
	 * Method creates event from the given message
	 *
	 * @param message received message
	 * @return PowerShortageEvent
	 */
	public static EnableServerEvent create(String message) {
		final SwitchServerOnOffGUIMessage powerShortageMessage = readDisableServerMessage(message);
		return new EnableServerEvent(powerShortageMessage);
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
