package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.resupply;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.AGENT;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.RuleType.CHECK_SINGLE_AFFECTED_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.RESUPPLY_JOB_WITH_GREEN_POWER_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class ProcessCheckSingleAffectedJobRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessCheckSingleAffectedJobRule.class);

	public ProcessCheckSingleAffectedJobRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(CHECK_SINGLE_AFFECTED_JOB_RULE,
				"check a single job affected by power shortage",
				"rule tries to re-supply job with green power");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		final AID greenSource = agentProps.getGreenSourceForJobMap().get(job.getJobId());
		if (nonNull(greenSource)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Trying to supply job {} using green power", job.getJobId());

			final RuleSetFacts resupplyFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			resupplyFacts.put(JOB, job);
			resupplyFacts.put(AGENT, greenSource);

			agent.addBehaviour(
					InitiateRequest.create(agent, resupplyFacts, RESUPPLY_JOB_WITH_GREEN_POWER_RULE, controller));
		}
	}
}
