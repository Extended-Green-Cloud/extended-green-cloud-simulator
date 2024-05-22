package org.greencloud.gui.messages;

import java.util.LinkedList;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.job.instance.JobInstanceCMA;

@JsonSerialize(as = ImmutableUpdateJobQueueMessage.class)
@JsonDeserialize(as = ImmutableUpdateJobQueueMessage.class)
@Value.Immutable
public interface UpdateJobQueueMessage extends ExternalMessage {
	LinkedList<JobInstanceCMA> getData();

	default String getType() {
		return "UPDATE_JOB_QUEUE";
	}
}
