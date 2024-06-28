package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.announcing;

import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ENERGY_TYPE;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.energy.EnergyTypeEnum.BACK_UP_POWER;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS_BACKUP_ENERGY_PLANNED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.mapper.JobMapper.mapPowerJobToEnergyJob;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.facts.ProposalsFactsFactory.constructFactsForProposalsComparison;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareAcceptJobOfferReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.EnergyJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentCFPRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class LookForGreenSourceForJobExecutionRule extends AgentCFPRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(LookForGreenSourceForJobExecutionRule.class);

	public LookForGreenSourceForJobExecutionRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_RULE,
				"look for Green Source",
				"rule looks for Green Source that will provide power for job execution");
	}

	@Override
	protected ACLMessage createCFPMessage(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final List<AID> greenSources = agentProps.getOwnedActiveGreenSources().stream().toList();
		final double estimatedEnergy = agentProps.estimatePowerForJob(job);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Energy estimated for executing job {} is {}.", job.getJobId(), estimatedEnergy);

		final EnergyJob energyJob = mapPowerJobToEnergyJob(job, estimatedEnergy);
		return prepareCallForProposal(energyJob, greenSources, SERVER_JOB_CFP_PROTOCOL, facts.get(RULE_SET_IDX));
	}

	@Override
	protected int compareProposals(final RuleSetFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final RuleSetFacts comparatorFacts =
				constructFactsForProposalsComparison(facts.get(RULE_SET_IDX), bestProposal, newProposal);
		controller.fire(comparatorFacts);
		return comparatorFacts.get(RESULT);
	}

	@Override
	protected void handleRejectProposal(final ACLMessage proposalToReject, final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		agent.send(prepareReply(proposalToReject, mapClientJobToJobInstanceId(job), REJECT_PROPOSAL));
	}

	@Override
	protected void handleNoResponses(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("No responses were retrieved from green sources - executing job using non-renewable power.");
		scheduleForNonRenewablePowerExecution(facts);
	}

	@Override
	protected void handleNoProposals(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("No Green Sources available - executing job using non-renewable power.");
		scheduleForNonRenewablePowerExecution(facts);
	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		if (agentProps.getServerJobs().containsKey(job)) {
			final AID greenSource = bestProposal.getSender();
			final String jobId = job.getJobId();

			agentProps.getServerJobs().replace(job, ACCEPTED);

			MDC.put(MDC_JOB_ID, jobId);
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Chosen Green Source for the job with id {} : {}. Scheduling job execution.",
					jobId, greenSource.getLocalName());
			agentProps.getGreenSourceForJobMap().put(jobId, greenSource);

			agent.send(prepareAcceptJobOfferReply(bestProposal, mapToJobInstanceId(job), SERVER_JOB_CFP_PROTOCOL));
		} else {
			agent.send(prepareReply(bestProposal, mapClientJobToJobInstanceId(job), REJECT_PROPOSAL));
			scheduleForNonRenewablePowerExecution(facts);
		}

	}

	private void scheduleForNonRenewablePowerExecution(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		agentProps.getServerJobs().replace(job, IN_PROGRESS_BACKUP_ENERGY_PLANNED);

		facts.put(ENERGY_TYPE, BACK_UP_POWER);
		agent.addBehaviour(ScheduleOnce.create(agent, facts, START_JOB_EXECUTION_RULE, controller,
				SELECT_BY_FACTS_IDX));
	}

	@Override
	public AgentRule copy() {
		return new LookForGreenSourceForJobExecutionRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
