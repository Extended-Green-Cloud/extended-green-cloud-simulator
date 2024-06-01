package org.greencloud.agentsystem.strategies.intentstandardonestep.centralmanager.job.allocation;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ALLOCATION_REQUEST_DATA;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJobs;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class PrepareServerResourcesDataRequestRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(PrepareServerResourcesDataRequestRule.class);

	public PrepareServerResourcesDataRequestRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ALLOCATION_REQUEST_DATA,
				"method requests servers data from RMAs",
				"initiates request for servers resources");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);
		final int ruleSetIds = facts.get(RULE_SET_IDX);

		MDC.put(MDC_RULE_SET_ID, valueOf(ruleSetIds));
		logger.info("Requesting server resources from RMAs to perform jobs allocation.");

		agent.addBehaviour(InitiateRequest.create(agent, constructFactsWithJobs(ruleSetIds, jobs),
				PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE, controller));
	}

	@Override
	public AgentRule copy() {
		return new PrepareServerResourcesDataRequestRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
