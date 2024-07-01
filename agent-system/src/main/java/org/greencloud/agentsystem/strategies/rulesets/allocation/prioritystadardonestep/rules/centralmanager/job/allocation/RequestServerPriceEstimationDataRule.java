package org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep.rules.centralmanager.job.allocation;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION_TIMER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForJobsAllocationPreparation;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForRMADataParsing;
import static org.greencloud.commons.utils.messaging.factory.RequestMessageFactory.requestDataForAllocation;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENTS;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.allocation.ImmutablePriorityBasedAllocationData;
import org.greencloud.commons.domain.allocation.PriorityBasedAllocationData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class RequestServerPriceEstimationDataRule extends AgentRequestRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(RequestServerPriceEstimationDataRule.class);

	public RequestServerPriceEstimationDataRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE,
				"sends request for resources data to Regional Manager Agents",
				"when next jobs are allocated, it sends a request to RMA");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		return requestDataForAllocation(facts, agentProps.getAvailableRegionalManagers());
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);
		final RuleSetFacts factsParser = constructFactsForRMADataParsing(facts.get(RULE_SET_IDX), informs);
		controller.fire(factsParser);

		final PriorityBasedAllocationData allocationData = ImmutablePriorityBasedAllocationData.builder()
				.modifications(agentProps.getModifications())
				.jobsToAllocate(jobs)
				.executorsResources(facts.get(RESULT))
				.serversPerRMA(facts.get(AGENTS))
				.build();

		controller.fire(constructFactsForJobsAllocationPreparation(facts.get(RULE_SET_IDX), jobs, allocationData,
				facts.get(ALLOCATION_TIMER)));
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("RMA {} refused to send resources for job execution.", refuse.getSender().getLocalName());
	}

	@Override
	public AgentRule copy() {
		return new RequestServerPriceEstimationDataRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
