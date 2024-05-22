package org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.listening.jobupdate.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_UNKNOWN_STATUS_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FINISH_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;

import java.util.List;
import java.util.Optional;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessRMAJobStatusUpdateOtherStatusRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	public ProcessRMAJobStatusUpdateOtherStatusRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller, 4);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE,
				JOB_STATUS_RECEIVER_HANDLE_UNKNOWN_STATUS_RULE,
				"handles job update - default status handler",
				"rule runs when status which does not need special handling was received");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final String type = facts.get(MESSAGE_TYPE);
		return !List.of(FINISH_JOB_ID, STARTED_JOB_ID, FAILED_JOB_ID).contains(type) && !type.isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final Optional<ClientJob> jobOptional = facts.get(JOB);

		if (jobOptional.isPresent()) {
			final ClientJob job = jobOptional.get();
			final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);
			agent.send(prepareJobStatusMessageForClient(job, jobStatusUpdate, facts.get(MESSAGE_TYPE),
					facts.get(RULE_SET_IDX)));
		}
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMAJobStatusUpdateOtherStatusRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
