package org.greencloud.agentsystem.strategies.basetwostepallocation.rules.regionalmanager.job.listening.newjob.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_REFUSED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLE_NO_AGENTS_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFICATION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobRemoval;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

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

public class ProcessNewJobVerificationNoServersRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessNewJobVerificationNoServersRule.class);

	public ProcessNewJobVerificationNoServersRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_VERIFICATION_RULE, NEW_JOB_RECEIVER_HANDLE_NO_AGENTS_RULE,
				"handles new job verification - no active Servers",
				"rule run when RMA verifies possibility of the execution of new job received from CMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return agentProps.getOwnedActiveServers().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJobWithServer job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("No servers with enough resources for job {}!", job.getJobId());

		controller.fire(constructFactsForJobRemoval(facts.get(RULE_SET_IDX), mapToClientJob(job)));
		facts.put(JOB_REFUSED, job);
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewJobVerificationNoServersRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
