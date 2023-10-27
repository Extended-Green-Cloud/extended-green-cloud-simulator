package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.proposing;

import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.INPUT_DATA;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.ORIGINAL_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_BY_SERVER;
import static org.greencloud.commons.enums.rules.RuleType.COMPUTE_PRICE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.INSUFFICIENT_RESOURCES_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.factory.OfferMessageFactory.prepareServerJobOffer;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareAcceptJobOfferReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.GreenSourceData;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithProtocol;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentProposalRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class ProposeToCNARule extends AgentProposalRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProposeToCNARule.class);

	public ProposeToCNARule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROPOSE_TO_EXECUTE_JOB_RULE,
				"propose job execution to CNA",
				"rule sends proposal message to CNA and handles the response");
	}

	@Override
	protected ACLMessage createProposalMessage(final RuleSetFacts facts) {
		final GreenSourceData data = readMessageContent(facts.get(MESSAGE), GreenSourceData.class);

		final RuleSetFacts priceFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		priceFacts.put(INPUT_DATA, data);
		priceFacts.put(RULE_TYPE, COMPUTE_PRICE_RULE);
		controller.fire(priceFacts);

		return prepareServerJobOffer(agentProps, priceFacts.get(RESULT), data.getJobId(), facts.get(ORIGINAL_MESSAGE),
				facts.get(RULE_SET_IDX));
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
			logger.info("Cloud Network {} rejected the job volunteering offer", reject.getSender().getLocalName());

			agentProps.removeJob(job);
			agentProps.getGreenSourceForJobMap().remove(job.getJobId());
			agent.send(prepareReply(facts.get(MESSAGE), jobInstance, REJECT_PROPOSAL));
		}
	}
}
