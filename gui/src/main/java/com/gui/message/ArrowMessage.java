package com.gui.message;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableArrowMessage.class)
@JsonDeserialize(as = ImmutableArrowMessage.class)
@Value.Immutable
public interface ArrowMessage {

	String getAgentName();

	List<String> getData();

	String getType();
}
