package org.greencloud.agentsystem.strategies.deafult.rules.server.job.proposing.prepare;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.OFFER;
import static org.greencloud.commons.enums.energy.EnergyTypeEnum.UNKNOWN;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_PRICE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_DATA_FOR_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJob;
import static org.greencloud.commons.utils.messaging.factory.OfferMessageFactory.prepareServerJobOffer;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.ServerData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class PrepareProposalForRMA extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(PrepareProposalForRMA.class);

	public PrepareProposalForRMA(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_RULE,
				"RMA proposal preparation",
				"prepares the content of proposal sent to RMA");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final double estimatedPrice = computeExecutionPrice(job, facts);
		final Pair<Instant, Double> executionEstimation = agentProps.getEstimatedEarliestJobStartTimeAndDuration(job);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Estimated Server job price: {}.", estimatedPrice);
		logger.info("Estimated earliest job start time: {}. The estimated execution time will be {}.",
				executionEstimation.getLeft(), executionEstimation.getRight());

		agentProps.getServerPriceForJob().put(job.getJobInstanceId(), agentProps.getPricePerHour());

		final ACLMessage offerMessage = prepareServerJobOffer(agentProps, estimatedPrice, executionEstimation,
				job.getJobId(), facts.get(MESSAGE), facts.get(RULE_SET_IDX), UNKNOWN);

		facts.put(OFFER, readMessageContent(offerMessage, ServerData.class));
		facts.put(RESULT, offerMessage);
	}

	private Double computeExecutionPrice(final ClientJob job, final RuleSetFacts facts) {
		final RuleSetFacts priceFacts = constructFactsWithJob(facts.get(RULE_SET_IDX), job);
		priceFacts.put(RULE_TYPE, COMPUTE_PRICE_RULE);
		controller.fire(priceFacts);

		return priceFacts.get(RESULT);
	}

	@Override
	public AgentRule copy() {
		return new PrepareProposalForRMA(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
