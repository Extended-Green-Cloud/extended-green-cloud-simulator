package org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.announcing.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.CREATED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessLookForRMAForJobExecutionFailureRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ProcessLookForRMAForJobExecutionFailureRule.class);
	private static final Integer MAX_RETRIES = 10;

	private final ConcurrentHashMap<String, AtomicInteger> retryCounter;

	public ProcessLookForRMAForJobExecutionFailureRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller, 3);
		this.retryCounter = new ConcurrentHashMap<>();
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE,
				"handle cases when there is no RMA for job execution",
				"rule provides common handler for cases when there are no candidates to execute the job");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		retryCounter.putIfAbsent(job.getJobId(), new AtomicInteger(0));

		if (!agentProps.isPossiblyAfterDeadline(job) && retryCounter.get(job.getJobId()).get() < MAX_RETRIES) {
			logger.info("All Regional Manager Agents refused to the call for proposal. Putting job back to the queue");

			if (agentProps.getJobsToBeExecuted().offer(job)) {
				retryCounter.get(job.getJobId()).incrementAndGet();
				agentProps.getClientJobs().replace(job, CREATED);
			}
		} else {
			logger.info("All Regional Manager Agents refused to the call for proposal. Sending failure information.");

			final int ruleSetIdx = agentProps.removeJob(job);
			controller.removeRuleSet(agentProps.getRuleSetForJob(), ruleSetIdx);
			agentProps.getRmaForJobMap().remove(job.getJobId());
			agent.send(prepareJobStatusMessageForClient(job, FAILED_JOB_ID, facts.get(RULE_SET_IDX)));
			retryCounter.remove(job.getJobId());
		}
	}

	@Override
	public AgentRule copy() {
		return new ProcessLookForRMAForJobExecutionFailureRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
