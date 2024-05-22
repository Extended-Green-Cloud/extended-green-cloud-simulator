package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.job.execution;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.COMPUTE_FINAL_PRICE;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SERVER_DISCONNECTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.factory.PriceMessageFactory.prepareFinalPriceMessage;
import static org.greencloud.commons.utils.messaging.factory.PriceMessageFactory.preparePriceMessage;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessPowerSupplyRemoveRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessPowerSupplyRemoveRule.class);

	public ProcessPowerSupplyRemoveRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(FINISH_JOB_EXECUTION_RULE,
				"handle finish job power supply",
				"rule executes handler which completes job power supply");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final boolean sendFinalPriceSummary = facts.get(COMPUTE_FINAL_PRICE);

		if (sendFinalPriceSummary) {
			final Instant changeTime = nonNull(facts.get(MESSAGE_CONTENT))
					? ((JobWithStatus) facts.get(MESSAGE_CONTENT)).getChangeTime()
					: getCurrentTime();
			agentProps.getJobsExecutionTime()
					.stopJobExecutionTimer(job, agentProps.getServerJobs().get(job), changeTime);
			final double finalPrice = isJobStarted(agentProps.getServerJobs().get(job)) ?
					agentProps.computeFinalJobInstancePrice(job) : 0;

			final ACLMessage message = facts.get(MESSAGE);
			final JobInstanceIdentifier jobInstanceId = mapToJobInstanceId(job);

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Execution of the job {} (job instance: {}) cost {} in Green Source.",
					job.getJobId(), job.getJobInstanceId(), finalPrice);

			final ACLMessage priceMessage = nonNull(message)
					? prepareFinalPriceMessage(message.getReplyWith(), agentProps.getOwnerServer(), jobInstanceId,
					finalPrice, facts.get(RULE_SET_IDX))
					: preparePriceMessage(agentProps.getOwnerServer(), jobInstanceId, finalPrice,
					facts.get(RULE_SET_IDX));
			agent.send(priceMessage);
		}
		agentProps.removeJob(job);

		return agentProps.getGreenSourceDisconnection().isBeingDisconnectedFromServer();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final AID server = agentProps.getGreenSourceDisconnection().getServerToBeDisconnected();
		final boolean isLastJobRemoved = agentProps.getServerJobs().keySet().stream()
				.noneMatch(serverJob -> serverJob.getServer().equals(server));

		if (isLastJobRemoved) {
			final RuleSetFacts disconnectionFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			disconnectionFacts.put(AGENT, server);
			disconnectionFacts.put(MESSAGE, agentProps.getGreenSourceDisconnection().getOriginalAdaptationMessage());

			agent.addBehaviour(
					InitiateRequest.create(agent, disconnectionFacts, PROCESS_SERVER_DISCONNECTION_RULE, controller));
		}
	}

	@Override
	public AgentRule copy() {
		return new ProcessPowerSupplyRemoveRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
