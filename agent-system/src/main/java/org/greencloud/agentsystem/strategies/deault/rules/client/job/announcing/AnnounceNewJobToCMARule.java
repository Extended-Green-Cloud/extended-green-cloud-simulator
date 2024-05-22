package org.greencloud.agentsystem.strategies.deault.rules.client.job.announcing;

import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobAnnouncementMessage;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;

public class AnnounceNewJobToCMARule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(AnnounceNewJobToCMARule.class);

	public AnnounceNewJobToCMARule(final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ANNOUNCEMENT_RULE,
				"announcing new job to CMA.",
				"when CMA was found, Client announce new job.");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final AID cma = facts.get(AGENT);
		logger.info("Sending job announcement information to Central Manager Agent.");
		agent.send(prepareJobAnnouncementMessage(cma, agentProps.getJob(), facts.get(RULE_SET_IDX)));
	}

	@Override
	public AgentRule copy() {
		return new AnnounceNewJobToCMARule(controller);
	}

	@Override
	public String getAgentType() {
		return CLIENT.getName();
	}
}
