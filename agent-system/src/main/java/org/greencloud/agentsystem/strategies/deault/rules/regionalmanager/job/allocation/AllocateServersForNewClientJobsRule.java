package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.allocation;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.join;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForJobsAllocationHandling;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class AllocateServersForNewClientJobsRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(AllocateServersForNewClientJobsRule.class);

	public AllocateServersForNewClientJobsRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ALLOCATION_RULE,
				"allocate jobs between Servers",
				"when batch of new job is received, it is being allocated between Servers");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);
		final String indexes = join(jobs.stream().map(PowerJob::getJobId).toList(), ",");

		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Allocating servers for the next jobs batch for jobs with ids: {}.", indexes);
		controller.fire(constructFactsForJobsAllocationHandling(facts.get(RULE_SET_IDX), jobs));
	}

	@Override
	public AgentRule copy() {
		return new AllocateServersForNewClientJobsRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
