package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.regionalmanager.job.allocation.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForJobsAllocationDataRequest;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.timer.Timer;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessServerInitiateAllocationRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessServerInitiateAllocationRule.class);

	public ProcessServerInitiateAllocationRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_NEW_JOB_ALLOCATION_RULE,
				"handle next jobs allocation",
				"handles allocation of next job batch between Servers");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);

		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Preparing data for servers' jobs allocation.");

		final Timer timer = new Timer();
		timer.startTimeMeasure(getCurrentTime());

		final RuleSetFacts dataRequestFacts =
				constructFactsForJobsAllocationDataRequest(facts.get(RULE_SET_IDX), jobs, timer);
		controller.fire(dataRequestFacts);
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerInitiateAllocationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
