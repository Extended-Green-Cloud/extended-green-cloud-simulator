package org.greencloud.agentsystem.strategies.deafult.rules.client.df;

import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.constants.DFServiceConstants.CMA_SERVICE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SEARCH_OWNED_AGENTS_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.utils.yellowpages.YellowPagesRegister.search;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Set;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.exception.AgentNotFoundException;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentSearchRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;

public class SearchForCMAByClientRule extends AgentSearchRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(SearchForCMAByClientRule.class);

	public SearchForCMAByClientRule(final RulesController<ClientAgentProps, ClientNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SEARCH_OWNED_AGENTS_RULE,
				"searching for Central Manager.",
				"handle search for Central Manager Agent");
	}

	@Override
	protected Set<AID> searchAgents(final RuleSetFacts facts) {
		return search(agent, agentProps.getParentDFAddress(), CMA_SERVICE_TYPE);
	}

	@Override
	protected void handleNoResults(final RuleSetFacts facts) {
		logger.info("Central Manager was not found");
		agent.doDelete();
	}

	@Override
	protected void handleResults(final Set<AID> dfResults, final RuleSetFacts facts) {
		if (!agentProps.isAnnounced()) {
			agentNode.announceNewClient();
			agentProps.setAnnounced(true);
		}
		facts.put(AGENT, dfResults.stream().findFirst()
				.orElseThrow(() -> new AgentNotFoundException("CentralManager")));
		facts.put(RULE_TYPE, NEW_JOB_ANNOUNCEMENT_RULE);
		controller.fire(facts);
	}

	@Override
	public AgentRule copy() {
		return new SearchForCMAByClientRule(controller);
	}

	@Override
	public String getAgentType() {
		return CLIENT.getName();
	}
}
