package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.df;

import static org.greencloud.commons.constants.FactTypeConstants.AGENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SEARCH_OWNED_AGENTS_RULE;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.search;
import static org.greencloud.commons.constants.DFServiceConstants.SCHEDULER_SERVICE_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Set;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentSearchRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.client.ClientNode;

import jade.core.AID;

public class SearchForSchedulerByClientRule extends AgentSearchRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(SearchForSchedulerByClientRule.class);

	public SearchForSchedulerByClientRule(final RulesController<ClientAgentProps, ClientNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SEARCH_OWNED_AGENTS_RULE,
				"searching for Scheduler",
				"handle search for Scheduler Agent");
	}

	@Override
	protected Set<AID> searchAgents(final StrategyFacts facts) {
		return search(agent, agentProps.getParentDFAddress(), SCHEDULER_SERVICE_TYPE);
	}

	@Override
	protected void handleNoResults(final StrategyFacts facts) {
		logger.info("Scheduler was not found");
		agent.doDelete();
	}

	@Override
	protected void handleResults(final Set<AID> dfResults, final StrategyFacts facts) {
		if (!agentProps.isAnnounced()) {
			agentNode.announceNewClient();
			agentProps.setAnnounced(true);
		}
		facts.put(AGENT, dfResults.stream().findFirst().orElseThrow());
		facts.put(RULE_TYPE, NEW_JOB_ANNOUNCEMENT_RULE);
		controller.fire(facts);
	}
}
