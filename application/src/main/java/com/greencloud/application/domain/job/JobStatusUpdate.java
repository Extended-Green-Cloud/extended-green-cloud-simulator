package com.greencloud.application.domain.job;

import java.time.Instant;

/**
 * Content of the message sent when the job status is being changed
 *
 * @param jobInstance job of interest
 * @param changeTime  time when the status has changed
 */
public record JobStatusUpdate(JobInstanceIdentifier jobInstance, Instant changeTime) {
}
