package org.greencloud.gui.event;

import static org.greencloud.commons.enums.event.EventTypeEnum.GREEN_SOURCE_CREATION_EVENT;
import static org.jrba.utils.mapper.JsonMapper.getMapper;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.messages.CreateGreenSourceMessage;
import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.environment.domain.ExternalEvent;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

@Getter
@SuppressWarnings("rawtypes")
public class GreenSourceCreationEvent extends ExternalEvent {

	GreenSourceCreator greenSourceCreator;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 */
	protected GreenSourceCreationEvent(final Instant occurrenceTime, final GreenSourceCreator greenSourceCreator) {
		super(null, GREEN_SOURCE_CREATION_EVENT, occurrenceTime);
		this.greenSourceCreator = greenSourceCreator;
	}

	public GreenSourceCreationEvent(CreateGreenSourceMessage createGreenSourceMessage) {
		this(createGreenSourceMessage.getData().getOccurrenceTime(), createGreenSourceMessage.getData());
	}

	public static GreenSourceCreationEvent create(String message) {
		final CreateGreenSourceMessage createGreenSourceMessage = readGreenSourceCreationMessage(message);
		return new GreenSourceCreationEvent(createGreenSourceMessage);
	}

	private static CreateGreenSourceMessage readGreenSourceCreationMessage(String message) {
		try {
			return getMapper().readValue(message, CreateGreenSourceMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public <T extends AgentNode> void trigger(final Map<String, T> agentNodes) {
		// no communication with agents here
	}
}
