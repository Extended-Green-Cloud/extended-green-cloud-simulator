package org.greencloud.commons.domain.facts;

import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;

import org.jeasy.rules.api.Facts;

/**
 * Abstract class extending traditional RuleSetFacts with assigned rule set index.
 */
public class RuleSetFacts extends Facts {

	/**
	 * Constructor
	 *
	 * @param ruleSetIndex new rule set index
	 */
	public RuleSetFacts(final int ruleSetIndex) {
		super();
		put(RULE_SET_IDX, ruleSetIndex);
	}
}
