package org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob;

import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.ORIGINAL_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.greencloud.commons.mapper.FactsMapper.mapToStrategyFacts;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.HardwareResources;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class ProcessCNANewJobWeatherDropRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessCNANewJobWeatherDropRule.class);

	public ProcessCNANewJobWeatherDropRule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handles new CNA job request",
				"handling new job sent by CNA");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(MESSAGE_CONTENT);
		final HardwareResources resources = agentProps.getAvailableResources(job, null, null);

		if (!resources.areSufficient(job.getEstimatedResources())
				|| !agentProps.canTakeIntoProcessing()
				|| agentProps.isHasError()) {
			final ACLMessage message = facts.get(MESSAGE);
			logger.info("Not enough available resources! Sending refuse message to Cloud Network Agent");
			agent.send(prepareRefuseReply(message));
		} else {
			agentProps.addJob(job, facts.get(STRATEGY_IDX), PROCESSING);
			agentProps.takeJobIntoProcessing();

			final StrategyFacts proposalFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
			proposalFacts.put(JOB, job);
			proposalFacts.put(ORIGINAL_MESSAGE, facts.get(MESSAGE));
			agent.addBehaviour(InitiateProposal.create(agent, proposalFacts, PROPOSE_TO_EXECUTE_JOB_RULE, controller));
		}
	}
}
