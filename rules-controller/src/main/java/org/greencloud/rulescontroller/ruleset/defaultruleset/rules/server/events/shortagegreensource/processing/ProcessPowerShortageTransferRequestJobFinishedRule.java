package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource.processing;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_HANDLE_JOB_NOT_FOUND_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static jade.lang.acl.ACLMessage.REFUSE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.server.ServerNode;

public class ProcessPowerShortageTransferRequestJobFinishedRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public ProcessPowerShortageTransferRequestJobFinishedRule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE,
				LISTEN_FOR_JOB_TRANSFER_HANDLE_JOB_NOT_FOUND_RULE,
				"handles job transfer request when job is not found",
				"rule handles the transfer request coming from Green Source affected by power shortage");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		return !job.getEndTime().isAfter(getCurrentTime());
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		agent.send(prepareStringReply(facts.get(MESSAGE), DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE, REFUSE));
	}
}

