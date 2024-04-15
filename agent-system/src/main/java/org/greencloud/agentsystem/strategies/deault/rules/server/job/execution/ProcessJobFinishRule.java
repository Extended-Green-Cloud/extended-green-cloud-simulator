package org.greencloud.agentsystem.strategies.deault.rules.server.job.execution;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_FINISH_INFORM;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_MANUAL_FINISH_INFORM;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINAL_PRICE_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_FINISH_JOB_BACK_UP_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.job.JobUtils.isJobUnique;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobFinishMessage;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobFinishMessageForRMA;
import static org.greencloud.commons.utils.messaging.factory.PriceMessageFactory.preparePriceMessage;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.listen.ListenForSingleMessage;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessJobFinishRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobFinishRule.class);

	public ProcessJobFinishRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
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
		return nonNull(agentProps.getGreenSourceForJobMap().get(job.getJobId()));
	}

	/**
	 * Method executes given rule
	 *
	 * @param facts facts used in evaluation
	 */
	@Override
	public void executeRule(final RuleSetFacts facts) {
		sendFinishInformation(facts);

		facts.put(RULE_TYPE, PROCESS_FINISH_JOB_BACK_UP_EXECUTION_RULE);
		controller.fire(facts);
	}

	private void sendFinishInformation(final RuleSetFacts facts) {
		final boolean isJobFullyFinished = facts.get(JOB_FINISH_INFORM);
		final boolean isJobManuallyFinished = ofNullable((Boolean) facts.get(JOB_MANUAL_FINISH_INFORM)).orElse(false);
		final ClientJob job = facts.get(JOB);
		final AID greenSource = agentProps.getGreenSourceForJobMap().get(job.getJobId());
		final ACLMessage jobFinishMessage = prepareJobFinishMessage(job, facts.get(RULE_SET_IDX), greenSource);

		agentProps.getJobsExecutionTime()
				.stopJobExecutionTimer(job, agentProps.getServerJobs().get(job), getCurrentTime());

		if (!isJobManuallyFinished) {
			agent.send(jobFinishMessage);
		}

		if (!isJobManuallyFinished && isJobFullyFinished) {
			final RuleSetFacts listenerFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			listenerFacts.put(JOB, job);
			listenerFacts.put(MESSAGE, jobFinishMessage);
			agent.addBehaviour(
					ListenForSingleMessage.create(agent, listenerFacts, FINAL_PRICE_RECEIVER_RULE, controller));
		} else if (isJobFullyFinished) {
			finishJobInRMA(job, facts);
			updateStateAfterJobIsDone(facts);
		} else {
			informRMAAboutPrice(job, facts);
			updateStateAfterJobIsDone(facts);
		}
	}

	private void informRMAAboutPrice(final ClientJob job, final RuleSetFacts facts) {
		agentProps.updateJobExecutionCost(job);
		final Double finalJobPrice = agentProps.getTotalPriceForJob().get(job.getJobId());
		final JobInstanceIdentifier jobInstanceId = mapToJobInstanceId(job);
		final ACLMessage rmaPriceMessage = preparePriceMessage(agentProps.getOwnerRegionalManagerAgent(), jobInstanceId,
				finalJobPrice, facts.get(RULE_SET_IDX));
		agentProps.getTotalPriceForJob().remove(job.getJobId());
		agent.send(rmaPriceMessage);
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
		final JobInstanceIdentifier jobInstance = mapClientJobToJobInstanceId(job);

		if (isJobStarted(job, agentProps.getServerJobs())) {
			agentProps.incrementJobCounter(jobInstance, FINISH);
		}

		if (isJobUnique(job.getJobId(), agentProps.getServerJobs())) {
			agentProps.getGreenSourceForJobMap().remove(job.getJobId());
			agentNode.updateClientNumber(getJobCount(agentProps.getServerJobs(), ACCEPTED_JOB_STATUSES));
		}
		agentProps.removeJob(job);

		if (agentProps.isDisabled() && agentProps.getServerJobs().size() == 0) {
			logger.info("Server completed all planned jobs and is fully disabled.");
			agentNode.disableServer();
		}

		agentProps.updateGUI();
	}
}
