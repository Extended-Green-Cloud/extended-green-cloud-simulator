package org.greencloud.agentsystem.strategies.deault.rules.server.job.execution.processing;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ENERGY_TYPE;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_MANUAL_FINISH_INFORM;
import static org.greencloud.commons.enums.energy.EnergyTypeEnum.BACK_UP_POWER;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINAL_PRICE_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_FINISH_JOB_BACK_UP_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.job.JobUtils.isJobUnique;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobFinishMessage;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobFinishMessageForRMA;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.listen.ListenForSingleMessage;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessJobFinishExecutionRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobFinishExecutionRule.class);

	public ProcessJobFinishExecutionRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_FINISH_JOB_EXECUTION_RULE,
				"processing finish of the job execution in Server",
				"rule handles finish of the Job execution in given Server");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		return facts.get(ENERGY_TYPE).equals(BACK_UP_POWER) ||
				nonNull(agentProps.getGreenSourceForJobMap().get(job.getJobId()));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		sendFinishInformation(facts);

		facts.put(RULE_TYPE, PROCESS_FINISH_JOB_BACK_UP_EXECUTION_RULE);
		controller.fire(facts);
	}

	private void sendFinishInformation(final RuleSetFacts facts) {
		final boolean isJobManuallyFinished = ofNullable((Boolean) facts.get(JOB_MANUAL_FINISH_INFORM)).orElse(false);
		final boolean isExecutedOnBackUpPower = facts.get(ENERGY_TYPE).equals(BACK_UP_POWER);
		final ClientJob job = facts.get(JOB);

		agentProps.getJobsExecutionTime()
				.stopJobExecutionTimer(job, agentProps.getServerJobs().get(job), getCurrentTime());

		if (!isJobManuallyFinished && !isExecutedOnBackUpPower) {
			final AID greenSource = agentProps.getGreenSourceForJobMap().get(job.getJobId());
			final ACLMessage jobFinishMessage = prepareJobFinishMessage(job, facts.get(RULE_SET_IDX), greenSource);
			agent.send(jobFinishMessage);

			final RuleSetFacts listenerFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			listenerFacts.put(JOB, job);
			listenerFacts.put(MESSAGE, jobFinishMessage);
			agent.addBehaviour(
					ListenForSingleMessage.create(agent, listenerFacts, FINAL_PRICE_RECEIVER_RULE, controller));
		} else {
			finishJobInRMA(job, facts);
			updateStateAfterJobIsDone(facts);
		}
	}

	private void finishJobInRMA(final ClientJob job, final RuleSetFacts facts) {
		agentProps.updateJobExecutionCost(job);
		final Double finalJobPrice = agentProps.getTotalPriceForJob().get(job.getJobId());
		final ACLMessage rmaMessage = prepareJobFinishMessageForRMA(job, facts.get(RULE_SET_IDX), finalJobPrice,
				agentProps.getOwnerRegionalManagerAgent());
		agentProps.getTotalPriceForJob().remove(job.getJobId());
		agent.send(rmaMessage);
	}

	private void updateStateAfterJobIsDone(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		if (isJobStarted(job, agentProps.getServerJobs())) {
			agentProps.incrementJobCounter(job.getJobId(), FINISH);
		}

		if (isJobUnique(job.getJobId(), agentProps.getServerJobs())) {
			agentProps.getGreenSourceForJobMap().remove(job.getJobId());
			agentNode.updateClientNumber(getJobCount(agentProps.getServerJobs(), ACCEPTED_JOB_STATUSES));
		}
		agentProps.removeJob(job);

		if (agentProps.isDisabled() && agentProps.getServerJobs().isEmpty()) {
			logger.info("Server completed all planned jobs and is fully disabled.");
			agentNode.disableServer();
		}
		agentProps.updateGUI();
	}

	@Override
	public AgentRule copy() {
		return new ProcessJobFinishExecutionRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
