package org.greencloud.commons.mapper;

import org.greencloud.commons.constants.FactTypeConstants;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.jeasy.rules.api.Facts;

/**
 * Class defines set of utilities used to handle facts
 */
public class FactsMapper {

	/**
	 * Method copies set of facts to new instance
	 *
	 * @param facts set of facts that are to be copied
	 * @return new set of facts
	 */
	public static StrategyFacts mapToStrategyFacts(final StrategyFacts facts) {
		final StrategyFacts newFacts = new StrategyFacts(facts.get(FactTypeConstants.STRATEGY_IDX));
		facts.asMap().forEach(newFacts::put);
		return newFacts;
	}

	/**
	 * Method copies set of facts to new instance
	 *
	 * @param facts set of facts that are to be copied
	 * @return new set of facts
	 */
	public static StrategyFacts mapToStrategyFacts(final Facts facts) {
		final StrategyFacts newFacts = new StrategyFacts(facts.get(FactTypeConstants.STRATEGY_IDX));
		facts.asMap().forEach(newFacts::put);
		return newFacts;
	}
}
