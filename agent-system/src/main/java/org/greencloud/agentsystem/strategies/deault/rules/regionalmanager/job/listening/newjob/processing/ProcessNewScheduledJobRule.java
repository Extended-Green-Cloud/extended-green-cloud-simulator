package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.newjob.processing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.rules.CustomRulesConstructor.constructRuleSetForCustomClientComparison;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.mapper.FactsMapper.mapToRuleSetFacts;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateProposal;
import org.jrba.rulesengine.rest.domain.RuleSetRest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSet;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessNewScheduledJobRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessNewScheduledJobRule.class);

	public ProcessNewScheduledJobRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE,
				"handles new scheduled jobs",
				"rule run when RMA processes new job received from RMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return !agentProps.getOwnedActiveServers().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		facts.put(RULE_SET_IDX, controller.getLatestLongTermRuleSetIdx().get());
		final ClientJob job = facts.get(MESSAGE_CONTENT);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Preparing and sending offer to CMA for job {}", job.getJobId());

		agentProps.addJob(job, facts.get(RULE_SET_IDX), PROCESSING);
		checkForCustomComparator(job, facts);

		facts.put(JOB, job);
		agent.addBehaviour(InitiateProposal.create(agent, mapToRuleSetFacts(facts), PROPOSE_TO_EXECUTE_JOB_RULE,
				controller));
	}

	private void checkForCustomComparator(final ClientJob job, final RuleSetFacts facts) {
		int newRuleSetIdx = facts.get(RULE_SET_IDX);

		if (nonNull(job.getSelectionPreference())) {
			final String log = "Comparing Server offers using custom comparator";
			final String ruleSetName = "CUSTOM_CLIENT_COMPARATOR_" + job.getJobId();
			final RuleSetRest rules = constructRuleSetForCustomClientComparison(job.getSelectionPreference(),
					ruleSetName, log, job.getJobId(), REGIONAL_MANAGER);
			newRuleSetIdx = controller.getLatestRuleSetIdx().get() + 1;

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf(newRuleSetIdx));
			logger.info("Client with job {} requested to use custom server comparison. Adding rule set {}",
					job.getJobId(), ruleSetName);

			final RuleSet modifications = new RuleSet(rules);
			controller.addModifiedTemporaryRuleSetFromCurrent(modifications, newRuleSetIdx);
		}
		agentProps.getRuleSetForJob().replace(job.getJobInstanceId(), newRuleSetIdx);
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewScheduledJobRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
