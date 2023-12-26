package org.greencloud.gui.event;

import static org.greencloud.commons.enums.event.EventTypeEnum.SERVER_CREATION_EVENT;
import static org.greencloud.commons.mapper.JsonMapper.getMapper;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.messages.CreateServerMessage;
import org.greencloud.gui.messages.domain.ServerCreator;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

@Getter
public class ServerCreationEvent extends AbstractEvent {

	ServerCreator serverCreator;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 */
	protected ServerCreationEvent(final Instant occurrenceTime, final ServerCreator serverCreator) {
		super(SERVER_CREATION_EVENT, occurrenceTime, null);
		this.serverCreator = serverCreator;
	}

	public ServerCreationEvent(CreateServerMessage createServerMessage) {
		this(createServerMessage.getData().getOccurrenceTime(), createServerMessage.getData());
	}

	public static ServerCreationEvent create(String message) {
		final CreateServerMessage createServerMessage = readServerCreationMessage(message);
		return new ServerCreationEvent(createServerMessage);
	}

	private static CreateServerMessage readServerCreationMessage(String message) {
		try {
			return getMapper().readValue(message, CreateServerMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public void trigger(final Map<String, EGCSNode> agentNodes) {
		// no communication with agents here
	}
}
