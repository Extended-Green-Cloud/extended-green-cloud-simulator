package com.database.knowledge.domain.agent.monitoring;

import org.immutables.value.Value;

import com.database.knowledge.domain.MonitoringData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import domain.agent.monitoring.ImmutableProcessedApiRequest;

@Value.Immutable
@JsonSerialize(as = ImmutableProcessedApiRequest.class)
@JsonDeserialize(as = ImmutableProcessedApiRequest.class)
public interface ProcessedApiRequest extends MonitoringData {

	String getRequestedType();

	String getRequestedTimeslot();

	String getCallType();
}
