package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.regionalmanager.job.listening.newjob.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ACCEPTED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFICATION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.greencloud.commons.utils.facts.PriorityFactsFactory.constructFactsForPriorityPreEvaluation;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENTS;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class ProcessNewJobVerificationSuccessfulRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessNewJobVerificationSuccessfulRule.class);

	public ProcessNewJobVerificationSuccessfulRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_VERIFICATION_RULE, NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE,
				"handles new job verification - verification successful",
				"rule run when RMA verifies possibility of the execution of new job received from CMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final List<AID> servers = facts.get(AGENTS);
		return !agentProps.getOwnedActiveServers().isEmpty() && !servers.isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJobWithServer job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job {} accepted for execution! Job is being pre-processed before adding to the queue.",
				job.getJobId());
		controller.fire(constructFactsForPriorityPreEvaluation(facts.get(RULE_SET_IDX), mapToClientJob(job)));

		facts.put(JOB_ACCEPTED, job);
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewJobVerificationSuccessfulRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
