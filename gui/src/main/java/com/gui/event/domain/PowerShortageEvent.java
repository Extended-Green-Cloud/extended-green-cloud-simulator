package com.gui.event.domain;

import static com.gui.event.domain.EventTypeEnum.POWER_SHORTAGE_EVENT;

import java.time.Instant;

import com.greencloud.commons.args.event.powershortage.PowerShortageCause;
import com.gui.message.PowerShortageMessage;

/**
 * Event making the given agent exposed to the power shortage
 */
public class PowerShortageEvent extends AbstractEvent {

	private final int newMaximumCapacity;
	private final boolean finished;
	private final PowerShortageCause cause;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime     time when the power shortage will happen
	 * @param newMaximumCapacity maximum power during the power shortage
	 * @param finished           flag indicating whether the event informs of the power shortage finish or start
	 * @param cause              the main cause of the power shortage
	 */
	public PowerShortageEvent(Instant occurrenceTime, int newMaximumCapacity, boolean finished,
			final PowerShortageCause cause) {
		super(POWER_SHORTAGE_EVENT, occurrenceTime);
		this.newMaximumCapacity = newMaximumCapacity;
		this.finished = finished;
		this.cause = cause;
	}

	public PowerShortageEvent(PowerShortageMessage powerShortageMessage) {
		super(POWER_SHORTAGE_EVENT, powerShortageMessage.getData().getOccurrenceTime());
		this.newMaximumCapacity = powerShortageMessage.getData().getNewMaximumCapacity().intValue();
		this.finished = powerShortageMessage.getData().isFinished();
		this.cause = PowerShortageCause.PHYSICAL_CAUSE;
	}

	/**
	 * @return new maximum power during power shortage
	 */
	public int getNewMaximumCapacity() {
		return newMaximumCapacity;
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
	public PowerShortageCause getCause() {
		return cause;
	}
}
