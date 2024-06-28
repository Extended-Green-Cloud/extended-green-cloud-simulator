package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.regionalmanager.job.allocation.processing;

import static java.lang.String.valueOf;
import static org.greencloud.agentsystem.strategies.algorithms.allocation.ResourceAllocator.allocate;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION_TIMER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForUnsuccessfulDataAllocationPreparation;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJobs;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.allocation.AllocationData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.timer.Timer;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class ProcessServerSecondAllocationRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessServerSecondAllocationRule.class);

	public ProcessServerSecondAllocationRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_NEW_JOB_ALLOCATION_RULE,
				"handle next jobs batch allocation",
				"handles allocation of next job batch between Servers");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);

		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Performing the allocation of the jobs to the servers.");

		final AllocationData allocationData = facts.get(RESULT);
		final Map<String, List<String>> allocatedJobs = allocate(allocationData, agentProps);
		final long allocationTime = ((Timer) facts.get(ALLOCATION_TIMER)).stopTimeMeasure(getCurrentTime());

		agentNode.reportJobAllocationPercentage(jobs, allocatedJobs, allocationTime, agentProps.getAgentName());
		controller.fire(
				constructFactsForUnsuccessfulDataAllocationPreparation(facts.get(RULE_SET_IDX), allocatedJobs, jobs));
		allocatedJobs.forEach((serverName, jobsAllocatedToServer) ->
				initiateJobsAllocationRequest(facts, mapToJobs(jobsAllocatedToServer, jobs), serverName));
	}

	private void initiateJobsAllocationRequest(final RuleSetFacts facts, final List<ClientJob> jobs,
			final String serverName) {
		final AID server = agentProps.getServerByName(serverName);
		final RuleSetFacts allocationFacts = constructFactsWithJobs(facts.get(RULE_SET_IDX), jobs);
		allocationFacts.put(AGENT, server);

		agent.addBehaviour(InitiateRequest.create(agent, allocationFacts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
	}

	private List<ClientJob> mapToJobs(final List<String> jobNames, final List<ClientJob> jobs) {
		return jobs.stream().filter(job -> jobNames.contains(job.getJobId())).toList();
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerSecondAllocationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
