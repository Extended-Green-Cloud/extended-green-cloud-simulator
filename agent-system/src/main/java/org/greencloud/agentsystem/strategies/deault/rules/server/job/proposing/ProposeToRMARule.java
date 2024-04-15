package org.greencloud.agentsystem.strategies.deault.rules.server.job.proposing;

import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.jrba.rulesengine.constants.FactTypeConstants.INPUT_DATA;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.ORIGINAL_MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.energy.EnergyTypeEnum.GREEN_ENERGY;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_BY_SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_PRICE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.INSUFFICIENT_RESOURCES_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.factory.OfferMessageFactory.prepareServerJobOffer;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareAcceptJobOfferReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.GreenSourceData;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithProtocol;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentProposalRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProposeToRMARule extends AgentProposalRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProposeToRMARule.class);

	public ProposeToRMARule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROPOSE_TO_EXECUTE_JOB_RULE,
				"propose job execution to RMA",
				"rule sends proposal message to RMA and handles the response");
	}

	@Override
	protected ACLMessage createProposalMessage(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final GreenSourceData data = readMessageContent(facts.get(MESSAGE), GreenSourceData.class);

		final RuleSetFacts priceFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		priceFacts.put(INPUT_DATA, data);
		priceFacts.put(RULE_TYPE, COMPUTE_PRICE_RULE);
		controller.fire(priceFacts);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Estimated Server job price: {}", (double) priceFacts.get(RESULT));

		agentProps.getServerPriceForJob().put(job.getJobInstanceId(), agentProps.getPricePerHour());
		return prepareServerJobOffer(agentProps, priceFacts.get(RESULT), data.getJobId(), facts.get(ORIGINAL_MESSAGE),
				facts.get(RULE_SET_IDX), GREEN_ENERGY);
	}

	@Override
	protected void handleAcceptProposal(final ACLMessage accept, final RuleSetFacts facts) {
		final JobWithProtocol jobWithProtocol = readMessageContent(accept, JobWithProtocol.class);
		final JobInstanceIdentifier jobInstance = jobWithProtocol.getJobInstanceIdentifier();
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job)) {
			final Map<String, Resource> availableResources = agentProps.getAvailableResources(job, null, null);

			agentProps.incrementJobCounter(jobInstance, ACCEPTED);
			agentProps.getServerJobs().replace(job, ACCEPTED_BY_SERVER);

			if (!areSufficient(availableResources, job.getRequiredResources())) {
				final RuleSetFacts tempFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
				tempFacts.put(JOB, job);
				tempFacts.put(MESSAGE_TYPE, jobWithProtocol.getReplyProtocol());
				tempFacts.put(ORIGINAL_MESSAGE, facts.get(ORIGINAL_MESSAGE));
				tempFacts.put(MESSAGE, facts.get(MESSAGE));
				tempFacts.put(RULE_TYPE, INSUFFICIENT_RESOURCES_RULE);

				controller.fire(tempFacts);
			} else {
				MDC.put(MDC_JOB_ID, jobInstance.getJobId());
				MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
				logger.info("Sending ACCEPT_PROPOSAL to Green Source Agent");
				agent.send(prepareAcceptJobOfferReply(facts.get(MESSAGE), jobInstance,
						jobWithProtocol.getReplyProtocol()));
			}
		}
	}

	@Override
	protected void handleRejectProposal(final ACLMessage reject, final RuleSetFacts facts) {
		final JobInstanceIdentifier jobInstance = readMessageContent(reject, JobInstanceIdentifier.class);
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Regional Manager {} rejected the job volunteering offer", reject.getSender().getLocalName());

			agentProps.removeJob(job);

			if (agentProps.isDisabled() && agentProps.getServerJobs().size() == 0) {
				logger.info("Server completed all planned jobs and is fully disabled.");
				agentNode.disableServer();
			}

			agentProps.getGreenSourceForJobMap().remove(job.getJobId());
			agent.send(prepareReply(facts.get(MESSAGE), jobInstance, REJECT_PROPOSAL));
		}
	}
}
