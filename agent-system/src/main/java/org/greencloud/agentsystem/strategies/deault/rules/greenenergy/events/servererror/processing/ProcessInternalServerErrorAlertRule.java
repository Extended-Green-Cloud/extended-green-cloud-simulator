package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.servererror.processing;

import static java.util.Objects.nonNull;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_SERVER_ERROR_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_SERVER_ERROR_HANDLE_NEW_ALERT_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceIdAndServer;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ALERT_PROTOCOL;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessInternalServerErrorAlertRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessInternalServerErrorAlertRule.class);

	private ACLMessage message;

	public ProcessInternalServerErrorAlertRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_SERVER_ERROR_HANDLER_RULE,
				LISTEN_FOR_SERVER_ERROR_HANDLE_NEW_ALERT_RULE,
				"handling information about Server error",
				"handling different types of information regarding possible Server errors");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		message = facts.get(MESSAGE);
		return message.getProtocol().equals(INTERNAL_SERVER_ERROR_ALERT_PROTOCOL);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobPowerShortageTransfer jobTransfer = readMessageContent(message, JobPowerShortageTransfer.class);
		final String jobInstanceId = jobTransfer.getOriginalJobInstanceId();
		final String jobId = jobTransfer.getSecondJobInstanceId().getJobId();

		final ServerJob affectedJob = getJobByInstanceIdAndServer(jobInstanceId, message.getSender(),
				agentProps.getServerJobs());

		MDC.put(MDC_JOB_ID, jobId);
		if (nonNull(affectedJob)) {
			logger.info("Received information about job {} power shortage in server. Updating green source state",
					affectedJob.getJobId());
			final RuleSetFacts divisionFacts = agentProps.divideJobForPowerShortage(jobTransfer, affectedJob,
					facts);
			controller.fire(divisionFacts);
		} else {
			logger.info("Job {} to divide due to power shortage was not found", jobId);
		}
	}
}
