package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.CHECK_WEATHER_FOR_RE_SUPPLY_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_SERVER_RE_SUPPLY_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceIdAndServer;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.util.Objects.isNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

import jade.lang.acl.ACLMessage;

public class ProcessReSupplyRequestRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessReSupplyRequestRule.class);

	public ProcessReSupplyRequestRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_SERVER_RE_SUPPLY_HANDLER_RULE,
				"handling information about Server error",
				"handling different types of information regarding possible Server errors");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final JobInstanceIdentifier jobInstanceId = facts.get(MESSAGE_CONTENT);
		final ACLMessage message = facts.get(MESSAGE);
		final ServerJob jobToCheck = getJobByInstanceIdAndServer(jobInstanceId.getJobInstanceId(), message.getSender(),
				agentProps.getServerJobs());

		if (isNull(jobToCheck)) {
			MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
			logger.info("Job {} is no longer existing in given green energy source", jobInstanceId.getJobId());
			agent.send(prepareStringReply(message, JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));

			return false;
		}
		facts.put(JOB, jobToCheck);
		return true;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ServerJob jobToCheck = facts.get(JOB);
		final ACLMessage message = facts.get(MESSAGE);

		MDC.put(MDC_JOB_ID, jobToCheck.getJobId());
		logger.info("Verifying if job {} can be supplied with green energy", jobToCheck.getJobId());

		final RuleSetFacts weatherCheckFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		weatherCheckFacts.put(JOB, jobToCheck);
		weatherCheckFacts.put(MESSAGE, message);
		agent.addBehaviour(
				InitiateRequest.create(agent, weatherCheckFacts, CHECK_WEATHER_FOR_RE_SUPPLY_RULE, controller));
	}
}
