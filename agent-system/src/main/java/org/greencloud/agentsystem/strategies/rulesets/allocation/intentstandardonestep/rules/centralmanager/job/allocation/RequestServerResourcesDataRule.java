package org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.centralmanager.job.allocation;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION_TIMER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.ALLOCATION_PARAMETERS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.CLUSTER_EXECUTORS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.CLUSTER_NO_JOBS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.MINIMAL_GREEN_ENERGY_UTILIZATION;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.MINIMAL_RELIABILITY;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.MODIFICATIONS;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ENERGY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ID;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.RELIABILITY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.TYPE;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.ENERGY_PREFERENCE;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.NO_MODIFICATION;
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
import java.util.Map;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.allocation.ImmutableIntentBasedAllocationData;
import org.greencloud.commons.domain.allocation.IntentBasedAllocationData;
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

public class RequestServerResourcesDataRule extends AgentRequestRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(RequestServerResourcesDataRule.class);

	public RequestServerResourcesDataRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE,
				"sends request for resource allocation data to Regional Manager Agents",
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

		final List<Map<String, Object>> jobResources = jobs.stream()
				.map(this::mapJobResources)
				.toList();
		final Map<String, Object> allocationParameters = agentProps.getSystemKnowledge().get(ALLOCATION_PARAMETERS);

		final IntentBasedAllocationData allocationData = ImmutableIntentBasedAllocationData.builder()
				.modifications(agentProps.getModifications())
				.jobResources(jobResources)
				.executorsResources(factsParser.get(RESULT))
				.serversPerRMA(factsParser.get(AGENTS))
				.clusterNoExecutors((int) allocationParameters.get(CLUSTER_EXECUTORS))
				.clusterNoJobs((int) allocationParameters.get(CLUSTER_NO_JOBS))
				.build();

		controller.fire(constructFactsForJobsAllocationPreparation(facts.get(RULE_SET_IDX), jobs, allocationData,
				facts.get(ALLOCATION_TIMER)));
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("RMA {} refused to send resources for job execution.", refuse.getSender().getLocalName());
	}

	private Map<String, Object> mapJobResources(final ClientJob job) {
		final Map<String, Object> jobResources = job.getRequiredResources().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getAmountInCommonUnit()));
		final Map<String, Object> allocationParameters = agentProps.getSystemKnowledge().get(ALLOCATION_PARAMETERS);

		jobResources.put(TYPE, job.getJobType());
		jobResources.put(ID, job.getJobId());
		jobResources.put(DURATION, job.getDuration());
		jobResources.put(RELIABILITY, allocationParameters.get(MINIMAL_RELIABILITY));

		if (allocationParameters.getOrDefault(MODIFICATIONS, NO_MODIFICATION).equals(ENERGY_PREFERENCE)) {
			jobResources.put(ENERGY, allocationParameters.get(MINIMAL_GREEN_ENERGY_UTILIZATION));
		}

		return jobResources;
	}

	@Override
	public AgentRule copy() {
		return new RequestServerResourcesDataRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
