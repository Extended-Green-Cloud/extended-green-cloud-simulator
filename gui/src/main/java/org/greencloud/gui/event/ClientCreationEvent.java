package org.greencloud.gui.event;

import static org.greencloud.commons.enums.event.EventTypeEnum.CLIENT_CREATION_EVENT;
import static org.jrba.utils.mapper.JsonMapper.getMapper;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.messages.CreateClientMessage;
import org.greencloud.gui.messages.domain.JobCreator;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.environment.domain.ExternalEvent;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

@Getter
@SuppressWarnings("rawtypes")
public class ClientCreationEvent extends ExternalEvent {

	JobCreator jobCreator;
	String clientName;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 */
	protected ClientCreationEvent(final Instant occurrenceTime, final JobCreator jobCreator, final String clientName) {
		super(clientName, CLIENT_CREATION_EVENT, occurrenceTime);
		this.jobCreator = jobCreator;
		this.clientName = clientName;
	}

	public ClientCreationEvent(CreateClientMessage createClientMessage) {
		this(createClientMessage.getData().getOccurrenceTime(), createClientMessage.getData(),
				createClientMessage.getClientName());
	}

	public static ClientCreationEvent create(String message) {
		final CreateClientMessage createClientMessage = readClientCreationMessage(message);
		return new ClientCreationEvent(createClientMessage);
	}

	private static CreateClientMessage readClientCreationMessage(String message) {
		try {
			return getMapper().readValue(message, CreateClientMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public <T extends AgentNode> void trigger(final Map<String, T> agentNodes) {
		// no communication with agents here
	}
}
