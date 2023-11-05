package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.proposing;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.COMPUTE_FINAL_PRICE;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RESOURCES;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SCHEDULE_POWER_SUPPLY_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROPOSE_TO_EXECUTE_JOB_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceIdAndServer;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.factory.OfferMessageFactory.prepareGreenEnergyPowerSupplyOffer;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.extended.JobWithProtocol;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentProposalRule;
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
		final double energyCost =
				convertToHourDuration(job.getStartTime(), job.getEndTime()) * agentProps.getPricePerPowerUnit();
		agentProps.getPriceForJob().put(job,  agentProps.getPricePerPowerUnit());
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
			agentProps.incrementJobCounter(jobInstance, ACCEPTED);

			final RuleSetFacts acceptFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			acceptFacts.put(JOB, job);
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
			final RuleSetFacts finishFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			finishFacts.put(JOB, facts.get(JOB));
			finishFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
			finishFacts.put(COMPUTE_FINAL_PRICE, false);
			controller.fire(finishFacts);
		}

		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		logger.info("Server rejected the job proposal");
	}
}
