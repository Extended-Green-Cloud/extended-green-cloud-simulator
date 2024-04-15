package org.greencloud.agentsystem.strategies.deault.rules.server.events.shortagegreensource.processing;

import static jade.lang.acl.ACLMessage.REFUSE;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLE_JOB_NOT_FOUND_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;

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

