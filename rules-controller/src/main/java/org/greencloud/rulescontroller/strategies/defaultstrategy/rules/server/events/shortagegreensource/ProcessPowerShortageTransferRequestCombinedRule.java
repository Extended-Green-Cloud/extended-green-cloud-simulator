package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.events.shortagegreensource;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.util.Objects.isNull;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.events.shortagegreensource.processing.ProcessPowerShortageTransferRequestJobFinishedRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.events.shortagegreensource.processing.ProcessPowerShortageTransferRequestTransferCNARule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.events.shortagegreensource.processing.ProcessPowerShortageTransferRequestTransferGreenSourceRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class ProcessPowerShortageTransferRequestCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessPowerShortageTransferRequestCombinedRule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE,
				"handles job transfer Green Source request",
				"rule handles the transfer request coming from Green Source affected by power shortage");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessPowerShortageTransferRequestJobFinishedRule(controller),
				new ProcessPowerShortageTransferRequestTransferCNARule(controller),
				new ProcessPowerShortageTransferRequestTransferGreenSourceRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final JobPowerShortageTransfer transfer = facts.get(MESSAGE_CONTENT);
		final ClientJob job = getJobByInstanceId(transfer.getOriginalJobInstanceId(), agentProps.getServerJobs());

		if (isNull(job)) {
			agent.send(prepareStringReply(facts.get(MESSAGE), JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));
			return false;
		}

		facts.put(JOB, job);
		return true;
	}
}
