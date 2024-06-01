package org.greencloud.agentsystem.strategies.deafult.rules.centralmanager.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_NEW_CLIENT_JOB_TEMPLATE;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForNewClientJobsRule extends AgentMessageListenerRule<CentralManagerAgentProps, CMANode> {

	public ListenForNewClientJobsRule(final RulesController<CentralManagerAgentProps, CMANode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ClientJob.class, LISTEN_FOR_NEW_CLIENT_JOB_TEMPLATE, 1,
				NEW_JOB_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_RULE,
				"listen for new client jobs",
				"rule run when CMA reads new Client Job message");
	}

	@Override
	public AgentRule copy() {
		return new ListenForNewClientJobsRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
