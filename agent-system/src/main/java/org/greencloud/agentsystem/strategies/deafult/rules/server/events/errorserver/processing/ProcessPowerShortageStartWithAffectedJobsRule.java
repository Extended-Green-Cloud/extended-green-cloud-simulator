package org.greencloud.agentsystem.strategies.deafult.rules.server.events.errorserver.processing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_POWER_SHORTAGE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POWER_SHORTAGE_ERROR_START_REQUEST_TRANSFER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ALERT_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.SET_EVENT_ERROR;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessPowerShortageStartWithAffectedJobsRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageStartWithAffectedJobsRule.class);

	public ProcessPowerShortageStartWithAffectedJobsRule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_RULE, POWER_SHORTAGE_ERROR_START_REQUEST_TRANSFER_RULE,
				"handle power shortage start event - request transfer",
				"rule handles start of power shortage event");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final List<ClientJob> affectedJobs = facts.get(JOBS);
		return nonNull(affectedJobs) && !affectedJobs.isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> affectedJobs = facts.get(JOBS);

		affectedJobs.forEach(job -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Job {} is affected by the server break down!", job.getJobId());

			agent.send(prepareNetworkFailureInformation(job, INTERNAL_SERVER_ERROR_ALERT_PROTOCOL,
					facts.get(RULE_SET_IDX), agentProps.getGreenSourceForJobMap().get(job.getJobId())));
		});
		facts.put(JOBS, affectedJobs);
		facts.put(SET_EVENT_ERROR, true);
		agent.addBehaviour(ScheduleOnce.create(agent, facts, HANDLE_POWER_SHORTAGE_RULE, controller,
				SELECT_BY_FACTS_IDX));
	}

	@Override
	public AgentRule copy() {
		return new ProcessPowerShortageStartWithAffectedJobsRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
