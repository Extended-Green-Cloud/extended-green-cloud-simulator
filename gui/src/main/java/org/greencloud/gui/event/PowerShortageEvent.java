package org.greencloud.gui.event;

import static org.greencloud.commons.enums.event.EventTypeEnum.POWER_SHORTAGE_EVENT;
import static org.jrba.utils.mapper.JsonMapper.getMapper;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.greencloud.commons.enums.event.PowerShortageCauseEnum;
import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.messages.PowerShortageMessage;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.environment.domain.ExternalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;

/**
 * Event making the given agent exposed to the power shortage
 */
@Getter
@SuppressWarnings("rawtypes")
public class PowerShortageEvent extends ExternalEvent {

	private static final Logger logger = LoggerFactory.getLogger(PowerShortageEvent.class);

	private final boolean finished;
	private final PowerShortageCauseEnum cause;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the power shortage will happen
	 * @param finished       flag indicating whether the event informs of the power shortage finish or start
	 * @param cause          the main cause of the power shortage
	 * @param agentName      name of the agent for which event was triggered
	 */
	public PowerShortageEvent(Instant occurrenceTime, boolean finished, final PowerShortageCauseEnum cause,
			final String agentName) {
		super(agentName, POWER_SHORTAGE_EVENT, occurrenceTime);
		this.finished = finished;
		this.cause = cause;
	}

	public PowerShortageEvent(PowerShortageMessage powerShortageMessage) {
		super(powerShortageMessage.getAgentName(), POWER_SHORTAGE_EVENT,
				powerShortageMessage.getData().getOccurrenceTime());
		this.finished = Boolean.TRUE.equals(powerShortageMessage.getData().isFinished());
		this.cause = PowerShortageCauseEnum.PHYSICAL_CAUSE;
	}

	/**
	 * Method creates event from the given message
	 *
	 * @param message received message
	 * @return PowerShortageEvent
	 */
	public static PowerShortageEvent create(String message) {
		final PowerShortageMessage powerShortageMessage = readPowerShortage(message);
		return new PowerShortageEvent(powerShortageMessage);
	}

	private static PowerShortageMessage readPowerShortage(String message) {
		try {
			return getMapper().readValue(message, PowerShortageMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public <T extends AgentNode> void trigger(final Map<String, T> agentNodes) {
		AgentNode agentNode = agentNodes.get(agentName);

		if (Objects.isNull(agentNode)) {
			logger.error("Agent {} was not found. Power shortage couldn't be triggered", agentName);
			return;
		}
		agentNode.addEvent(this);
	}
}
