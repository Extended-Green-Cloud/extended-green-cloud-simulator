package org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.polling.processing;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_HANDLE_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFY_DEADLINE_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessPollNextClientJobSuccessfullyRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	public ProcessPollNextClientJobSuccessfullyRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_POLLING_RULE, NEW_JOB_POLLING_HANDLE_JOB_RULE,
				"poll next job to be announced",
				"when there are jobs in the queue, CMA polls next job");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return agentProps.getJobsToBeExecuted().size() >= agentProps.getPollingBatchSize() ||
				(!agentProps.getJobsToBeExecuted().isEmpty() &&
						between(agentProps.getLastPollingTime(), now()).toSeconds() > 3);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		agentProps.setLastPollingTime(now());

		final List<ClientJob> jobsToAllocate = new ArrayList<>();
		agentProps.getJobsToBeExecuted().drainTo(jobsToAllocate, agentProps.getPollingBatchSize());
		agentNode.updateScheduledJobQueue(agentProps);

		final List<ClientJob> jobsWithAdjustedTime = jobsToAllocate.stream()
				.map(job -> checkTimeFrames(job, facts))
				.map(Optional::ofNullable)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();

		final RuleSetFacts allocationFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		allocationFacts.put(JOBS, jobsWithAdjustedTime);
		allocationFacts.put(RULE_TYPE, NEW_JOB_ALLOCATION_RULE);

		controller.fire(allocationFacts);
	}

	@Override
	public AgentRule copy() {
		return new ProcessPollNextClientJobSuccessfullyRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}

	private ClientJob checkTimeFrames(final ClientJob job, final RuleSetFacts facts) {
		facts.put(RULE_SET_IDX, agentProps.getRuleSetForJob().get(job.getJobId()));

		final RuleSetFacts timeFrameFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		timeFrameFacts.put(JOB, job);
		timeFrameFacts.put(RULE_TYPE, NEW_JOB_VERIFY_DEADLINE_RULE);

		controller.fire(timeFrameFacts);
		return timeFrameFacts.get(JOB);
	}
}
