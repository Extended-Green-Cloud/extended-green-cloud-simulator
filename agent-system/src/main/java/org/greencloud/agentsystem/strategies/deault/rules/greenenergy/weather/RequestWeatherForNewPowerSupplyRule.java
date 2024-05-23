package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.weather;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.CHECK_WEATHER_FOR_NEW_POWER_SUPPLY_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobRemovalWithPrice;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.greencloud.commons.utils.messaging.factory.WeatherCheckMessageFactory.prepareWeatherCheckRequest;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESOURCES;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateProposal;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class RequestWeatherForNewPowerSupplyRule extends AgentRequestRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(RequestWeatherForNewPowerSupplyRule.class);

	public RequestWeatherForNewPowerSupplyRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(CHECK_WEATHER_FOR_NEW_POWER_SUPPLY_RULE,
				"check current weather conditions",
				"rule communicates with Monitoring to check weather conditions for new power supply request from Server");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final ACLMessage cfp = facts.get(MESSAGE);
		final String protocol = cfp.getProtocol();
		final String conversationId = cfp.getConversationId();
		final ServerJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Sending request for weather to Monitoring Agent");

		return prepareWeatherCheckRequest(agentProps, job, conversationId, protocol, facts.get(RULE_SET_IDX));
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		try {
			final MonitoringData data = readMessageContent(inform, MonitoringData.class);
			final Optional<Double> availablePower = agentProps.getAvailableEnergy(job, data, true);
			final String jobId = job.getJobId();
			final double energyForJob = job.getEstimatedEnergy();

			if (availablePower.isEmpty()) {
				MDC.put(MDC_JOB_ID, job.getJobId());
				MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
				logger.info("Too bad weather conditions, sending refuse message to server for job with id {}.", jobId);
				handleRefusal(job, facts);
			} else if (energyForJob > availablePower.get()) {
				MDC.put(MDC_JOB_ID, job.getJobId());
				MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
				logger.info("Refusing job with id {} - not enough available energy. Needed {}, available {}", jobId,
						energyForJob, availablePower.get());
				handleRefusal(job, facts);
			} else {
				MDC.put(MDC_JOB_ID, job.getJobId());
				MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
				logger.info("Replying with propose message to server for job with id {}.", jobId);
				final double power = availablePower.get();

				final RuleSetFacts offerFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
				offerFacts.put(RESULT, power);
				offerFacts.put(JOB, job);
				offerFacts.put(MESSAGE, facts.get(MESSAGE));
				offerFacts.put(RESOURCES, data);

				agent.addBehaviour(InitiateProposal.create(agent, offerFacts, PROPOSE_TO_EXECUTE_JOB_RULE, controller));
			}
		} catch (IncorrectMessageContentException e) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("I didn't understand the response with the weather data, sending refuse message to server");
			handleRefusal(job, facts);
		}
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Weather data not available, sending refuse message to server.");
		handleRefusal(job, facts);
	}

	private void handleRefusal(final ServerJob job, final RuleSetFacts facts) {
		controller.fire(constructFactsForJobRemovalWithPrice(facts.get(RULE_SET_IDX), job, false));
		agent.send(prepareRefuseReply(facts.get(MESSAGE)));
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		// case does not apply here
	}

	@Override
	public AgentRule copy() {
		return new RequestWeatherForNewPowerSupplyRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
