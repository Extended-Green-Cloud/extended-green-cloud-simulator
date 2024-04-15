package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.weather;

import static com.database.knowledge.domain.agent.DataType.AVAILABLE_GREEN_ENERGY;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_CAUSE;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_IS_FINISHED;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.enums.event.PowerShortageCauseEnum.PHYSICAL_CAUSE;
import static org.greencloud.commons.enums.event.PowerShortageCauseEnum.WEATHER_CAUSE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.CHECK_WEATHER_PERIODICALLY_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.PERIODIC_WEATHER_CHECK_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.WeatherCheckMessageFactory.prepareWeatherCheckRequest;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.database.knowledge.domain.agent.greensource.AvailableGreenEnergy;

import jade.lang.acl.ACLMessage;

public class RequestWeatherPeriodicallyRule extends AgentRequestRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(RequestWeatherPeriodicallyRule.class);

	public RequestWeatherPeriodicallyRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(CHECK_WEATHER_PERIODICALLY_RULE,
				"check weather periodically",
				"rule verifies current weather after scheduled period of time");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, null);
		logger.info("Sending request for weather to Monitoring Agent");

		return prepareWeatherCheckRequest(agentProps, null,
				PERIODIC_WEATHER_CHECK_PROTOCOL,
				PERIODIC_WEATHER_CHECK_PROTOCOL,
				facts.get(RULE_SET_IDX));
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		try {
			final MonitoringData data = readMessageContent(inform, MonitoringData.class);
			final Instant time = getCurrentTime();
			final Instant realTime = convertToRealTime(time);
			final double availablePower = agentProps.getAvailableEnergy(realTime, data).orElse(-1.0);

			if (availablePower < 0 && !agentProps.getServerJobs().isEmpty()) {
				MDC.put(MDC_JOB_ID, null);
				logger.info(
						"Received the weather data at {}. There was a power drop! Scheduling job transferring behaviour!",
						time);

				final RuleSetFacts shortageFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
				shortageFacts.put(RULE_TYPE, POWER_SHORTAGE_ERROR_RULE);
				shortageFacts.put(EVENT_TIME, time);
				shortageFacts.put(EVENT_CAUSE, agentProps.isHasError() ? PHYSICAL_CAUSE : WEATHER_CAUSE);
				shortageFacts.put(EVENT_IS_FINISHED, false);
				shortageFacts.put(RESULT, availablePower);

				controller.fire(shortageFacts);
			} else {
				MDC.put(MDC_JOB_ID, null);
				logger.info("Received the weather data at {}. Power has not dropped. Continuing jobs execution", time);
			}
			reportAvailableEnergyData(agentProps.getAvailableGreenEnergy(data, time));
		} catch (IncorrectMessageContentException e) {
			MDC.put(MDC_JOB_ID, null);
			logger.info("The weather data is not available at {}", getCurrentTime());
		}
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, null);
		logger.info("The weather data is not available at {}", getCurrentTime());
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case does not apply here
	}

	private void reportAvailableEnergyData(final double availableEnergy) {
		final double energyPercentage = agentProps.getEnergyPercentage(availableEnergy);
		agentNode.writeMonitoringData(AVAILABLE_GREEN_ENERGY, new AvailableGreenEnergy(energyPercentage),
				agent.getName());
		agentNode.updateGreenEnergyAmount(availableEnergy);
	}
}
