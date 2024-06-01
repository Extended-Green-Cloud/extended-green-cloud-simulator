package org.greencloud.agentsystem.strategies.deafult.rules.centralmanager.job.listening.newjob.processing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.CREATED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ADD_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.greencloud.commons.utils.rules.CustomRulesConstructor.constructRuleSetForCustomClientComparison;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rest.domain.RuleSetRest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSet;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessNewClientJobAddJobRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ProcessNewClientJobAddJobRule.class);

	public ProcessNewClientJobAddJobRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ADD_JOB_RULE,
				"add new job after preprocessing to the queue",
				"rule handles adding next job to the queue after its preprocessing");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		return agentProps.getJobsToBeExecuted().offer(job);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		int newRuleSetIdx = facts.get(RULE_SET_IDX);

		if (nonNull(job.getSelectionPreference())) {
			final String log = "Comparing RMA offers using custom comparator";
			final String ruleSetName = "CUSTOM_CLIENT_COMPARATOR_" + job.getClientIdentifier().toUpperCase();
			final RuleSetRest rules = constructRuleSetForCustomClientComparison(job.getSelectionPreference(),
					ruleSetName, log, job.getJobId(), CENTRAL_MANAGER);
			newRuleSetIdx = controller.getLatestRuleSetIdx().get() + 1;

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf(newRuleSetIdx));
			logger.info("Client {} requested to use custom server comparison. Adding rule set {}",
					job.getClientIdentifier(), ruleSetName);

			final RuleSet modifications = new RuleSet(rules);
			controller.addModifiedTemporaryRuleSetFromCurrent(modifications, newRuleSetIdx);
		}
		agentProps.addJob(job, newRuleSetIdx, CREATED);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf(newRuleSetIdx));
		logger.info("Job {} has been successfully added to job scheduling queue", job.getJobId());

		agentNode.updateScheduledJobQueue(agentProps);
		agent.send(prepareJobStatusMessageForClient(job, SCHEDULED_JOB_ID, newRuleSetIdx));
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewClientJobAddJobRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
