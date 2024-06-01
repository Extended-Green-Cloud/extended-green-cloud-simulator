package org.greencloud.commons.utils.facts;

import static org.greencloud.commons.constants.EGCSFactTypeConstants.BEST_PROPOSAL;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.BEST_PROPOSAL_CONTENT;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.NEW_PROPOSAL;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.NEW_PROPOSAL_CONTENT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

/**
 * Factory constructing common rule set facts for proposals
 */
public class ProposalsFactsFactory {

	/**
	 * Method construct facts passed to rules responsible for comparison of offers
	 *
	 * @param index        index of a rule set
	 * @param bestProposal best proposal message
	 * @param newProposal  new proposal message
	 * @param type         type of message content
	 * @return RuleSetFacts
	 */
	public static <E> RuleSetFacts constructFactsForProposalsComparison(final int index,
			final ACLMessage bestProposal, final ACLMessage newProposal, final Class<E> type) {
		final E bestOfferContent = readMessageContent(bestProposal, type);
		final E newOfferContent = readMessageContent(newProposal, type);

		final RuleSetFacts comparatorFacts = new RuleSetFacts(index);
		comparatorFacts.put(RULE_TYPE, COMPARE_EXECUTION_PROPOSALS);
		comparatorFacts.put(BEST_PROPOSAL, bestProposal);
		comparatorFacts.put(NEW_PROPOSAL, newProposal);
		comparatorFacts.put(BEST_PROPOSAL_CONTENT, bestOfferContent);
		comparatorFacts.put(NEW_PROPOSAL_CONTENT, newOfferContent);

		return comparatorFacts;
	}

	/**
	 * Method construct facts passed to rules responsible for comparison of offers
	 *
	 * @param index        index of a rule set
	 * @param bestProposal best proposal message
	 * @param newProposal  new proposal message
	 * @return RuleSetFacts
	 */
	public static RuleSetFacts constructFactsForProposalsComparison(final int index,
			final ACLMessage bestProposal, final ACLMessage newProposal) {
		final RuleSetFacts comparatorFacts = new RuleSetFacts(index);
		comparatorFacts.put(RULE_TYPE, COMPARE_EXECUTION_PROPOSALS);
		comparatorFacts.put(BEST_PROPOSAL, bestProposal);
		comparatorFacts.put(NEW_PROPOSAL, newProposal);

		return comparatorFacts;
	}
}
