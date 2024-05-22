package org.greencloud.agentsystem.strategies.executiontimebased.centralmanager.job.priority;

import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ADD_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.REQUEST_JOB_EXECUTION_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGES;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class AskForFastestExecutionTime extends AgentRequestRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(AskForFastestExecutionTime.class);

	public AskForFastestExecutionTime(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withMessageProtocol(REQUEST_JOB_EXECUTION_TIME)
				.withObjectContent(job)
				.withReceivers(agentProps.getAvailableRegionalManagers())
				.build();
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received all responses. Computing job {} priority.", job.getJobId());

		final RuleSetFacts jobRelatedFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		jobRelatedFacts.put(MESSAGES, informs);
		agentProps.getPriorityFacts().put(job, jobRelatedFacts);

		final RuleSetFacts addingJobFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		addingJobFacts.put(RULE_TYPE, NEW_JOB_ADD_JOB_RULE);
		addingJobFacts.put(JOB, job);
		controller.fire(addingJobFacts);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("ASK_FOR_FASTEST_EXECUTION_TIME",
				"ask RMAs for estimations of the fastest job execution time",
				"evaluate job execution time options to establish job priority");
	}

	@Override
	public AgentRule copy() {
		return new AskForFastestExecutionTime(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
