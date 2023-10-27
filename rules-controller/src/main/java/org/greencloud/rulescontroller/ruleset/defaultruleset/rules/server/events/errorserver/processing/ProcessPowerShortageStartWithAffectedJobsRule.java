package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.errorserver.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOBS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.SET_EVENT_ERROR;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_POWER_SHORTAGE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_START_REQUEST_TRANSFER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_START_RULE;
import static org.greencloud.commons.enums.rules.RuleType.TRANSFER_JOB_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToPowerShortageJob;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ALERT_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static org.greencloud.rulescontroller.ruleset.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobDivided;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.gui.agents.server.ServerNode;

public class ProcessPowerShortageStartWithAffectedJobsRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageStartWithAffectedJobsRule.class);

	public ProcessPowerShortageStartWithAffectedJobsRule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_START_RULE,
				POWER_SHORTAGE_ERROR_START_REQUEST_TRANSFER_RULE,
				"handle power shortage start event - request transfer",
				"rule handles start of power shortage event");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final List<ClientJob> affectedJobs = facts.get(JOBS);
		return !affectedJobs.isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> affectedJobs = facts.get(JOBS);
		final Instant startTime = facts.get(EVENT_TIME);

		affectedJobs.forEach(job -> {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Requesting job {} transfer in Cloud Network", job.getJobId());

			final RuleSetFacts divisionFacts = agentProps.constructDivisionFacts(job, startTime,
					facts.get(RULE_SET_IDX));
			controller.fire(divisionFacts);
			final JobDivided<ClientJob> instances = divisionFacts.get(RESULT);
			final JobPowerShortageTransfer jobTransfer = mapToPowerShortageJob(job.getJobInstanceId(), instances,
					startTime);

			final RuleSetFacts initiatorFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			initiatorFacts.put(JOB, jobTransfer);
			initiatorFacts.put(JOB_ID, JobMapper.mapClientJobToJobInstanceId(instances.getSecondInstance()));

			agent.addBehaviour(InitiateRequest.create(agent, initiatorFacts, TRANSFER_JOB_RULE, controller));
			agent.send(prepareNetworkFailureInformation(jobTransfer, INTERNAL_SERVER_ERROR_ALERT_PROTOCOL,
					initiatorFacts.get(RULE_SET_IDX),
					agentProps.getGreenSourceForJobMap().get(job.getJobId())));
		});

		facts.put(JOBS, affectedJobs);
		facts.put(SET_EVENT_ERROR, true);
		agent.addBehaviour(
				ScheduleOnce.create(agent, facts, HANDLE_POWER_SHORTAGE_RULE, controller, SELECT_BY_FACTS_IDX));
	}
}
