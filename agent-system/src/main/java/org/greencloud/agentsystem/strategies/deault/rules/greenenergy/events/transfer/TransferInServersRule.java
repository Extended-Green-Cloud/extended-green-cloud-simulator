package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.transfer;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.COMPUTE_FINAL_PRICE;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.TRANSFER_INSTANT;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REFUSED_TRANSFER_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.TRANSFER_JOB_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.mapper.JobMapper.mapToPowerShortageJob;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareJobTransferRequest;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.transfer.JobDivided;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

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
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final Instant startTime = facts.get(EVENT_TIME);

		final RuleSetFacts divisionFacts = agentProps.constructDivisionFacts(job, startTime, facts.get(RULE_SET_IDX));
		controller.fire(divisionFacts);
		final JobDivided<ServerJob> newJobInstances = divisionFacts.get(RESULT);
		final JobPowerShortageTransfer transfer = mapToPowerShortageJob(job.getJobInstanceId(), newJobInstances,
				startTime);

		facts.put(TRANSFER_INSTANT, newJobInstances.getSecondInstance());

		return prepareJobTransferRequest(transfer, newJobInstances.getSecondInstance().getServer(),
				facts.get(RULE_SET_IDX));
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		final ServerJob job = facts.get(TRANSFER_INSTANT);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		if (agentProps.getServerJobs().containsKey(job)) {
			final String jobId = job.getJobId();
			logger.info("Transfer of job with id {} was established successfully. Finishing job on power shortage.",
					jobId);

			if (isJobStarted(job, agentProps.getServerJobs())) {
				agentProps.incrementJobCounter(mapToJobInstanceId(job), FINISH);
			}

			final RuleSetFacts finishFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			finishFacts.put(JOB, job);
			finishFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
			finishFacts.put(COMPUTE_FINAL_PRICE, true);
			controller.fire(finishFacts);
		} else {
			logger.info("The job instance with id {} was finished upon transfer.", job.getJobInstanceId());
		}
		agentProps.updateGUI();
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		final String messageContent = refuse.getContent();
		final ServerJob job = facts.get(TRANSFER_INSTANT);

		final RuleSetFacts refuseFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		refuseFacts.put(JOB, job);
		refuseFacts.put(MESSAGE_CONTENT, messageContent);
		refuseFacts.put(RULE_TYPE, REFUSED_TRANSFER_JOB_RULE);

		controller.fire(refuseFacts);
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case does not apply here
	}
}
