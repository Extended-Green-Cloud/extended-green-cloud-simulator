package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.errorhandling.listening;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ID;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.jrba.rulesengine.enums.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.errorhandling.listening.processing.ProcessTransferRequestJobNotFoundRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.errorhandling.listening.processing.ProcessTransferRequestJobPresentCombinedRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessTransferRequestCombinedRule
		extends AgentCombinedRule<RegionalManagerAgentProps, RegionalManagerNode> {

	public ProcessTransferRequestCombinedRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE,
				"transfer job handler",
				"handles request to transfer job from one Server to another");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessTransferRequestJobNotFoundRule(controller),
				new ProcessTransferRequestJobPresentCombinedRule(controller).getRules().get(0)
		);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobPowerShortageTransfer transferData = facts.get(MESSAGE_CONTENT);
		final String jobId = transferData.getSecondJobInstanceId().getJobId();
		final ClientJob job = getJobById(jobId, agentProps.getNetworkJobs());

		facts.put(JOB, ofNullable(job));
		facts.put(JOB_ID, jobId);
	}
}
