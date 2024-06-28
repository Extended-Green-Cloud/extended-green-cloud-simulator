package org.greencloud.agentsystem.strategies.rulesets.allocation.intentstandardonestep.rules.regionalmanager.job.listening.allocation;

import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.JOBS_MAP;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ENERGY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.RELIABILITY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.SUFFICIENCY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobs;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.ALLOCATION_DATA_REQUEST;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.agent.ImmutableRegionResources;
import org.greencloud.commons.domain.agent.ImmutableServerResources;
import org.greencloud.commons.domain.agent.RegionResources;
import org.greencloud.commons.domain.agent.ServerJobsEstimation;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.domain.allocation.ImmutableAllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.domain.resources.ResourceCharacteristic;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class RequestServersForJobsExecutionEstimationRule extends AgentRequestRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(RequestServersForJobsExecutionEstimationRule.class);

	public RequestServersForJobsExecutionEstimationRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE,
				"sends request for jobs execution estimation to servers.",
				"when next jobs are to be allocated, RMA asks servers to estimate their execution.");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withMessageProtocol(ALLOCATION_DATA_REQUEST)
				.withObjectContent(mapToAllocatedJobs(((ImmutableAllocatedJobs) facts.get(JOBS)).getAllocationJobs()))
				.withReceivers(agentProps.getOwnedActiveServers())
				.build();
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final RuleSetFacts facts) {
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Preparing information about regional resources.");

		final List<ClientJobWithServer> jobs = ((ImmutableAllocatedJobs) facts.get(JOBS)).getAllocationJobs();
		final Map<String, ServerResources> serversResources = informs.stream()
				.collect(toMap(msg -> msg.getSender().getName(), msg -> mapToServerResources(jobs, msg)));
		final RegionResources resources = ImmutableRegionResources.builder().serversResources(serversResources).build();

		agent.send(prepareReply(facts.get(MESSAGE), resources, facts.get(MESSAGE_TYPE)));
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Server {} refused to execute given job.", refuse.getSender().getLocalName());
	}

	private ServerResources mapToServerResources(final List<ClientJobWithServer> jobs,
			final ACLMessage serverResponse) {
		final AID server = serverResponse.getSender();
		final ServerJobsEstimation serverData = readMessageContent(serverResponse, ServerJobsEstimation.class);
		final Map<String, Resource> serverResources =
				new HashMap<>(agentProps.getOwnedServerResources().get(server).getResources());

		serverResources.put(ENERGY, constructEnergyResource(serverData));
		serverResources.put(RELIABILITY, constructReliabilityResource(serverData));
		serverResources.put(DURATION, constructDurationResource(serverData));
		serverResources.put(BUDGET, constructBudgetResource(serverData));
		serverResources.put(SUFFICIENCY, constructJobFitnessEstimation(jobs, serverResources));

		return ImmutableServerResources.builder().resources(serverResources).build();
	}

	private Resource constructEnergyResource(final ServerJobsEstimation serverData) {
		final ResourceCharacteristic energyCharacteristic =
				ImmutableResourceCharacteristic.builder().value(serverData.getAverageGreenEnergyUtilization()).build();

		return ImmutableResource.builder()
				.putCharacteristics(JOBS_MAP, energyCharacteristic)
				.build();
	}

	private Resource constructReliabilityResource(final ServerJobsEstimation serverData) {
		final ResourceCharacteristic reliabilityCharacteristic =
				ImmutableResourceCharacteristic.builder().value(serverData.getServerReliability()).build();

		return ImmutableResource.builder()
				.putCharacteristics(JOBS_MAP, reliabilityCharacteristic)
				.build();
	}

	private Resource constructDurationResource(final ServerJobsEstimation serverData) {
		final Map<String, Long> jobsDurationMap = serverData.getJobsEstimation().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getEstimatedDuration()));

		final ResourceCharacteristic durationCharacteristic =
				ImmutableResourceCharacteristic.builder().value(jobsDurationMap).build();

		return ImmutableResource.builder()
				.putCharacteristics(JOBS_MAP, durationCharacteristic)
				.build();
	}

	private Resource constructBudgetResource(final ServerJobsEstimation serverData) {
		final Map<String, Double> jobsBudgetMap = serverData.getJobsEstimation().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getEstimatedPrice()));

		final ResourceCharacteristic budgetCharacteristic =
				ImmutableResourceCharacteristic.builder().value(jobsBudgetMap).build();

		return ImmutableResource.builder()
				.putCharacteristics(JOBS_MAP, budgetCharacteristic)
				.build();
	}

	private Resource constructJobFitnessEstimation(final List<ClientJobWithServer> jobs,
			final Map<String, Resource> serverResources) {
		final Map<String, Boolean> jobFitnessMap = jobs.stream().
				collect(toMap(ClientJob::getJobId, job -> areSufficient(serverResources, job.getRequiredResources())));

		final ResourceCharacteristic jobFitnessCharacteristic =
				ImmutableResourceCharacteristic.builder().value(jobFitnessMap).build();

		return ImmutableResource.builder()
				.putCharacteristics(JOBS_MAP, jobFitnessCharacteristic)
				.build();
	}

	@Override
	public AgentRule copy() {
		return new RequestServersForJobsExecutionEstimationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
