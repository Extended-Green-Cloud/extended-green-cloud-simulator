package com.gui.message;

import java.util.concurrent.PriorityBlockingQueue;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.job.ClientJob;

@JsonSerialize(as = ImmutableUpdateJobQueueMessage.class)
@JsonDeserialize(as = ImmutableUpdateJobQueueMessage.class)
@Value.Immutable
public interface UpdateJobQueueMessage {
	PriorityBlockingQueue<ClientJob> getData();

	String getType();

}
