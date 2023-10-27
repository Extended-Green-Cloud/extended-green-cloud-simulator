package org.greencloud.rulescontroller.rest.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.enums.rules.RuleStepType;
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
		@JsonSubTypes.Type(value = RuleRest.class, name = "CHAIN"),
		@JsonSubTypes.Type(value = ScheduledRuleRest.class, name = "SCHEDULED"),
		@JsonSubTypes.Type(value = PeriodicRuleRest.class, name = "PERIODIC"),
		@JsonSubTypes.Type(value = ProposalRuleRest.class, name = "PROPOSAL"),
		@JsonSubTypes.Type(value = BehaviourRuleRest.class, name = "BEHAVIOUR"),
		@JsonSubTypes.Type(value = CallForProposalRuleRest.class, name = "CFP"),
		@JsonSubTypes.Type(value = MessageListenerRuleRest.class, name = "LISTENER"),
		@JsonSubTypes.Type(value = SingleMessageListenerRuleRest.class, name = "LISTENER_SINGLE"),
		@JsonSubTypes.Type(value = RequestRuleRest.class, name = "REQUEST"),
		@JsonSubTypes.Type(value = SearchRuleRest.class, name = "SEARCH"),
		@JsonSubTypes.Type(value = SubscriptionRuleRest.class, name = "SUBSCRIPTION"),
		@JsonSubTypes.Type(value = CombinedRuleRest.class, name = "COMBINED")
})
public class RuleRest implements Serializable {

	String agentType;
	String type;
	String subType;
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
