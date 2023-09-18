package com.gui.event;

import static com.gui.event.domain.EventTypeEnum.POWER_SHORTAGE_EVENT;

import java.time.Instant;

import org.greencloud.commons.enums.event.PowerShortageCauseEnum;
import com.gui.message.PowerShortageMessage;

/**
 * Event making the given agent exposed to the power shortage
 */
public class PowerShortageEvent extends AbstractEvent {

	private final boolean finished;
	private final PowerShortageCauseEnum cause;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime     time when the power shortage will happen
	 * @param finished           flag indicating whether the event informs of the power shortage finish or start
	 * @param cause              the main cause of the power shortage
	 */
	public PowerShortageEvent(Instant occurrenceTime, boolean finished, final PowerShortageCauseEnum cause) {
		super(POWER_SHORTAGE_EVENT, occurrenceTime);
		this.finished = finished;
		this.cause = cause;
	}

	public PowerShortageEvent(PowerShortageMessage powerShortageMessage) {
		super(POWER_SHORTAGE_EVENT, powerShortageMessage.getData().getOccurrenceTime());
		this.finished = powerShortageMessage.getData().isFinished();
		this.cause = PowerShortageCauseEnum.PHYSICAL_CAUSE;
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
