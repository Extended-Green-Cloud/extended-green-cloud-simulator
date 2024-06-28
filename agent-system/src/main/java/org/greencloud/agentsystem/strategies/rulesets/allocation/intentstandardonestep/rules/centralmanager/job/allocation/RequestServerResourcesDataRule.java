package org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.centralmanager.job.allocation;

import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
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
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobs;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForJobsAllocationPreparation;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.ALLOCATION_DATA_REQUEST;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.agent.RegionResources;
import org.greencloud.commons.domain.allocation.ImmutableIntentBasedAllocationData;
import org.greencloud.commons.domain.allocation.IntentBasedAllocationData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.allocation.AllocationModificationEnum;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

@SuppressWarnings("unchecked")
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
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withMessageProtocol(ALLOCATION_DATA_REQUEST)
				.withObjectContent(mapToAllocatedJobs(facts.get(JOBS)))
				.withReceivers(agentProps.getAvailableRegionalManagers())
				.build();
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);
		final List<RegionResources> regionResources = informs.stream()
				.map(rmaResponse -> readMessageContent(rmaResponse, RegionResources.class))
				.toList();
		final List<Map<String, Object>> serversResources = agentProps.getServerResourcesFromRMAsData(regionResources);

		final List<Map<String, Object>> jobResources = jobs.stream()
				.map(this::mapJobResources)
				.toList();
		final Map<String, List<String>> serversPerRMA = informs.stream()
				.collect(toMap(response -> response.getSender().getName(), this::getServersPerRMA));

		final Map<String, Object> allocationParameters = agentProps.getSystemKnowledge().get(ALLOCATION_PARAMETERS);
		final List<AllocationModificationEnum> modifications =
				((List<String>)  allocationParameters.getOrDefault(MODIFICATIONS, emptyList())).stream()
						.map(AllocationModificationEnum::valueOf)
						.toList();

		final IntentBasedAllocationData allocationData = ImmutableIntentBasedAllocationData.builder()
				.modifications(modifications)
				.jobResources(jobResources)
				.executorsResources(serversResources)
				.serversPerRMA(serversPerRMA)
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

	private List<String> getServersPerRMA(final ACLMessage rmaResponse) {
		return readMessageContent(rmaResponse, RegionResources.class).getServersResources().keySet().stream().toList();
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
