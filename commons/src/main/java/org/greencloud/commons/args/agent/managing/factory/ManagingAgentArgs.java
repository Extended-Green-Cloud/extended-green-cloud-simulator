package org.greencloud.commons.args.agent.managing.factory;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Objects;

import org.immutables.value.Value;
import org.jrba.agentmodel.domain.args.AgentArgs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

/**
 * Arguments used to build Green Energy Agent
 */
@Value.Immutable
@JsonSerialize(as = ImmutableManagingAgentArgs.class)
@JsonDeserialize(as = ImmutableManagingAgentArgs.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ManagingAgentArgs extends AgentArgs {

	/**
	 * @return threshold indicating the desired system quality
	 */
	double getSystemQualityThreshold();

	/**
	 * @return minimum number for power drops enabling green source to be considered
	 * in weather prediction error adaptation
	 */
	@Value.Default
	default Integer getPowerShortageThreshold() {
		return 2;
	}

	/**
	 * @return time (in milliseconds) in between consecutive monitoring system calls
	 */
	@Value.Default
	default Integer getMonitoringSystemTimeout() {
		return 500;
	}

	/**
	 * @return time frame (in seconds) of historic data that is taken into during the re-evaluation of quality goals
	 */
	@Value.Default
	default Integer getQualityGoalsEvaluationTimeFrame() {
		return 24 * 60 * 60;
	}

	/**
	 * @return number of historic consecutive quality goals' value records that are taken into account during
	 * system analysis
	 */
	@Value.Default
	default Integer getQualityGoalsWindowSize() {
		return 10;
	}

	/**
	 * @return time frame (in seconds) of short term historic data that is taken into account during system analysis
	 * and action planning
	 */
	@Value.Default
	default Integer getShortTermDataTimeFrame() {
		return 10;
	}

	/**
	 * @return time frame (in seconds) of long-term historic data that is taken into account during system analysis
	 * and action planning
	 */
	@Value.Default
	default Integer getLongTermDataTimeFrame() {
		return 15;
	}

	/**
	 * @return time frame (in milliseconds) of historic health-check data that is taken into account during system
	 * analysis and action planning
	 */
	@Value.Default
	default Integer getHealthCheckTimeFrame() {
		return 500;
	}

	/**
	 * @return time (in seconds) that the system waits for the verification of the component adaptation action impact
	 */
	@Value.Default
	default Integer getComponentAdaptationVerificationDelay() {
		return 5;
	}

	/**
	 * @return time (in seconds) that the system waits for the verification of the system adaptation action impact
	 */
	@Value.Default
	default Integer getSystemAdaptationVerificationDelay() {
		return 10;
	}

	/**
	 * @return time (in milliseconds) that the system waits for the confirmation of the system adaptation plan
	 */
	@Value.Default
	default Integer getSystemAdaptationPlanConfirmationTimeout() {
		return 20000;
	}

	/**
	 * @return (optional) list of actions specified by their type names which are by default disabled in the system
	 */
	@Value.Default
	@JacksonXmlElementWrapper(localName = "disabledActions")
	default ArrayList<String> getDisabledActions() {
		return new ArrayList<>();
	}

	@Value.Check
	default void check() {
		if (getSystemQualityThreshold() <= 0 || getSystemQualityThreshold() > 1) {
			throw new InvalidParameterException("Quality threshold must be a value from range [0,1]");
		}
		if (Objects.nonNull(getPowerShortageThreshold()) && getPowerShortageThreshold() < 1) {
			throw new InvalidParameterException("Minimum number of power drops must be at least 1");
		}
	}
}
