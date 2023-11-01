package org.greencloud.gui.event;

import static org.greencloud.gui.event.domain.EventTypeEnum.CLIENT_CREATION_EVENT;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.messages.CreateClientMessage;
import org.greencloud.gui.messages.domain.JobCreator;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

@Getter
public class ClientCreationEvent extends AbstractEvent {

	JobCreator jobCreator;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 */
	protected ClientCreationEvent(final Instant occurrenceTime, final JobCreator jobCreator) {
		super(CLIENT_CREATION_EVENT, occurrenceTime, null);
		this.jobCreator = jobCreator;
	}

	public ClientCreationEvent(CreateClientMessage createClientMessage) {
		this(createClientMessage.getData().getOccurrenceTime(), createClientMessage.getData());
	}

	public static ClientCreationEvent create(String message) {
		final CreateClientMessage createClientMessage = readClientCreationMessage(message);
		return new ClientCreationEvent(createClientMessage);
	}

	private static CreateClientMessage readClientCreationMessage(String message) {
		try {
			return mapper.readValue(message, CreateClientMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public void trigger(final Map<String, EGCSNode> agentNodes) {
		// no communication with agents here
	}
}
