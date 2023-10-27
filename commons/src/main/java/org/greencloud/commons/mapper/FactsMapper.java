package org.greencloud.commons.mapper;

import org.greencloud.commons.constants.FactTypeConstants;
import org.greencloud.commons.domain.facts.RuleSetFacts;
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
	public static RuleSetFacts mapToRuleSetFacts(final RuleSetFacts facts) {
		final RuleSetFacts newFacts = new RuleSetFacts(facts.get(FactTypeConstants.RULE_SET_IDX));
		facts.asMap().forEach(newFacts::put);
		return newFacts;
	}

	/**
	 * Method copies set of facts to new instance
	 *
	 * @param facts set of facts that are to be copied
	 * @return new set of facts
	 */
	public static RuleSetFacts mapToRuleSetFacts(final Facts facts) {
		final RuleSetFacts newFacts = new RuleSetFacts(facts.get(FactTypeConstants.RULE_SET_IDX));
		facts.asMap().forEach(newFacts::put);
		return newFacts;
	}
}
