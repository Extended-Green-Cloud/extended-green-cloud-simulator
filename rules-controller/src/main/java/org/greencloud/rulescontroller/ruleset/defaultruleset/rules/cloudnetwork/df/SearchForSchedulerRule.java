package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.df;

import static org.greencloud.commons.constants.DFServiceConstants.SCHEDULER_SERVICE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SEARCH_OWNER_AGENT_RULE;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.search;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Set;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentSearchRule;
import org.slf4j.Logger;

import jade.core.AID;

public class SearchForSchedulerRule extends AgentSearchRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(SearchForSchedulerRule.class);

	public SearchForSchedulerRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SEARCH_OWNER_AGENT_RULE,
				"searching for Scheduler",
				"handle search for Scheduler Agent");
	}

	@Override
	protected Set<AID> searchAgents(final RuleSetFacts facts) {
		return search(agent, agentProps.getParentDFAddress(), SCHEDULER_SERVICE_TYPE);
	}

	@Override
	protected void handleNoResults(final RuleSetFacts facts) {
		logger.info("Scheduler was not found");
		agent.doDelete();
	}

	@Override
	protected void handleResults(final Set<AID> dfResults, final RuleSetFacts facts) {
		agentProps.setScheduler(dfResults.stream().findFirst().orElseThrow());
	}
}
