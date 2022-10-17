package com.gui.message.domain;

import java.time.Instant;

public interface EventData {

	Instant getOccurrenceTime();

	boolean isFinished();
}
