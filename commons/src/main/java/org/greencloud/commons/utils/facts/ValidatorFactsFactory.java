package org.greencloud.commons.utils.facts;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.VALIDATE_REGIONAL_ERROR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.VALIDATE_SERVER_ERROR_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import org.jrba.rulesengine.ruleset.RuleSetFacts;

/**
 * Factory constructing common rule set facts used in adaptation
 */
public class ValidatorFactsFactory {

	/**
	 * Method construct facts used to validate server error
	 *
	 * @param facts            facts that are to be used as a basis for validation
	 * @return RuleSetFacts
	 */
	public static RuleSetFacts constructFactsForServerValidation(final RuleSetFacts facts) {
		facts.put(RULE_TYPE, VALIDATE_SERVER_ERROR_RULE);
		return facts;
	}

	/**
	 * Method construct facts used to validate server error
	 *
	 * @param facts            facts that are to be used as a basis for validation
	 * @return RuleSetFacts
	 */
	public static RuleSetFacts constructFactsForRMAValidation(final RuleSetFacts facts) {
		facts.put(RULE_TYPE, VALIDATE_REGIONAL_ERROR_RULE);
		return facts;
	}

}
