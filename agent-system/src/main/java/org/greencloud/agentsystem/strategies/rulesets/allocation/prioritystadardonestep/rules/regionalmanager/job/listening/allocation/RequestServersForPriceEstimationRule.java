package org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep.rules.regionalmanager.job.listening.allocation;

import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.JOBS_MAP;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ENERGY;
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
import org.greencloud.commons.domain.agent.ServerPriceEstimation;
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

public class RequestServersForPriceEstimationRule extends AgentRequestRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(RequestServersForPriceEstimationRule.class);

	public RequestServersForPriceEstimationRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE,
				"sends request for jobs price estimation to servers.",
				"when next jobs are to be allocated, RMA asks servers to estimate their prices.");
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
		logger.info("Preparing information about regional resources containing job prices.");

		final List<ClientJobWithServer> jobs = ((ImmutableAllocatedJobs) facts.get(JOBS)).getAllocationJobs();
		final Map<String, ServerResources> serversResources = informs.stream()
				.collect(toMap(msg -> msg.getSender().getName(), msg -> mapToServerResources(msg, jobs)));
		final RegionResources resources = ImmutableRegionResources.builder().serversResources(serversResources).build();

		agent.send(prepareReply(facts.get(MESSAGE), resources, facts.get(MESSAGE_TYPE)));
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Server {} refused to execute given job.", refuse.getSender().getLocalName());
	}

	private ServerResources mapToServerResources(final ACLMessage serverResponse,
			final List<ClientJobWithServer> jobs) {
		final AID server = serverResponse.getSender();
		final ServerPriceEstimation serverData = readMessageContent(serverResponse, ServerPriceEstimation.class);
		final Map<String, Resource> serverResources =
				new HashMap<>(agentProps.getOwnedServerResources().get(server).getResources());

		serverResources.put(ENERGY, constructEnergyResource(serverData));
		serverResources.put(SUFFICIENCY, constructJobFitnessEstimation(jobs, serverResources));
		serverResources.put(BUDGET, constructBudgetResource(serverData));

		return ImmutableServerResources.builder().resources(serverResources).build();
	}

	private Resource constructEnergyResource(final ServerPriceEstimation serverData) {
		final ResourceCharacteristic energyCharacteristic =
				ImmutableResourceCharacteristic.builder()
						.value(requireNonNull(serverData.getAverageGreenEnergyUtilization())).build();

		return ImmutableResource.builder()
				.putCharacteristics(JOBS_MAP, energyCharacteristic)
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

	private Resource constructBudgetResource(final ServerPriceEstimation prices) {
		final ResourceCharacteristic budgetCharacteristic =
				ImmutableResourceCharacteristic.builder().value(prices.getJobsPrices()).build();

		return ImmutableResource.builder()
				.putCharacteristics(JOBS_MAP, budgetCharacteristic)
				.build();
	}

	@Override
	public AgentRule copy() {
		return new RequestServersForPriceEstimationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
