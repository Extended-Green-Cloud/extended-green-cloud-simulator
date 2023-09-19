package org.greencloud.rulescontroller.strategy;

import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;

import java.util.function.ToIntFunction;

import org.greencloud.rulescontroller.RulesController;
import org.jeasy.rules.api.Facts;

/**
 * Enum storing types of strategy selection
 */
public enum StrategySelector {

	SELECT_BY_FACTS_IDX, SELECT_LATEST;

	/**
	 * Method returns selector that can be used to choose strategy index.
	 *
	 * @param selector type of selector
	 * @return selector function
	 */
	public static ToIntFunction<Facts> selectStrategyIndex(final StrategySelector selector,
			final RulesController<?, ?> controller) {
		return switch (selector) {
			case SELECT_BY_FACTS_IDX -> facts -> facts.get(STRATEGY_IDX);
			case SELECT_LATEST -> facts -> controller.getLatestStrategy().get();
		};
	}
}
