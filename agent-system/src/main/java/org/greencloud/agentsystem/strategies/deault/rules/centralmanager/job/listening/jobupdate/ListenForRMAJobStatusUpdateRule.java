package org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.listening.jobupdate;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_JOB_STATUS_UPDATE_TEMPLATE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ListenForRMAJobStatusUpdateRule extends AgentMessageListenerRule<CentralManagerAgentProps, CMANode> {

	public ListenForRMAJobStatusUpdateRule(final RulesController<CentralManagerAgentProps, CMANode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, JobWithStatus.class, LISTEN_FOR_JOB_STATUS_UPDATE_TEMPLATE, 20,
				JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for update regarding client job status",
				"rule run when CMA reads message with updated client job status");
	}

	@Override
	protected int selectRuleSetIdx(final RuleSetFacts facts) {
		final JobWithStatus jobUpdate = facts.get(MESSAGE_CONTENT);
		return agentProps.getRuleSetForJob().get(jobUpdate.getJobId());
	}

	@Override
	public AgentRule copy() {
		return new ListenForRMAJobStatusUpdateRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
