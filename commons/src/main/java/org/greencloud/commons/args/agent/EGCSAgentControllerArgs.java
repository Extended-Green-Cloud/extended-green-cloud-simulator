package org.greencloud.commons.args.agent;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.jrba.agentmodel.domain.args.AgentArgs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jade.core.AID;

/**
 * Arguments passed to agent controllers
 */
@JsonSerialize(as = ImmutableEGCSAgentControllerArgs.class)
@JsonDeserialize(as = ImmutableEGCSAgentControllerArgs.class)
@Value.Immutable
public interface EGCSAgentControllerArgs <T extends AgentArgs>  {

	T getAgentArgs();

	Optional<String> getMainDFAddress();

	Optional<String> getMainHostPlatformId();

	Optional<Map<String, Map<String, Object>>> getSystemKnowledge();

	Boolean isInformer();

	Optional<AID> getManagingAgent();
}
