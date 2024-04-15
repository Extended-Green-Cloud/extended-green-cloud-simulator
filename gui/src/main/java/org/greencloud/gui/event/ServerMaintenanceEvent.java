package org.greencloud.gui.event;

import static org.greencloud.commons.enums.event.EventTypeEnum.SERVER_MAINTENANCE_EVENT;
import static org.jrba.utils.mapper.JsonMapper.getMapper;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.messages.ServerMaintenanceMessage;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.environment.domain.ExternalEvent;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

/**
 * Event simulating server enabling
 */
@Getter
@SuppressWarnings("rawtypes")
public class ServerMaintenanceEvent extends ExternalEvent {

	Map<String, Resource> newResources;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 * @param agentName      name of the agent for which the event is executed
	 */
	public ServerMaintenanceEvent(final Instant occurrenceTime, final String agentName,
			final Map<String, Resource> newResources) {
		super(agentName, SERVER_MAINTENANCE_EVENT, occurrenceTime);
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
			return getMapper().readValue(message, ServerMaintenanceMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public <T extends AgentNode> void trigger(final Map<String, T> agentNodes) {
		agentNodes.get(agentName).addEvent(this);
	}
}
