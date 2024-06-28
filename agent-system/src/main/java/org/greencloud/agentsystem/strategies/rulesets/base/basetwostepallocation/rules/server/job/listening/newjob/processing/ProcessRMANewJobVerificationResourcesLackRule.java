package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.server.job.listening.newjob.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_REFUSED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLE_NO_RESOURCES_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFICATION_RULE;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessRMANewJobVerificationResourcesLackRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessRMANewJobVerificationResourcesLackRule.class);

	public ProcessRMANewJobVerificationResourcesLackRule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_VERIFICATION_RULE, NEW_JOB_RECEIVER_HANDLE_NO_RESOURCES_RULE,
				"handles new RMA job request - there are no resources needed for job execution.",
				"handling new job sent by RMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJobWithServer job = facts.get(JOB);

		return agentProps.canTakeIntoProcessing() &&
				agentProps.isHasError() &&
				!areSufficient(agentProps.resources(), job.getRequiredResources());
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJobWithServer job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Server does not have resources needed for job execution.");

		facts.put(JOB_REFUSED, job);
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMANewJobVerificationResourcesLackRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
