package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.centralmanager.job.allocation.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForJobsAllocationDataRequest;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.timer.Timer;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessRegionalManagerInitiateAllocationRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ProcessRegionalManagerInitiateAllocationRule.class);

	public ProcessRegionalManagerInitiateAllocationRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_NEW_JOB_ALLOCATION_RULE,
				"handle next jobs allocation",
				"handles allocation of next job batch between RMAs without Server's allocation");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);

		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Preparing data for jobs allocation.");

		final Timer timer = new Timer();
		timer.startTimeMeasure(getCurrentTime());

		final RuleSetFacts dataRequestFacts =
				constructFactsForJobsAllocationDataRequest(facts.get(RULE_SET_IDX), jobs, timer);
		controller.fire(dataRequestFacts);
	}

	@Override
	public AgentRule copy() {
		return new ProcessRegionalManagerInitiateAllocationRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
