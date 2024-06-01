package org.greencloud.agentsystem.strategies.basetwostepallocation.rules.server.job.listening.allocation;

import static jade.lang.acl.ACLMessage.INFORM;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ALLOCATION_DATA_REQUEST_HANDLER_RULE;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForDataAllocationPreparation;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessRMARequestForAllocationData extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	public ProcessRMARequestForAllocationData(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(ALLOCATION_DATA_REQUEST_HANDLER_RULE,
				"handles new jobs allocation",
				"rule run when RMA processes new jobs allocation received from CMA");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final AllocatedJobs jobs = facts.get(MESSAGE_CONTENT);
		final RuleSetFacts dataFacts =
				constructFactsForDataAllocationPreparation(facts.get(RULE_SET_IDX), facts.get(MESSAGE), jobs, INFORM);
		controller.fire(dataFacts);
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMARequestForAllocationData(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
