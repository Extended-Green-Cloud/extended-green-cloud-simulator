package org.greencloud.rulescontroller.behaviour.search;

import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleStepType.SEARCH_AGENTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SEARCH_HANDLE_NO_RESULTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SEARCH_HANDLE_RESULTS_STEP;

import java.util.Set;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.enums.rules.RuleType;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Abstract behaviour providing template for handling agents search in DF
 */
public class SearchForAgents extends OneShotBehaviour {

	private final StrategyFacts facts;
	protected RulesController<?, ?> controller;

	/**
	 * Constructor
	 *
	 * @param agent      agent executing the behaviour
	 * @param facts      facts under which the search is to be performed
	 * @param ruleType   type of the rule that handles search execution
	 * @param controller rules controller
	 */
	public SearchForAgents(final Agent agent, final StrategyFacts facts, final RuleType ruleType,
			final RulesController<?, ?> controller) {
		super(agent);
		this.facts = FactsMapper.mapToStrategyFacts(facts);
		this.facts.put(RULE_TYPE, ruleType);
		this.controller = controller;
	}

	/**
	 * Method looks for agent which registered given service.
	 */
	@Override
	public void action() {
		facts.put(RULE_STEP, SEARCH_AGENTS_STEP);
		controller.fire(facts);

		final Set<AID> foundAgents = facts.get(RESULT);

		if (foundAgents.isEmpty()) {
			facts.put(RULE_STEP, SEARCH_HANDLE_NO_RESULTS_STEP);
		} else {
			facts.put(RULE_STEP, SEARCH_HANDLE_RESULTS_STEP);
		}
		controller.fire(facts);
		postProcessSearch(facts);
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions at the end of search execution
	 */
	protected void postProcessSearch(final StrategyFacts facts) {
		// to be overridden if necessary
	}
}
