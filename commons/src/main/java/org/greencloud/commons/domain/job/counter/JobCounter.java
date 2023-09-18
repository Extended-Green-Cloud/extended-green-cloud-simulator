package org.greencloud.commons.domain.job.counter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;

/**
 * Record represents counter object that is used in agent state management services in order to record the number
 * of executed jobs (i.e. counting aggregated number of jobs per each execution state).
 * It has mainly the statistical and debugging purposes.
 *
 * @param count   aggregated number of jobs per each execution state
 * @param handler handler executed after changing the counter
 */
public record JobCounter(AtomicLong count, Consumer<JobInstanceIdentifier> handler) {


	public JobCounter(final Consumer<JobInstanceIdentifier> handler) {
		this(new AtomicLong(0L), handler);
	}

	public long getCount() {
		return count.get();
	}

}
