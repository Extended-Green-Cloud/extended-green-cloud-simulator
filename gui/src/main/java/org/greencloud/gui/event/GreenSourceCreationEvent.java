package org.greencloud.gui.event;

import static org.greencloud.gui.event.domain.EventTypeEnum.GREEN_SOURCE_CREATION_EVENT;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.messages.CreateGreenSourceMessage;
import org.greencloud.gui.messages.domain.GreenSourceCreator;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

@Getter
public class GreenSourceCreationEvent extends AbstractEvent {

	GreenSourceCreator greenSourceCreator;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 */
	protected GreenSourceCreationEvent(final Instant occurrenceTime, final GreenSourceCreator greenSourceCreator) {
		super(GREEN_SOURCE_CREATION_EVENT, occurrenceTime, null);
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
			return mapper.readValue(message, CreateGreenSourceMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public void trigger(final Map<String, EGCSNode> agentNodes) {
		// no communication with agents here
	}
}
