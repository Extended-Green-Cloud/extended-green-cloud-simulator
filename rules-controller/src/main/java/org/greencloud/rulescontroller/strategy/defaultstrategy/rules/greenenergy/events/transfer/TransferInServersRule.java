package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.greenenergy.events.transfer;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.TRANSFER_INSTANT;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.REFUSED_TRANSFER_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.TRANSFER_JOB_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.mapper.JobMapper.mapToPowerShortageJob;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareJobTransferRequest;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.transfer.JobDivided;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

import jade.lang.acl.ACLMessage;

public class TransferInServersRule extends AgentRequestRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(TransferInServersRule.class);

	public TransferInServersRule(final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(TRANSFER_JOB_RULE, "request job transfer in Server",
				"rule sends request to Server to perform job transfer");
	}

	@Override
	protected ACLMessage createRequestMessage(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		final Instant startTime = facts.get(EVENT_TIME);

		final StrategyFacts divisionFacts = agentProps.constructDivisionFacts(job, startTime, facts.get(STRATEGY_IDX));
		controller.fire(divisionFacts);
		final JobDivided<ServerJob> newJobInstances = divisionFacts.get(RESULT);
		final JobPowerShortageTransfer transfer = mapToPowerShortageJob(job.getJobInstanceId(), newJobInstances,
				startTime);

		facts.put(TRANSFER_INSTANT, newJobInstances.getSecondInstance());

		return prepareJobTransferRequest(transfer, newJobInstances.getSecondInstance().getServer(),
				facts.get(STRATEGY_IDX));
	}

	@Override
	protected void handleInform(final ACLMessage inform, final StrategyFacts facts) {
		final ServerJob job = facts.get(TRANSFER_INSTANT);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		if (agentProps.getServerJobs().containsKey(job)) {
			final String jobId = job.getJobId();
			logger.info("Transfer of job with id {} was established successfully. Finishing job on power shortage.",
					jobId);

			if (isJobStarted(job, agentProps.getServerJobs())) {
				agentProps.incrementJobCounter(mapToJobInstanceId(job), FINISH);
			}
			final StrategyFacts finishFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
			finishFacts.put(JOB, job);
			finishFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
			controller.fire(finishFacts);
		} else {
			logger.info("The job with id {} has finished before transfer.", job.getJobId());
		}
		agentProps.updateGUI();
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final StrategyFacts facts) {
		final String messageContent = refuse.getContent();
		final ServerJob job = facts.get(TRANSFER_INSTANT);

		final StrategyFacts refuseFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		refuseFacts.put(JOB, job);
		refuseFacts.put(MESSAGE_CONTENT, messageContent);
		refuseFacts.put(RULE_TYPE, REFUSED_TRANSFER_JOB_RULE);

		controller.fire(refuseFacts);
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final StrategyFacts facts) {
		// case does not apply here
	}
}
