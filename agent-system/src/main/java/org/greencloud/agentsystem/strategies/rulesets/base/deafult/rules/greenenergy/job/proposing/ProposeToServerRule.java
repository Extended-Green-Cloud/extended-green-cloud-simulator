package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.proposing;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SCHEDULE_POWER_SUPPLY_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJob;
import static org.greencloud.commons.utils.facts.JobUpdateFactsFactory.constructFactsForJobRemovalWithPrice;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceIdAndServer;
import static org.greencloud.commons.utils.messaging.factory.OfferMessageFactory.prepareGreenEnergyPowerSupplyOffer;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESOURCES;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.extended.JobWithProtocol;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentProposalRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProposeToServerRule extends AgentProposalRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProposeToServerRule.class);

	public ProposeToServerRule(final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROPOSE_TO_EXECUTE_JOB_RULE,
				"propose job execution to Server",
				"rule sends proposal message to Server and handles the response");
	}

	@Override
	protected ACLMessage createProposalMessage(final RuleSetFacts facts) {
		final ServerJob job = facts.get(JOB);
		final double availablePower = facts.get(RESULT);
		final double energyCost = convertToHourDuration(job.getDuration()) * agentProps.getPricePerPowerUnit();
		agentProps.getPriceForJob().put(job, agentProps.getPricePerPowerUnit());
		return prepareGreenEnergyPowerSupplyOffer(energyCost, availablePower,
				agentProps.computeCombinedPowerError(job), job.getJobId(), facts.get(MESSAGE));
	}

	@Override
	protected void handleAcceptProposal(final ACLMessage accept, final RuleSetFacts facts) {
		final JobWithProtocol jobWithProtocol = readMessageContent(accept, JobWithProtocol.class);
		final JobInstanceIdentifier jobInstance = jobWithProtocol.getJobInstanceIdentifier();
		final ServerJob job = getJobByInstanceIdAndServer(jobInstance.getJobInstanceId(), accept.getSender(),
				agentProps.getServerJobs());

		if (nonNull(job)) {
			final Optional<Double> averageAvailablePower = agentProps.getAvailableEnergy(job, facts.get(RESOURCES),
					true);
			agentProps.incrementJobCounter(job.getJobId(), ACCEPTED);

			final RuleSetFacts acceptFacts = constructFactsWithJob(facts.get(RULE_SET_IDX), job);
			acceptFacts.put(RESULT, averageAvailablePower);
			acceptFacts.put(JOB_ID, jobWithProtocol);
			acceptFacts.put(MESSAGE, accept);
			acceptFacts.put(RULE_TYPE, PROCESS_SCHEDULE_POWER_SUPPLY_RULE);

			controller.fire(acceptFacts);
		}
	}

	@Override
	protected void handleRejectProposal(final ACLMessage reject, final RuleSetFacts facts) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(reject, JobInstanceIdentifier.class);
		final ServerJob serverJob = getJobByInstanceIdAndServer(jobInstanceId.getJobInstanceId(),
				reject.getSender(), agentProps.getServerJobs());

		if (nonNull(serverJob)) {
			controller.fire(constructFactsForJobRemovalWithPrice(facts.get(RULE_SET_IDX), facts.get(JOB), false));
		}

		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		logger.info("Server rejected the job proposal");
	}

	@Override
	public AgentRule copy() {
		return new ProposeToServerRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
