package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.adaptation;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_PARAMS;
import static org.greencloud.commons.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.enums.rules.RuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.utils.math.MathOperations.nextFibonacci;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.args.adaptation.singleagent.ChangeGreenSourceWeights;
import com.gui.agents.server.ServerNode;

import jade.core.AID;

public class ChangeGreenSourceWeightRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public ChangeGreenSourceWeightRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize default rule metadata
	 *
	 * @return rule description
	 */
	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(ADAPTATION_REQUEST_RULE,
				"disable Server",
				"performing adaptation which disables Server");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return facts.get(ADAPTATION_TYPE).equals(CHANGE_GREEN_SOURCE_WEIGHT);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final String targetAgent = ((ChangeGreenSourceWeights) facts.get(ADAPTATION_PARAMS)).greenSourceName();
		final ConcurrentHashMap<AID, Integer> newWeights =
				new ConcurrentHashMap<>(agentProps.getWeightsForGreenSourcesMap());

		if (newWeights.keySet().stream().noneMatch(agent -> agent.getName().equals(targetAgent))) {
			facts.put(RESULT, false);
		}

		newWeights.entrySet().forEach(entry -> increaseWeight(entry, targetAgent));
		agentProps.setWeightsForGreenSourcesMap(newWeights);
		facts.put(RESULT, true);
	}

	private void increaseWeight(final Map.Entry<AID, Integer> entry, final String targetGreenSource) {
		if (!entry.getKey().getName().equals(targetGreenSource)) {
			entry.setValue(nextFibonacci(entry.getValue()));
		}
	}
}
