package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.jobupdate.processing;

import static jade.lang.acl.ACLMessage.INFORM;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ENERGY_TYPE;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.energy.EnergyTypeEnum.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_CONFIRM_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessUpdateFromGreenSourceJobConfirmationRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessUpdateFromGreenSourceJobConfirmationRule.class);

	public ProcessUpdateFromGreenSourceJobConfirmationRule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 4);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_CONFIRM_RULE,
				"handles job confirmation update",
				"handling messages received from Green Source informing about job confirmation");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return message.getPerformative() == INFORM && message.getProtocol().equals(SERVER_JOB_CFP_PROTOCOL);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final JobInstanceIdentifier jobInstance = facts.get(MESSAGE_CONTENT);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Scheduling the execution of the job {}.", jobInstance.getJobId());

		facts.put(ENERGY_TYPE, GREEN_ENERGY);
		agent.addBehaviour(ScheduleOnce.create(agent, facts, START_JOB_EXECUTION_RULE, controller,
				SELECT_BY_FACTS_IDX));
	}

	@Override
	public AgentRule copy() {
		return new ProcessUpdateFromGreenSourceJobConfirmationRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}

