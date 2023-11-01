package org.greencloud.gui.event;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.event.domain.EventTypeEnum;
import org.greencloud.gui.messages.ServerMaintenanceMessage;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

/**
 * Event simulating server enabling
 */
@Getter
public class ServerMaintenanceEvent extends AbstractEvent {

	Map<String, Resource> newResources;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 * @param agentName      name of the agent for which the event is executed
	 */
	protected ServerMaintenanceEvent(final Instant occurrenceTime, final String agentName,
			final Map<String, Resource> newResources) {
		super(EventTypeEnum.SERVER_MAINTENANCE_EVENT, occurrenceTime, agentName);
		this.newResources = newResources;
	}

	public ServerMaintenanceEvent(ServerMaintenanceMessage serverMaintenanceMessage) {
		this(serverMaintenanceMessage.getData().getOccurrenceTime(), serverMaintenanceMessage.getAgentName(),
				serverMaintenanceMessage.getData().getNewResources());
	}

	/**
	 * Method creates event from the given message
	 *
	 * @param message received message
	 * @return PowerShortageEvent
	 */
	public static ServerMaintenanceEvent create(String message) {
		final ServerMaintenanceMessage serverMaintenanceMessage = readServerMaintenanceMessage(message);
		return new ServerMaintenanceEvent(serverMaintenanceMessage);
	}

	private static ServerMaintenanceMessage readServerMaintenanceMessage(String message) {
		try {
			return mapper.readValue(message, ServerMaintenanceMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public void trigger(final Map<String, EGCSNode> agentNodes) {
		agentNodes.get(agentName).addEvent(this);
	}
}
