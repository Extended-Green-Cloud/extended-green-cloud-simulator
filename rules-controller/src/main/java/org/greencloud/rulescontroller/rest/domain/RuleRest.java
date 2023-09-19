package org.greencloud.rulescontroller.rest.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.AgentType;
import org.greencloud.commons.enums.rules.RuleStepType;
import org.greencloud.commons.enums.rules.RuleType;
import org.greencloud.rulescontroller.mvel.MVELObjectType;
import org.greencloud.rulescontroller.rule.AgentRuleType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		property = "agentRuleType",
		visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = RuleRest.class, name = "BASIC"),
		@JsonSubTypes.Type(value = ScheduledRuleRest.class, name = "SCHEDULED"),
		@JsonSubTypes.Type(value = ProposalRuleRest.class, name = "PROPOSAL")
})
public class RuleRest implements Serializable {

	AgentType agentType;
	RuleType type;
	RuleType subType;
	RuleStepType stepType;
	Integer priority;
	String name;
	String description;
	AgentRuleType agentRuleType;
	Map<String, MVELObjectType> initialParams;
	List<String> imports;
	String execute;
	String evaluate;
}
