package org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.newjob;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobs;
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobsWithRejection;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.greencloud.commons.utils.allocation.AllocationUtils.verifyJobsForAllocation;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobVerification;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

public class ProcessNewJobsAllocationRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	public ProcessNewJobsAllocationRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
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
				passJobsExecutionDecision(receivedMsg)
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
			final ACLMessage message) {
		return (acceptedJobs, refusedJobs) -> {
			if (refusedJobs.isEmpty()) {
				agent.send(prepareReply(message, mapToAllocatedJobs(acceptedJobs), INFORM));
			} else {
				agent.send(prepareReply(message, mapToAllocatedJobsWithRejection(acceptedJobs, refusedJobs), REFUSE));
			}
		};

	}

	@Override
	public AgentRule copy() {
		return new ProcessNewJobsAllocationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
