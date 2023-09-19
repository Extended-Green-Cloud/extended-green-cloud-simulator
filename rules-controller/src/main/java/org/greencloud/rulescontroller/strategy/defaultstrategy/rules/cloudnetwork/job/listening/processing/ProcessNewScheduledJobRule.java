package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.job.listening.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateCallForProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

public class ProcessNewScheduledJobRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessNewScheduledJobRule.class);

	public ProcessNewScheduledJobRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE,
				"handles new scheduled jobs",
				"rule run when CNA processes new job received from CNA");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return !agentProps.getOwnedActiveServers().isEmpty();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		facts.put(STRATEGY_IDX, controller.getLatestStrategy().get());
		final ClientJob job = facts.get(MESSAGE_CONTENT);

		MDC.put(MDC_AGENT_NAME, agent.getLocalName());
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Sending call for proposal to Server Agents for a job request with jobId {}!", job.getJobId());

		agentProps.addJob(job, facts.get(STRATEGY_IDX), PROCESSING);
		facts.put(JOB, job);
		agent.addBehaviour(InitiateCallForProposal.create(agent, facts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
	}
}
