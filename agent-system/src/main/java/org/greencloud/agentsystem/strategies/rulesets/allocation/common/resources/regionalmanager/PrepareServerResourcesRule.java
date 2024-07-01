package org.greencloud.agentsystem.strategies.rulesets.allocation.common.resources.regionalmanager;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION_DATA;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.AMOUNT;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.JOBS_MAP;
import static org.greencloud.commons.constants.resource.ResourceCharacteristicConstants.VALUE;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ENERGY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.RELIABILITY;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.START_TIME;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.SUFFICIENCY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_SERVER_RESOURCES_RULE;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGES;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.agent.ImmutableRegionResources;
import org.greencloud.commons.domain.agent.ImmutableServerResources;
import org.greencloud.commons.domain.agent.RegionResources;
import org.greencloud.commons.domain.agent.ServerJobsEstimation;
import org.greencloud.commons.domain.agent.ServerPriceEstimation;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.domain.allocation.ImmutableAllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;
import org.greencloud.commons.domain.resources.ImmutableResource;
import org.greencloud.commons.domain.resources.ImmutableResourceCharacteristic;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.domain.resources.ResourceCharacteristic;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class PrepareServerResourcesRule<T> extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private final Class<T> tClass;
	private final Map<String, Function<T, Resource>> allocationDataMap = Map.of(
			ENERGY, this::constructEnergyResource,
			BUDGET, this::constructBudgetResource,
			RELIABILITY, this::constructReliabilityResource,
			DURATION, this::constructDurationResource,
			START_TIME, this::constructStartTimeResource
	);

	public PrepareServerResourcesRule(final RulesController<RegionalManagerAgentProps, RMANode> controller,
			final Class<T> tClass) {
		super(controller);
		this.tClass = tClass;
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_SERVER_RESOURCES_RULE,
				"prepares map with server resources.",
				"when information about server resources is received, RMA maps it.");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<String> allocationDataFields = facts.get(ALLOCATION_DATA);
		final Collection<ACLMessage> messages = facts.get(MESSAGES);
		final List<ClientJobWithServer> jobs = ((ImmutableAllocatedJobs) facts.get(JOBS)).getAllocationJobs();

		final Map<String, ServerResources> serversResources = messages.stream()
				.collect(toMap(msg -> msg.getSender().getName(), msg -> mapResources(msg, jobs, allocationDataFields)));
		final RegionResources resources = ImmutableRegionResources.builder().serversResources(serversResources).build();

		facts.put(RESULT, resources);
	}

	private ServerResources mapResources(final ACLMessage serverResponse, final List<ClientJobWithServer> jobs,
			final List<String> allocationDataFields) {
		final AID server = serverResponse.getSender();
		final T serverData = readMessageContent(serverResponse, tClass);
		final Map<String, Resource> serverResources =
				new HashMap<>(agentProps.getOwnedServerResources().get(server).getResources());

		if (allocationDataFields.contains(SUFFICIENCY)) {
			serverResources.put(SUFFICIENCY, constructJobFitnessEstimation(jobs, serverResources));
		}

		allocationDataFields.stream()
				.filter(field -> !field.equals(SUFFICIENCY))
				.forEach(field -> serverResources.put(field, allocationDataMap.get(field).apply(serverData)));
		return ImmutableServerResources.builder().resources(serverResources).build();
	}

	private Resource constructEnergyResource(final T serverData) {
		final ResourceCharacteristic energyCharacteristic = switch (serverData) {
			case ServerPriceEstimation priceEstimation -> ImmutableResourceCharacteristic.builder()
					.value(requireNonNull(priceEstimation.getAverageGreenEnergyUtilization())).build();
			case ServerJobsEstimation jobsEstimation -> ImmutableResourceCharacteristic.builder()
					.value(jobsEstimation.getAverageGreenEnergyUtilization()).build();
			default -> ImmutableResourceCharacteristic.builder().value(0.0).build();
		};

		return ImmutableResource.builder()
				.putCharacteristics(JOBS_MAP, energyCharacteristic)
				.build();
	}

	private Resource constructReliabilityResource(final T serverData) {
		final ResourceCharacteristic reliabilityCharacteristic = switch (serverData) {
			case ServerJobsEstimation jobsEstimation -> ImmutableResourceCharacteristic.builder()
					.value(jobsEstimation.getServerReliability()).build();
			default -> ImmutableResourceCharacteristic.builder().value(0.0).build();
		};

		return ImmutableResource.builder()
				.putCharacteristics(JOBS_MAP, reliabilityCharacteristic)
				.build();
	}

	private Resource constructStartTimeResource(final T serverData) {
		final ResourceCharacteristic startTimeCharacteristic = switch (serverData) {
			case JobWithExecutionEstimation jobWithExecutionEstimation -> ImmutableResourceCharacteristic.builder()
					.value(requireNonNull(jobWithExecutionEstimation.getEarliestStartTime())).build();
			default -> ImmutableResourceCharacteristic.builder().value(0.0).build();
		};

		return ImmutableResource.builder()
				.putCharacteristics(VALUE, startTimeCharacteristic)
				.build();
	}

	private Resource constructDurationResource(final T serverData) {
		final ResourceCharacteristic durationCharacteristic = switch (serverData) {
			case JobWithExecutionEstimation jobsEstimation -> ImmutableResourceCharacteristic.builder()
					.value(jobsEstimation.getEstimatedDuration().doubleValue()).build();
			case ServerJobsEstimation jobsEstimation -> {
				final Map<String, Long> jobsDurationMap = jobsEstimation.getJobsEstimation().entrySet().stream()
						.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getEstimatedDuration()));
				yield ImmutableResourceCharacteristic.builder().value(jobsDurationMap).build();
			}
			default -> ImmutableResourceCharacteristic.builder().value(0.0).build();
		};
		final String characteristicName = tClass.equals(JobWithExecutionEstimation.class) ? AMOUNT : JOBS_MAP;

		return ImmutableResource.builder()
				.putCharacteristics(characteristicName, durationCharacteristic)
				.build();
	}

	private Resource constructBudgetResource(final T serverData) {
		final ResourceCharacteristic budgetCharacteristic = switch (serverData) {
			case ServerPriceEstimation priceEstimation -> ImmutableResourceCharacteristic.builder()
					.value(requireNonNull(priceEstimation.getJobsPrices())).build();
			case ServerJobsEstimation jobsEstimation -> {
				final Map<String, Double> jobsBudgetMap = jobsEstimation.getJobsEstimation().entrySet().stream()
						.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getEstimatedPrice()));
				yield ImmutableResourceCharacteristic.builder().value(jobsBudgetMap).build();
			}
			case JobWithExecutionEstimation jobWithExecutionEstimation -> ImmutableResourceCharacteristic.builder()
					.value(jobWithExecutionEstimation.getEstimatedPrice())
					.build();
			default -> ImmutableResourceCharacteristic.builder().value(0.0).build();
		};
		final String characteristicName = tClass.equals(JobWithExecutionEstimation.class) ? AMOUNT : JOBS_MAP;

		return ImmutableResource.builder()
				.putCharacteristics(characteristicName, budgetCharacteristic)
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
		return new PrepareServerResourcesRule<>(controller, tClass);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
