package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.listening;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceIdAndServer;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.execution.ProcessPowerSupplyFinishRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.execution.ProcessPowerSupplyStartRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessPowerSupplyStatusUpdateRule extends AgentCombinedRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessPowerSupplyStatusUpdateRule.class);

	public ProcessPowerSupplyStatusUpdateRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE,
				"handles power supply updates",
				"handling new updates regarding provided power supply coming from Server");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessPowerSupplyFinishRule(controller),
				new ProcessPowerSupplyStartRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);
		final ACLMessage message = facts.get(MESSAGE);
		final JobInstanceIdentifier jobInstanceId = jobStatusUpdate.getJobInstance();
		final ServerJob serverJob = getJobByInstanceIdAndServer(jobInstanceId.getJobInstanceId(),
				message.getSender(), agentProps.getServerJobs());

		if (nonNull(serverJob)) {
			facts.put(JOB_ID, jobInstanceId);
			facts.put(JOB, serverJob);
			return true;
		}
		MDC.put(MDC_JOB_ID, jobStatusUpdate.getJobInstance().getJobId());
		logger.info("Job {} was not found.", jobInstanceId.getJobInstanceId());

		return false;
	}
}
