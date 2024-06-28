package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.weather;

import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ON_HOLD;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.CHECK_WEATHER_FOR_RE_SUPPLY_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.RE_SUPPLY_SUCCESSFUL_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.WEATHER_UNAVAILABLE_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.greencloud.commons.utils.messaging.factory.WeatherCheckMessageFactory.prepareWeatherCheckRequest;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class RequestWeatherToVerifyEnergyReSupplyRule extends AgentRequestRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(RequestWeatherToVerifyEnergyReSupplyRule.class);

	public RequestWeatherToVerifyEnergyReSupplyRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(CHECK_WEATHER_FOR_RE_SUPPLY_RULE,
				"check current weather conditions for re-supply",
				"rule communicates with Monitoring to check weather conditions and verify if job can be re-supplied with green energy");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Sending request for weather to Monitoring Agent");

		return prepareWeatherCheckRequest(agentProps, job,
				SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL,
				SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL,
				facts.get(RULE_SET_IDX));
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final ACLMessage request = facts.get(MESSAGE);
		try {
			final MonitoringData data = readMessageContent(inform, MonitoringData.class);
			final double availableEnergy = agentProps.getAvailableEnergy(job, data, false).orElse(0D);
			final double energyForJob = job.getEstimatedEnergy();

			if (energyForJob > availableEnergy) {
				MDC.put(MDC_JOB_ID, job.getJobId());
				MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
				logger.info(
						"There is not enough available power (needed {}, have {}). Job {} cannot be supplied with green energy",
						energyForJob, availableEnergy, job.getJobId());
				agent.send(prepareStringReply(request, NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE, REFUSE));
			} else {
				if (agentProps.getServerJobs().containsKey(job)) {
					logger.info("Job {} is being supplied again using the green energy", job.getJobId());
					final boolean isActive = agentProps.getServerJobs().get(job).equals(ON_HOLD);
					final JobExecutionStatusEnum newStatus = EXECUTING_ON_GREEN.getStatus(isActive);
					final JobExecutionStatusEnum prevStatus = agentProps.getServerJobs().get(job);

					agentProps.getJobsExecutionTime()
							.updateJobExecutionDuration(job, prevStatus, newStatus, getCurrentTime());
					agentProps.getServerJobs().replace(job, EXECUTING_ON_GREEN.getStatus(isActive));
					agentProps.updateGUI();
					agent.send(prepareStringReply(request, RE_SUPPLY_SUCCESSFUL_MESSAGE, INFORM));
				} else {
					logger.info("Job {} cannot be supplied with green energy - job not found", job.getJobId());
					agent.send(prepareStringReply(request, JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));
				}
			}
		} catch (IncorrectMessageContentException e) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("The data for the job is not available. Leaving job {} on hold", job.getJobId());
		}
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final ACLMessage request = facts.get(MESSAGE);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("The data for the job is not available. Job {} cannot be supplied with green energy",
				job.getJobId());

		agent.send(prepareStringReply(request, WEATHER_UNAVAILABLE_CAUSE_MESSAGE, FAILURE));
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case does not apply here
	}

	@Override
	public AgentRule copy() {
		return new RequestWeatherToVerifyEnergyReSupplyRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
