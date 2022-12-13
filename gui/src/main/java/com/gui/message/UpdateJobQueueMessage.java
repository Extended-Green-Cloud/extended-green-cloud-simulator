package com.gui.message;

import java.util.LinkedList;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.job.ClientJob;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableUpdateJobQueueMessage.class)
@JsonDeserialize(as = ImmutableUpdateJobQueueMessage.class)
@Value.Immutable
public interface UpdateJobQueueMessage extends Message {
	LinkedList<ClientJob> getData();

	default String getType() {
		return "UPDATE_JOB_QUEUE";
	}
}
