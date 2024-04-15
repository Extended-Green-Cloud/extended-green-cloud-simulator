package org.greencloud.gui.messages;

import java.util.List;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.database.knowledge.domain.goal.AdaptationGoal;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableRegisterManagingAgentMessage.class)
@JsonDeserialize(as = ImmutableRegisterManagingAgentMessage.class)
@Value.Immutable
public interface RegisterManagingAgentMessage extends ExternalMessage {

	List<AdaptationGoal> getData();

	default String getType() {
		return "REGISTER_MANAGING";
	}

}
