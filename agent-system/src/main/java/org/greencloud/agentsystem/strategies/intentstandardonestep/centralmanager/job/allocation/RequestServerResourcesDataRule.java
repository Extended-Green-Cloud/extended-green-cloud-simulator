package org.greencloud.agentsystem.strategies.intentstandardonestep.centralmanager.job.allocation;

import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.JOBS_MAP;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.ALLOCATION_PARAMETERS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.CLUSTER_EXECUTORS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.CLUSTER_NO_JOBS;
import static org.greencloud.commons.constants.resource.ResourceCommonKnowledgeConstants.MINIMAL_RELIABILITY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ID;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.RELIABILITY;
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
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.domain.allocation.ImmutableIntentBasedAllocationData;
import org.greencloud.commons.domain.allocation.IntentBasedAllocationData;
import org.greencloud.commons.domain.job.basic.ClientJob;
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

		final List<Map<String, Object>> jobResources = jobs.stream()
				.map(this::mapJobResources)
				.toList();
		final List<String> uniqueResourceTypes = informs.stream()
				.map(rmaResponse -> readMessageContent(rmaResponse, RegionResources.class).getServersResources())
				.flatMap(resources -> resources.values().stream()
						.flatMap(entry -> entry.getResources().keySet().stream()))
				.toList();

		final List<Map<String, Object>> serversResources = informs.stream()
				.map(rmaResponse -> readMessageContent(rmaResponse, RegionResources.class))
				.map(RegionResources::getServersResources)
				.flatMap(serverResourcesMap -> serverResourcesMap.entrySet().stream())
				.map(serverResourceEntry -> mapServerResources(serverResourceEntry, uniqueResourceTypes))
				.toList();
		final Map<String, List<String>> serversPerRMA = informs.stream()
				.collect(toMap(response -> response.getSender().getName(), this::getServersPerRMA));

		final Map<String, Object> allocationParameters = agentProps.getSystemKnowledge().get(ALLOCATION_PARAMETERS);
		final IntentBasedAllocationData allocationData = ImmutableIntentBasedAllocationData.builder()
				.jobResources(jobResources)
				.executorsResources(serversResources)
				.serversPerRMA(serversPerRMA)
				.clusterNoExecutors((int) allocationParameters.get(CLUSTER_EXECUTORS))
				.clusterNoJobs((int) allocationParameters.get(CLUSTER_NO_JOBS))
				.build();

		controller.fire(constructFactsForJobsAllocationPreparation(facts.get(RULE_SET_IDX), jobs, allocationData));
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

		jobResources.put(ID, job.getJobId());
		jobResources.put(DURATION, job.getDuration());
		jobResources.put(RELIABILITY,
				agentProps.getSystemKnowledge().get(ALLOCATION_PARAMETERS).get(MINIMAL_RELIABILITY));
		return jobResources;
	}

	private Map<String, Object> mapServerResources(final Map.Entry<String, ServerResources> serverResources,
			final List<String> uniqueResourceTypes) {
		final Map<String, Object> resources = serverResources.getValue().getResources().entrySet().stream()
				.collect(toMap(Map.Entry::getKey,
						entry -> entry.getValue().getCharacteristics().containsKey(AMOUNT) ?
								entry.getValue().getAmountInCommonUnit() :
								entry.getValue().getCharacteristics().get(JOBS_MAP).getValue()));

		uniqueResourceTypes.forEach(type -> resources.putIfAbsent(type, 0));
		resources.put(ID, serverResources.getKey());
		return resources;
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
