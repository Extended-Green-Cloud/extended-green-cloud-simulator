package org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob.propose;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.ORIGINAL_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.COMPUTE_PRICE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.CONFIRMED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static org.greencloud.commons.utils.messaging.factory.OfferMessageFactory.prepareServerJobOffer;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.domain.job.extended.ImmutableJobWithStatus;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentProposalRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.extended.JobWithProtocol;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.domain.resources.HardwareResources;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class ProposeToCNAWeatherDropRule extends AgentProposalRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProposeToCNAWeatherDropRule.class);

	public ProposeToCNAWeatherDropRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROPOSE_TO_EXECUTE_JOB_RULE,
				"propose job execution to CNA",
				"rule sends proposal message to CNA and handles the response");
	}

	@Override
	protected ACLMessage createProposalMessage(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);

		final StrategyFacts priceFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		priceFacts.put(RULE_TYPE, COMPUTE_PRICE_RULE);
		priceFacts.put(JOB, job);
		controller.fire(priceFacts);

		return prepareServerJobOffer(agentProps, priceFacts.get(RESULT), job.getJobId(), facts.get(ORIGINAL_MESSAGE),
				facts.get(STRATEGY_IDX));
	}

	@Override
	protected void handleAcceptProposal(final ACLMessage accept, final StrategyFacts facts) {
		final JobWithProtocol jobWithProtocol = readMessageContent(accept, JobWithProtocol.class);
		final JobInstanceIdentifier jobInstance = jobWithProtocol.getJobInstanceIdentifier();
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job)) {
			final HardwareResources availableResources = agentProps.getAvailableResources(job, null, null);

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			if (!availableResources.areSufficient(job.getEstimatedResources())) {
				final JobWithStatus jobStatusUpdate = new ImmutableJobWithStatus(jobInstance, getCurrentTime());
				logger.info("Not enough resources to execute the job {}.", job.getJobId());

				agentProps.removeJob(job);
				agentProps.incrementJobCounter(jobInstance, FAILED);
				agent.send(prepareFailureReply(accept, jobStatusUpdate, FAILED_JOB_PROTOCOL));
			} else {
				logger.info("Scheduling the execution of the job {} on back-up power", jobInstance.getJobId());

				agentProps.getServerJobs().replace(job, JobExecutionStatusEnum.IN_PROGRESS_BACKUP_ENERGY_PLANNED);
				agentNode.updateClientNumber(getJobCount(agentProps.getServerJobs(), ACCEPTED_JOB_STATUSES));

				logger.info("Announcing new job {} in network!", jobInstance.getJobId());
				agentNode.announceClientJob();
				agent.send(prepareJobStatusMessageForCNA(jobInstance, CONFIRMED_JOB_ID, agentProps,
						facts.get(STRATEGY_IDX)));

				final StrategyFacts startFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
				startFacts.put(JOB, job);
				agent.addBehaviour(ScheduleOnce.create(agent, startFacts, START_JOB_EXECUTION_RULE, controller,
						f -> f.get(STRATEGY_IDX)));
			}
		}
	}

	@Override
	protected void handleRejectProposal(final ACLMessage reject, final StrategyFacts facts) {
		final JobInstanceIdentifier jobInstance = readMessageContent(reject, JobInstanceIdentifier.class);
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			logger.info("Cloud Network {} rejected the job volunteering offer", reject.getSender().getLocalName());

			agentProps.removeJob(job);
		}
	}
}
