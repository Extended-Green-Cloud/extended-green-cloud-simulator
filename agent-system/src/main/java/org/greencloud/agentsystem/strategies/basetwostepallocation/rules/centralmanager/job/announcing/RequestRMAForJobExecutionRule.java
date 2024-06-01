package org.greencloud.agentsystem.strategies.basetwostepallocation.rules.centralmanager.job.announcing;

import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJob;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CMA_JOB_ALLOCATION_PROTOCOl;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareExecutionRequest;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class RequestRMAForJobExecutionRule extends AgentRequestRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(RequestRMAForJobExecutionRule.class);

	public RequestRMAForJobExecutionRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
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
				CMA_JOB_ALLOCATION_PROTOCOl);
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		final AllocatedJobs acceptedJobs = readMessageContent(inform, AllocatedJobs.class);
		final AID rma = facts.get(AGENT);
		handleAcceptedJobs(acceptedJobs, facts, rma);
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		final AllocatedJobs jobs = readMessageContent(refuse, AllocatedJobs.class);
		final AID rma = facts.get(AGENT);

		requireNonNull(jobs.getRejectedAllocationJobs()).forEach(job -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("RMA {} refused execution of job {}.", rma.getLocalName(), job.getJobId());

			final RuleSetFacts failureFacts = constructFactsWithJob(facts.get(RULE_SET_IDX), mapToClientJob(job));
			failureFacts.put(RULE_TYPE, LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE);
			controller.fire(failureFacts);
		});
		handleAcceptedJobs(jobs, facts, rma);
	}

	private void handleAcceptedJobs(final AllocatedJobs acceptedJobs, final RuleSetFacts facts, final AID rma) {
		acceptedJobs.getAllocationJobs().forEach(job -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("RMA {} accepted execution of job {}.", rma.getLocalName(), job.getJobId());

			agentProps.getClientJobs().replace(mapToClientJob(job), PROCESSING, ACCEPTED);
			agentProps.getRmaForJobMap().put(job.getJobId(), rma);
		});
	}

	@Override
	public AgentRule copy() {
		return new RequestRMAForJobExecutionRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
