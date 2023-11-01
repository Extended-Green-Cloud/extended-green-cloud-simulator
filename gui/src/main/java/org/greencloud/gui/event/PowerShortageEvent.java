package org.greencloud.gui.event;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.greencloud.commons.enums.event.PowerShortageCauseEnum;
import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.event.domain.EventTypeEnum;
import org.greencloud.gui.messages.PowerShortageMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Event making the given agent exposed to the power shortage
 */
public class PowerShortageEvent extends AbstractEvent {

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
		super(EventTypeEnum.POWER_SHORTAGE_EVENT, occurrenceTime, agentName);
		this.finished = finished;
		this.cause = cause;
	}

	public PowerShortageEvent(PowerShortageMessage powerShortageMessage) {
		super(EventTypeEnum.POWER_SHORTAGE_EVENT, powerShortageMessage.getData().getOccurrenceTime(),
				powerShortageMessage.getAgentName());
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
			return mapper.readValue(message, PowerShortageMessage.class);
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	@Override
	public void trigger(final Map<String, EGCSNode> agentNodes) {
		EGCSNode agentNode = agentNodes.get(agentName);

		if (Objects.isNull(agentNode)) {
			logger.error("Agent {} was not found. Power shortage couldn't be triggered", agentName);
			return;
		}
		agentNode.addEvent(this);
	}

	/**
	 * @return flag if the power shortage should be finished or started
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * @return cause of the power shortage
	 */
	public PowerShortageCauseEnum getCause() {
		return cause;
	}
}
