package org.greencloud.agentsystem.strategies.rulesets.base.baseonestepallocation.rules.regionalmanager.job.listening.newjob.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ACCEPTED;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.RESOURCES_SUFFICIENCY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFICATION_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.rulesengine.constants.MVELParameterConstants.AGENT;
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

import jade.core.AID;

public class ProcessNewJobServerVerificationSuccessfulRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessNewJobServerVerificationSuccessfulRule.class);

	public ProcessNewJobServerVerificationSuccessfulRule(
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
		final AID server = facts.get(AGENT);
		final boolean areResourcesSufficient = facts.get(RESOURCES_SUFFICIENCY);

		return agentProps.getOwnedActiveServers().contains(server) && areResourcesSufficient;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJobWithServer job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job {} accepted for execution! Passing job to the server.", job.getJobId());

		facts.put(JOB_ACCEPTED, job);
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewJobServerVerificationSuccessfulRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
