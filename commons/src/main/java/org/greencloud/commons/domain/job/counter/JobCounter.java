package org.greencloud.commons.domain.job.counter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Record represents counter object that is used in agent state management services in order to record the number
 * of executed jobs (i.e. counting aggregated number of jobs per each execution state).
 * It has mainly the statistical and debugging purposes.
 *
 * @param count   aggregated number of jobs per each execution state
 * @param handler handler executed after changing the counter
 */
public record JobCounter(AtomicLong count, Consumer<String> handler) {

	public JobCounter(final Consumer<String> handler) {
		this(new AtomicLong(0L), handler);
	}

	public long getCount() {
		return count.get();
	}

}
