package org.greencloud.commons.domain.facts;

import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;

import org.jeasy.rules.api.Facts;

/**
 * Abstract class extending traditional StrategyFacts with assigned strategy index.
 */
public class StrategyFacts extends Facts {

	/**
	 * Constructor
	 *
	 * @param strategyIdx new strategy index
	 */
	public StrategyFacts(final int strategyIdx) {
		super();
		put(STRATEGY_IDX, strategyIdx);
	}
}
