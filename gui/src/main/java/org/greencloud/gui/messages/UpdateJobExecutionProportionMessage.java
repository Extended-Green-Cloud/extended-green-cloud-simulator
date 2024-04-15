package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateJobExecutionProportionMessage.class)
@JsonDeserialize(as = ImmutableUpdateJobExecutionProportionMessage.class)
public interface UpdateJobExecutionProportionMessage extends ExternalMessage {

	Double getData();

	String getAgentName();

	default String getType() {
		return "UPDATE_JOB_EXECUTION_PROPORTION";
	}
}
