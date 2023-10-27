package org.greencloud.commons.domain.ruleset;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object stores the data used in updating system rule set
 */
@JsonSerialize(as = ImmutableRuleSetUpdate.class)
@JsonDeserialize(as = ImmutableRuleSetUpdate.class)
@Value.Immutable
@ImmutableConfig
public interface RuleSetUpdate {

	int getRuleSetIdx();

	String getRuleSetType();
}
