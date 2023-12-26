package org.greencloud.commons.args.event;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface containing properties of scenario event that changes a rule set for given system components
 */
@Value.Immutable
@JsonSerialize(as = ImmutableModifyRuleSetEventArgs.class)
@JsonDeserialize(as = ImmutableModifyRuleSetEventArgs.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName("MODIFY_RULE_SET")
public interface ModifyRuleSetEventArgs extends EventArgs {

	/**
	 * @return agent representing region for which the rule set is to be modified
	 */
	String getAgentName();

	/**
	 * @return information if rule set should be fully exchanged or just modified
	 */
	Boolean getFullReplace();

	/**
	 * @return name of the rule set with which agent rules are to be exchanged
	 */
	String getRuleSetName();

}
