package org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob.execution;

import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobFinishMessage;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

import jade.core.AID;

public class ProcessJobFinishWeatherDropRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobFinishWeatherDropRule.class);

	public ProcessJobFinishWeatherDropRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_FINISH_JOB_EXECUTION_RULE,
				"processing finish of the job execution in Server",
				"rule handles finish of the Job execution in given Server");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final List<AID> receivers = singletonList(agentProps.getOwnerCloudNetworkAgent());
		agent.send(prepareJobFinishMessage(job, facts.get(STRATEGY_IDX), receivers.toArray(new AID[0])));

		updateStateAfterJobIsDone(facts);
	}

	private void updateStateAfterJobIsDone(final Facts facts) {
		final ClientJob job = facts.get(JOB);
		final JobInstanceIdentifier jobInstance = mapToJobInstanceId(job);

		if (isJobStarted(job, agentProps.getServerJobs())) {
			agentProps.incrementJobCounter(jobInstance, FINISH);
		}
		agentNode.updateClientNumber(getJobCount(agentProps.getServerJobs(), ACCEPTED_JOB_STATUSES));
		agentProps.removeJob(job);

		if (agentProps.isDisabled() && agentProps.getServerJobs().size() == 0) {
			logger.info("Server completed all planned jobs and is fully disabled.");
			agentNode.disableServer();
		}
		agentProps.updateGUI();
	}
}
