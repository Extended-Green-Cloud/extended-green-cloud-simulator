package com.gui.message;

import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.enums.job.JobClientStatusEnum;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableSetClientJobDurationMapMessage.class)
@JsonDeserialize(as = ImmutableSetClientJobDurationMapMessage.class)
@Value.Immutable
public interface SetClientJobDurationMapMessage extends Message {

	Map<JobClientStatusEnum, Long> getData();

	String getAgentName();

	default String getType() {
		return "SET_CLIENT_JOB_DURATION_MAP";
	}
}
