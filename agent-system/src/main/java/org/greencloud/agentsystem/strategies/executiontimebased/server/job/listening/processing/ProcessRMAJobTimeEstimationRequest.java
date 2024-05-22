package org.greencloud.agentsystem.strategies.executiontimebased.server.job.listening.processing;

import static jade.lang.acl.ACLMessage.INFORM;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessRMAJobTimeEstimationRequest extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessRMAJobTimeEstimationRequest.class);

	public ProcessRMAJobTimeEstimationRequest(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(MESSAGE_CONTENT);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received request from {}. Estimating job {} execution time.",
				agentProps.getOwnerRegionalManagerAgent(), job.getJobId());

		final double estimatedExecutionTime = agentProps.getJobExecutionDuration(job, getCurrentTime());
		final ACLMessage response = MessageBuilder.builder(facts.get(RULE_SET_IDX))
				.copy(((ACLMessage) facts.get(MESSAGE)).createReply())
				.withStringContent(valueOf(estimatedExecutionTime))
				.withPerformative(INFORM)
				.build();
		agent.send(response);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("JOB_EXECUTION_TIME_ESTIMATION_HANDLER_RULE",
				"handling RMA requests asking to estimate job execution time",
				"when RMA sends requests to Server, it estimates job execution time");
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMAJobTimeEstimationRequest(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
