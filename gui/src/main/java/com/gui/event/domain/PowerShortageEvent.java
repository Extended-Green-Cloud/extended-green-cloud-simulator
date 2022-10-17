package com.gui.event.domain;

import static com.gui.event.domain.EventTypeEnum.POWER_SHORTAGE_EVENT;

import java.time.Instant;

import com.gui.message.PowerShortageMessage;

/**
 * Event making the given agent exposed to the power shortage
 */
public class PowerShortageEvent extends AbstractEvent {

	private final int newMaximumCapacity;
	private final boolean finished;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime     time when the power shortage will happen
	 * @param newMaximumCapacity maximum power during the power shortage
	 * @param finished           flag indicating whether the event informs of the power shortage finish or start
	 */
	public PowerShortageEvent(Instant occurrenceTime, int newMaximumCapacity, boolean finished) {
		super(POWER_SHORTAGE_EVENT, occurrenceTime);
		this.newMaximumCapacity = newMaximumCapacity;
		this.finished = finished;
	}

	public PowerShortageEvent(PowerShortageMessage powerShortageMessage) {
		super(POWER_SHORTAGE_EVENT, powerShortageMessage.getData().getOccurrenceTime());
		this.newMaximumCapacity = powerShortageMessage.getData().getNewMaximumCapacity().intValue();
		this.finished = powerShortageMessage.getData().isFinished();
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
}
