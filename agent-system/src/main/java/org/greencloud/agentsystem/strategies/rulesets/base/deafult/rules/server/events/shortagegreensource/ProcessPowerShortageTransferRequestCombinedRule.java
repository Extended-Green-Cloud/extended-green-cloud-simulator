package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource;

import static jade.lang.acl.ACLMessage.REFUSE;
import static java.util.Objects.isNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.processing.ProcessPowerShortageTransferRequestGreenSourceRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.processing.ProcessPowerShortageTransferRequestJobFinishedRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.processing.ProcessPowerShortageTransferRequestNoGreenSourcesRule;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

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
				new ProcessPowerShortageTransferRequestGreenSourceRule(controller),
				new ProcessPowerShortageTransferRequestNoGreenSourcesRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final JobPowerShortageTransfer transfer = facts.get(MESSAGE_CONTENT);
		final ClientJob job = getJobByInstanceId(transfer.getOriginalJobInstanceId(), agentProps.getServerJobs());

		if (isNull(job)) {
			agent.send(prepareStringReply(facts.get(MESSAGE), JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));
			return false;
		}

		facts.put(JOB, job);
		return true;
	}

	@Override
	public AgentRule copy() {
		return new ProcessPowerShortageTransferRequestCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
