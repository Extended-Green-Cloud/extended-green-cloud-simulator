package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableReportSystemStartTimeMessage.class)
@JsonDeserialize(as = ImmutableReportSystemStartTimeMessage.class)
@Value.Immutable
public interface ReportSystemStartTimeMessage extends Message {

	/**
	 * @return unix time instant representing system start time
	 */
	long getTime();

	/**
	 * @return number of seconds that corresponds in simulation time to 1 hour
	 */
	long getSecondsPerHour();

	default String getType() {
		return "REPORT_SYSTEM_START_TIME";
	}
}
