package org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.df;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.DFServiceConstants.CMA_SERVICE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SEARCH_OWNER_AGENT_RULE;
import static org.jrba.utils.yellowpages.YellowPagesRegister.search;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Set;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentSearchRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;

public class SearchForCMARule extends AgentSearchRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(SearchForCMARule.class);

	public SearchForCMARule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SEARCH_OWNER_AGENT_RULE,
				"searching for CMA",
				"handle search for Central Manager Agent");
	}

	@Override
	protected Set<AID> searchAgents(final RuleSetFacts facts) {
		return search(agent, agentProps.getParentDFAddress(), CMA_SERVICE_TYPE);
	}

	@Override
	protected void handleNoResults(final RuleSetFacts facts) {
		logger.info("CMA was not found");
		agent.doDelete();
	}

	@Override
	protected void handleResults(final Set<AID> dfResults, final RuleSetFacts facts) {
		agentProps.setCma(dfResults.stream().findFirst().orElseThrow());
	}

	@Override
	public AgentRule copy() {
		return new SearchForCMARule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
