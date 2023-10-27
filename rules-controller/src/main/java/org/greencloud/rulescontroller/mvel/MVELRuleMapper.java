package org.greencloud.rulescontroller.mvel;

import org.greencloud.rulescontroller.rest.domain.BehaviourRuleRest;
import org.greencloud.rulescontroller.rest.domain.CallForProposalRuleRest;
import org.greencloud.rulescontroller.rest.domain.CombinedRuleRest;
import org.greencloud.rulescontroller.rest.domain.MessageListenerRuleRest;
import org.greencloud.rulescontroller.rest.domain.PeriodicRuleRest;
import org.greencloud.rulescontroller.rest.domain.ProposalRuleRest;
import org.greencloud.rulescontroller.rest.domain.RequestRuleRest;
import org.greencloud.rulescontroller.rest.domain.RuleRest;
import org.greencloud.rulescontroller.rest.domain.ScheduledRuleRest;
import org.greencloud.rulescontroller.rest.domain.SearchRuleRest;
import org.greencloud.rulescontroller.rest.domain.SingleMessageListenerRuleRest;
import org.greencloud.rulescontroller.rest.domain.SubscriptionRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.simple.AgentBehaviourRule;
import org.greencloud.rulescontroller.rule.simple.AgentChainRule;
import org.greencloud.rulescontroller.rule.template.AgentCFPRule;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;
import org.greencloud.rulescontroller.rule.template.AgentProposalRule;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
import org.greencloud.rulescontroller.rule.template.AgentSearchRule;
import org.greencloud.rulescontroller.rule.template.AgentSingleMessageListenerRule;
import org.greencloud.rulescontroller.rule.template.AgentSubscriptionRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

/**
 * Class containing methods to map rules obtained using MVEL expressions
 */
public class MVELRuleMapper {

	public static AgentRule getRuleForType(final RuleRest ruleRest, final RuleSet ruleSet) {
		return switch (ruleRest.getAgentRuleType()) {
			case SCHEDULED -> new AgentScheduledRule<>((ScheduledRuleRest) ruleRest);
			case PERIODIC -> new AgentPeriodicRule<>((PeriodicRuleRest) ruleRest);
			case PROPOSAL -> new AgentProposalRule<>((ProposalRuleRest) ruleRest);
			case REQUEST -> new AgentRequestRule<>((RequestRuleRest) ruleRest);
			case BEHAVIOUR -> new AgentBehaviourRule<>((BehaviourRuleRest) ruleRest);
			case SEARCH -> new AgentSearchRule<>((SearchRuleRest) ruleRest);
			case CFP -> new AgentCFPRule<>((CallForProposalRuleRest) ruleRest);
			case SUBSCRIPTION -> new AgentSubscriptionRule<>((SubscriptionRuleRest) ruleRest);
			case LISTENER_SINGLE -> new AgentSingleMessageListenerRule<>((SingleMessageListenerRuleRest) ruleRest);
			case COMBINED -> new AgentCombinedRule<>((CombinedRuleRest) ruleRest, ruleSet);
			case CHAIN -> new AgentChainRule<>(ruleRest, ruleSet);
			case LISTENER -> new AgentMessageListenerRule<>((MessageListenerRuleRest) ruleRest, ruleSet);
			default -> new AgentBasicRule<>(ruleRest);
		};
	}
}
