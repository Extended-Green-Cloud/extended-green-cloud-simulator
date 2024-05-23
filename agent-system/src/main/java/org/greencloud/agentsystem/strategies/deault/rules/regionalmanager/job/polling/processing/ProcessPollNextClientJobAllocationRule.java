package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.polling.processing;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_HANDLE_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForJobsAllocationInitiation;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobDeadlineVerification;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessPollNextClientJobAllocationRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	public ProcessPollNextClientJobAllocationRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_POLLING_RULE, NEW_JOB_POLLING_HANDLE_JOB_RULE,
				"poll next job to be announced",
				"when there are jobs in the queue, RMA polls next job");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return !agentProps.getOwnedActiveServers().isEmpty() && canPollNextJobs();

	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		agentProps.setLastPollingTime(now());

		final List<ClientJob> jobsToAllocate = new ArrayList<>();
		agentProps.getJobsToBeExecuted().drainTo(jobsToAllocate, agentProps.getPollingBatchSize());

		final List<ClientJob> jobsWithinTime = jobsToAllocate.stream()
				.map(job -> checkTimeFrames(job, facts))
				.map(Optional::ofNullable)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
		controller.fire(constructFactsForJobsAllocationInitiation(facts.get(RULE_SET_IDX), jobsWithinTime));
	}

	private ClientJob checkTimeFrames(final ClientJob job, final RuleSetFacts facts) {
		facts.put(RULE_SET_IDX, agentProps.getRuleSetForJob().get(job.getJobInstanceId()));
		final RuleSetFacts timeFrameFacts = constructFactsForJobDeadlineVerification(facts.get(RULE_SET_IDX), job);
		controller.fire(timeFrameFacts);

		return timeFrameFacts.get(JOB);
	}

	private boolean canPollNextJobs() {
		return agentProps.getJobsToBeExecuted().size() >= agentProps.getPollingBatchSize() ||
				(!agentProps.getJobsToBeExecuted().isEmpty() &&
						between(agentProps.getLastPollingTime(), now()).toSeconds() > 3);
	}

	@Override
	public AgentRule copy() {
		return new ProcessPollNextClientJobAllocationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
