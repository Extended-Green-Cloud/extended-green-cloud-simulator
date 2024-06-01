package org.greencloud.agentsystem.strategies.baseonestepallocation.rules.regionalmanager.job.listening.newjob;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobs;
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobsWithRejection;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.greencloud.commons.utils.allocation.AllocationUtils.verifyJobsForAllocation;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJobs;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobVerification;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessNewJobsServerAllocationRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	public ProcessNewJobsServerAllocationRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handles new jobs allocation",
				"rule run when RMA processes new jobs allocation received from CMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final AllocatedJobs allocation = facts.get(MESSAGE_CONTENT);
		return !allocation.getAllocationJobs().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage receivedMsg = facts.get(MESSAGE);
		final int index = controller.getLatestLongTermRuleSetIdx().get();

		verifyJobsForAllocation(
				facts,
				index,
				agent.getName(),
				performVerification(receivedMsg, index),
				passJobsExecutionDecision(receivedMsg, index)
		);
	}

	private Function<ClientJobWithServer, RuleSetFacts> performVerification(final ACLMessage message, final int index) {
		return job -> {
			agentProps.addJob(mapToClientJob(job), index, PROCESSING);
			final RuleSetFacts verifierFacts = constructFactsForJobVerification(index, job, message);
			controller.fire(verifierFacts);
			return verifierFacts;
		};
	}

	private BiConsumer<List<ClientJobWithServer>, List<ClientJobWithServer>> passJobsExecutionDecision(
			final ACLMessage message, final int index) {
		return (acceptedJobs, refusedJobs) -> {
			if (refusedJobs.isEmpty()) {
				agent.send(prepareReply(message, mapToAllocatedJobs(acceptedJobs), INFORM));
			} else {
				agent.send(prepareReply(message, mapToAllocatedJobsWithRejection(acceptedJobs, refusedJobs), REFUSE));
			}

			final Map<String, List<ClientJobWithServer>> serversAssignedPerJob =
					acceptedJobs.stream().collect(groupingBy(job -> requireNonNull(job.getServer())));

			serversAssignedPerJob.forEach((server, jobs) -> {
				final AID serverAgent = agentProps.getServerByName(server);
				final RuleSetFacts allocationFacts = constructFactsWithJobs(index, jobs);
				allocationFacts.put(AGENT, serverAgent);

				agent.addBehaviour(
						InitiateRequest.create(agent, allocationFacts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
			});
		};

	}

	@Override
	public AgentRule copy() {
		return new ProcessNewJobsServerAllocationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
