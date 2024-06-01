package org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.announcing;

import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.RMA_JOB_ALLOCATION_PROTOCOl;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareExecutionRequest;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class RequestServerForJobExecutionRule extends AgentRequestRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(RequestServerForJobExecutionRule.class);

	public RequestServerForJobExecutionRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_RULE,
				"initiate request in Regional Manager Agents",
				"when next jobs are allocated, it sends a request to RMA");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		return prepareExecutionRequest(facts.get(JOBS),
				facts.get(AGENT),
				facts.get(RULE_SET_IDX),
				RMA_JOB_ALLOCATION_PROTOCOl);
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		final AllocatedJobs acceptedJobs = readMessageContent(inform, AllocatedJobs.class);
		final AID server = facts.get(AGENT);
		handleAcceptedJobs(acceptedJobs, facts, server);
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		final AllocatedJobs jobs = readMessageContent(refuse, AllocatedJobs.class);
		final AID server = facts.get(AGENT);

		requireNonNull(jobs.getRejectedAllocationJobs()).forEach(job -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Server refused to execute the job. Putting the job back to the queue.");

			agentProps.getJobsToBeExecuted().add(mapToClientJob(job));
		});
		handleAcceptedJobs(jobs, facts, server);
	}

	private void handleAcceptedJobs(final AllocatedJobs acceptedJobs, final RuleSetFacts facts, final AID server) {
		acceptedJobs.getAllocationJobs().forEach(job -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Server accepted execution of the job {}.", job.getJobId());

			agentProps.getServerForJobMap().put(job.getJobId(), server);
		});
	}

	@Override
	public AgentRule copy() {
		return new RequestServerForJobExecutionRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
