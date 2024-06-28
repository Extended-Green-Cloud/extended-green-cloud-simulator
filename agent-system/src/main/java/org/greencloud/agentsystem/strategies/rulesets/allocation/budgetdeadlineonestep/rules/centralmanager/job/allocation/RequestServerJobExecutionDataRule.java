package org.greencloud.agentsystem.strategies.rulesets.allocation.budgetdeadlineonestep.rules.centralmanager.job.allocation;

import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION_TIMER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.VALUE;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.ALLOCATION_PARAMETERS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.MODIFICATIONS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.TYPES_ENCODING;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.START_TIME;
import static org.greencloud.commons.enums.allocation.AllocationModificationEnum.PRE_CLUSTERED_RESOURCES;
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
import org.greencloud.commons.domain.agent.ImmutableServerResources;
import org.greencloud.commons.domain.agent.RegionResources;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.domain.allocation.BudgetDealdineAllocationData;
import org.greencloud.commons.domain.allocation.ImmutableBudgetDealdineAllocationData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithExecutionEstimation;
import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;
import org.greencloud.commons.domain.resources.Resource;
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
public class RequestServerJobExecutionDataRule extends AgentRequestRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(RequestServerJobExecutionDataRule.class);

	public RequestServerJobExecutionDataRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE,
				"sends request for job execution data to Regional Manager Agents",
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
		final Map<String, JobWithExecutionEstimation> serversEstimations = regionResources.stream()
				.flatMap(resources -> mapExecutionEstimations(resources.getServersResources()).entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		final Map<String, ServerResources> serversResources = regionResources.stream()
				.flatMap(resources -> mapServerResources(resources.getServersResources()).entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		final Map<String, List<String>> serversPerRMA = informs.stream()
				.collect(toMap(response -> response.getSender().getName(), this::getServersPerRMA));
		final List<AllocationModificationEnum> modifications =
				((List<String>) agentProps.getSystemKnowledge().get(ALLOCATION_PARAMETERS)
						.getOrDefault(MODIFICATIONS, emptyList())).stream()
						.map(AllocationModificationEnum::valueOf)
						.toList();

		final BudgetDealdineAllocationData allocationData = ImmutableBudgetDealdineAllocationData.builder()
				.modifications(modifications)
				.jobForAllocation(jobs.getFirst())
				.jobPriority(modifications.contains(PRE_CLUSTERED_RESOURCES) ?
						getPriorityFromCommonKnowledge(jobs.getFirst()) :
						agentProps.getPriorityPerJob().get(jobs.getFirst().getJobId()))
				.executorsEstimations(serversEstimations)
				.executorsResources(serversResources)
				.serversPerRMA(serversPerRMA)
				.build();

		controller.fire(constructFactsForJobsAllocationPreparation(facts.get(RULE_SET_IDX), jobs, allocationData,
				facts.get(ALLOCATION_TIMER)));
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("RMA {} refused to send resources for job execution.", refuse.getSender().getLocalName());
	}

	private double getPriorityFromCommonKnowledge(final ClientJob job) {
		final String jobType = job.getJobType();

		return (double) ((Map<String, Integer>) agentProps.getSystemKnowledge().get(ALLOCATION_PARAMETERS)
				.getOrDefault(TYPES_ENCODING, emptyMap()))
				.getOrDefault(jobType, 0);
	}

	private List<String> getServersPerRMA(final ACLMessage rmaResponse) {
		return readMessageContent(rmaResponse, RegionResources.class).getServersResources().keySet().stream().toList();
	}

	private Map<String, ServerResources> mapServerResources(final Map<String, ServerResources> resources) {
		return resources.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> {
					final Map<String, Resource> resourceMap = entry.getValue().getResources().entrySet().stream()
							.filter(resource -> !List.of(BUDGET, DURATION, START_TIME).contains(resource.getKey()))
							.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
					return ImmutableServerResources.builder().resources(resourceMap).build();
				}));
	}

	private Map<String, JobWithExecutionEstimation> mapExecutionEstimations(
			final Map<String, ServerResources> resources) {
		return resources.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> {
					final Map<String, Resource> resourceMap = entry.getValue().getResources();
					final long epochSeconds = ((Double) resourceMap.get(START_TIME).getCharacteristics()
							.get(VALUE).getValue()).longValue();
					return ImmutableJobWithExecutionEstimation.builder()
							.estimatedPrice(resourceMap.get(BUDGET).getAmountInCommonUnit())
							.estimatedDuration(resourceMap.get(DURATION).getAmountInCommonUnit().longValue())
							.earliestStartTime(ofEpochSecond(epochSeconds))
							.build();
				}));
	}

	@Override
	public AgentRule copy() {
		return new RequestServerJobExecutionDataRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
