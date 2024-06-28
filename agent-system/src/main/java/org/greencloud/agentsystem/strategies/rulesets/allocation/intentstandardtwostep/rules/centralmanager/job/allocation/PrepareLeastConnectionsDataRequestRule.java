package org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardtwostep.rules.centralmanager.job.allocation;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION_TIMER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.ALLOCATION_PARAMETERS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.MODIFICATIONS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ALLOCATION_REQUEST_DATA;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForJobsAllocationPreparation;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJobs;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.allocation.ImmutableLeastConnectionAllocationData;
import org.greencloud.commons.domain.allocation.LeastConnectionAllocationData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.allocation.AllocationModificationEnum;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class PrepareLeastConnectionsDataRequestRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(PrepareLeastConnectionsDataRequestRule.class);

	public PrepareLeastConnectionsDataRequestRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ALLOCATION_REQUEST_DATA,
				"method prepares data for LC allocation",
				"gathers information about RMA connections.");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);
		final int ruleSetId = facts.get(RULE_SET_IDX);
		final RuleSetFacts requestFacts = constructFactsWithJobs(ruleSetId, jobs);
		requestFacts.put(ALLOCATION_TIMER, facts.get(ALLOCATION_TIMER));

		MDC.put(MDC_RULE_SET_ID, valueOf(ruleSetId));
		logger.info("Preparing information about RMA connections.");

		final List<AID> consideredRMAs = agentProps.getAvailableRegionalManagers();
		final Map<String, Long> rmaConnections = agentProps.getRmaForJobMap().entrySet().stream()
				.filter(entry -> consideredRMAs.stream().map(AID::getName).toList().contains(entry.getKey()))
				.collect(groupingBy(entry -> entry.getValue().getName(), counting()));
		final Map<String, Object> allocationParameters = agentProps.getSystemKnowledge().get(ALLOCATION_PARAMETERS);
		final List<AllocationModificationEnum> modifications =
				((List<String>) allocationParameters.getOrDefault(MODIFICATIONS, emptyList())).stream()
						.map(AllocationModificationEnum::valueOf)
						.toList();

		final LeastConnectionAllocationData allocationData = ImmutableLeastConnectionAllocationData.builder()
				.jobsToAllocate(jobs)
				.rMAConnections(rmaConnections)
				.modifications(modifications)
				.build();

		controller.fire(constructFactsForJobsAllocationPreparation(facts.get(RULE_SET_IDX), jobs, allocationData,
				facts.get(ALLOCATION_TIMER)));
	}

	@Override
	public AgentRule copy() {
		return new PrepareLeastConnectionsDataRequestRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
