package org.greencloud.gui.messages;

import java.util.LinkedList;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.job.instance.JobInstanceScheduler;

@JsonSerialize(as = ImmutableUpdateJobQueueMessage.class)
@JsonDeserialize(as = ImmutableUpdateJobQueueMessage.class)
@Value.Immutable
public interface UpdateJobQueueMessage extends Message {
	LinkedList<JobInstanceScheduler> getData();

	default String getType() {
		return "UPDATE_JOB_QUEUE";
	}
}
