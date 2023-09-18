package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.weather;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.CHECK_WEATHER_FOR_POWER_SHORTAGE_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NOT_ENOUGH_ENERGY_FOR_JOB_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.NETWORK_ERROR_FINISH_ALERT_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.ON_HOLD_JOB_CHECK_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static org.greencloud.commons.utils.messaging.factory.WeatherCheckMessageFactory.prepareWeatherCheckRequest;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static java.lang.String.join;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.exception.IncorrectMessageContentException;
import com.gui.agents.greenenergy.GreenEnergyNode;

import jade.lang.acl.ACLMessage;

public class RequestWeatherToCheckEnergyAfterPowerShortageRule
		extends AgentRequestRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(RequestWeatherToCheckEnergyAfterPowerShortageRule.class);

	public RequestWeatherToCheckEnergyAfterPowerShortageRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(CHECK_WEATHER_FOR_POWER_SHORTAGE_FINISH_RULE,
				"check current weather conditions",
				"rule communicates with Monitoring to check weather conditions and verify if job can be supplied with green energy");
	}

	@Override
	protected ACLMessage createRequestMessage(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Sending request for weather to Monitoring Agent");

		final String conversationId = join("_", job.getJobId(), job.getStartTime().toString());
		return prepareWeatherCheckRequest(agentProps, job, conversationId, ON_HOLD_JOB_CHECK_PROTOCOL,
				facts.get(STRATEGY_IDX));
	}

	@Override
	protected void handleInform(final ACLMessage inform, final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		try {
			final MonitoringData data = readMessageContent(inform, MonitoringData.class);
			final Optional<Double> availableEnergy = agentProps.getAvailableEnergy(job, data, false);

			if (availableEnergy.isEmpty() || job.getEstimatedEnergy() > availableEnergy.get()) {
				final StrategyFacts newFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
				newFacts.put(JOB, job);
				newFacts.put(RULE_TYPE, NOT_ENOUGH_ENERGY_FOR_JOB_RULE);
				controller.fire(newFacts);
			} else {
				logger.info("Changing the status of the job {}", job.getJobId());
				final boolean isJobStarted = isJobStarted(job, agentProps.getServerJobs());
				final JobExecutionStatusEnum newStatus = EXECUTING_ON_GREEN.getStatus(isJobStarted);

				agentProps.getServerJobs().replace(job, newStatus);
				agentProps.updateGUI();
				agent.send(prepareNetworkFailureInformation(mapToJobInstanceId(job),
						NETWORK_ERROR_FINISH_ALERT_PROTOCOL, facts.get(STRATEGY_IDX), job.getServer()));
			}
		} catch (IncorrectMessageContentException e) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			logger.info("The data for the job is not available. Leaving job {} on hold", job.getJobId());
		}
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("The data for the job is not available. Leaving job {} on hold", job.getJobId());
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final StrategyFacts facts) {
		// case does not apply here
	}
}
