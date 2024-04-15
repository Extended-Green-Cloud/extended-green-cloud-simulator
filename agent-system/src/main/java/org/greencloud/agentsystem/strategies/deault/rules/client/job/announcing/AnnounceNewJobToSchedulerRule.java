package org.greencloud.agentsystem.strategies.deault.rules.client.job.announcing;

import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobAnnouncementMessage;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;

public class AnnounceNewJobToSchedulerRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(AnnounceNewJobToSchedulerRule.class);

	public AnnounceNewJobToSchedulerRule(final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ANNOUNCEMENT_RULE,
				"announcing new job to RMA",
				"when Scheduler Agent was found, Client announce new job");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final AID scheduler = facts.get(AGENT);
		logger.info("Sending job announcement information to Scheduler Agent.");
		agent.send(prepareJobAnnouncementMessage(scheduler, agentProps.getJob(), facts.get(RULE_SET_IDX)));
	}
}
