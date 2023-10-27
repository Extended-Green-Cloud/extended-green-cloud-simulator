package org.greencloud.strategyinjection.agentsystem.ruleset.booking.df;

import static org.greencloud.commons.enums.rules.RuleType.SEARCH_OWNED_AGENTS_RULE;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.search;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Set;

import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentSearchRule;
import org.greencloud.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.greencloud.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.slf4j.Logger;

import jade.core.AID;

public class SearchForRestaurantsRule extends AgentSearchRule<BookingProps, BookingNode> {

	private static final Logger logger = getLogger(SearchForRestaurantsRule.class);

	public SearchForRestaurantsRule(final RulesController<BookingProps, BookingNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SEARCH_OWNED_AGENTS_RULE,
				"searching for Restaurants",
				"handle search for Restaurants Agents");
	}

	@Override
	protected Set<AID> searchAgents(final RuleSetFacts facts) {
		return search(agent, agent.getDefaultDF(), "RESTAURANT");
	}

	@Override
	protected void handleNoResults(final RuleSetFacts facts) {
		logger.info("No restaurants found!");
		agent.doDelete();
	}

	@Override
	protected void handleResults(final Set<AID> dfResults, final RuleSetFacts facts) {
		logger.info("Found {} restaurants!", (long) dfResults.size());
		agentProps.getRestaurants().addAll(dfResults);
	}
}
