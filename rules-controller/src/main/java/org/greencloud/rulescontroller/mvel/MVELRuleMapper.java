package org.greencloud.rulescontroller.mvel;

import org.greencloud.rulescontroller.rest.domain.ProposalRuleRest;
import org.greencloud.rulescontroller.rest.domain.RuleRest;
import org.greencloud.rulescontroller.rest.domain.ScheduledRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.template.AgentProposalRule;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;

/**
 * Class containing methods to map rules obtained using MVEL expressions
 */
public class MVELRuleMapper {

	public static final AgentRule getRuleForType(final RuleRest ruleRest) {
		return switch (ruleRest.getAgentRuleType()) {
			case SCHEDULED -> new AgentScheduledRule<>((ScheduledRuleRest) ruleRest);
			case PROPOSAL -> new AgentProposalRule<>((ProposalRuleRest) ruleRest);
			default -> new AgentBasicRule<>(ruleRest);
		};
	}
}
