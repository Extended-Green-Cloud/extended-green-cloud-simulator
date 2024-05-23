package org.greencloud.commons.utils.facts;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ADAPTATION_REQUEST_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.ADAPTATION_PARAMS;
import static org.jrba.rulesengine.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

/**
 * Factory constructing common rule set facts used in adaptation
 */
public class AdaptationFactsFactory {

	/**
	 * Method construct facts passed to rules responsible for handling adaptation
	 *
	 * @param index            index of a rule set
	 * @param adaptationType   type of adaptation action
	 * @param actionParameters adaptation parameters
	 * @return RuleSetFacts
	 */
	public static RuleSetFacts constructFactsForAdaptationRequest(final int index,
			final AdaptationActionTypeEnum adaptationType,
			final AdaptationActionParameters actionParameters) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(RULE_TYPE, ADAPTATION_REQUEST_RULE);
		facts.put(ADAPTATION_PARAMS, actionParameters);
		facts.put(ADAPTATION_TYPE, adaptationType);

		return facts;
	}

}
