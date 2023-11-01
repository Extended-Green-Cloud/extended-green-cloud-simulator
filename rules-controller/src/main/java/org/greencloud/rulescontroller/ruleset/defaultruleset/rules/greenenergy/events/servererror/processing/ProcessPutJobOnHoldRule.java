package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.servererror.processing;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_HOLD;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_SERVER_ERROR_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_SERVER_ERROR_HANDLE_PUT_ON_HOLD_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceIdAndServer;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessPutJobOnHoldRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessPutJobOnHoldRule.class);

	private ACLMessage message;

	public ProcessPutJobOnHoldRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_SERVER_ERROR_HANDLER_RULE,
				LISTEN_FOR_SERVER_ERROR_HANDLE_PUT_ON_HOLD_RULE,
				"handling request to put job on hold",
				"handling different types of information regarding possible Server errors");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		message = facts.get(MESSAGE);
		return message.getProtocol().equals(INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(message, JobInstanceIdentifier.class);
		final ServerJob job = getJobByInstanceIdAndServer(jobInstanceId.getJobInstanceId(), message.getSender(),
				agentProps.getServerJobs());

		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		if (nonNull(job)) {
			logger.info("Received information about job {} transfer failure. Putting job on hold",
					jobInstanceId.getJobId());
			final boolean hasStarted = isJobStarted(job, agentProps.getServerJobs());

			agentProps.getServerJobs().replace(job, EXECUTING_ON_HOLD.getStatus(hasStarted));
			agentProps.updateGUI();
		} else {
			logger.info("Job {} to put on hold was not found", jobInstanceId.getJobId());
		}
	}
}
