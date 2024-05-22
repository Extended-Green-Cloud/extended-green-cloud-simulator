package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.proposing.prepare;

import static jade.lang.acl.ACLMessage.PROPOSE;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_JOB_PROPOSAL;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithPrice;
import org.greencloud.commons.domain.job.extended.JobWithPrice;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;

import jade.lang.acl.ACLMessage;

public class PrepareProposalForCMA extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	public PrepareProposalForCMA(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_JOB_PROPOSAL,
				"CMA proposal preparation",
				"prepares the content of proposal sent to CMA");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		final JobWithPrice pricedJob = ImmutableJobWithPrice.builder()
				.jobId(job.getJobId())
				.availableResources(agentProps.getAggregatedResources())
				.build();
		final ACLMessage offerMessage = MessageBuilder.builder(facts.get(RULE_SET_IDX))
				.copy(((ACLMessage) facts.get(MESSAGE)).createReply())
				.withObjectContent(pricedJob)
				.withPerformative(PROPOSE)
				.build();

		facts.put(RESULT, offerMessage);
	}

	@Override
	public AgentRule copy() {
		return new PrepareProposalForCMA(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
