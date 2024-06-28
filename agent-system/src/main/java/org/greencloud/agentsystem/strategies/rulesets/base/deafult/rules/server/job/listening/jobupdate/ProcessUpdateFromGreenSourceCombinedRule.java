package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobupdate;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;
import java.util.Optional;

import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobupdate.processing.ProcessUpdateFromGreenSourceJobConfirmationRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobupdate.processing.ProcessUpdateFromGreenSourceJobFailureRule;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessUpdateFromGreenSourceCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessUpdateFromGreenSourceCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE,
				"handles updates regarding job execution",
				"handling messages received from Green Source informing about changes in power supply");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessUpdateFromGreenSourceJobConfirmationRule(controller),
				new ProcessUpdateFromGreenSourceJobFailureRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final JobInstanceIdentifier instance = facts.get(MESSAGE_CONTENT);
		final ClientJob job = getJobByInstanceId(instance.getJobInstanceId(), agentProps.getServerJobs());
		final Optional<ClientJob> jobOptional = ofNullable(job);

		if (jobOptional.isPresent()) {
			facts.put(JOB, jobOptional.get());
			return true;
		}
		return false;
	}

	@Override
	public AgentRule copy() {
		return new ProcessUpdateFromGreenSourceCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
