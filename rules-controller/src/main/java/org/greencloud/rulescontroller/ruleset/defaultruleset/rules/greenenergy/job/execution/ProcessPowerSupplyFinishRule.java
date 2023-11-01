package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.execution;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FINISH_JOB_ID;
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

public class ProcessPowerSupplyFinishRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessPowerSupplyFinishRule.class);

	private ServerJob job;
	private JobInstanceIdentifier jobInstance;

	public ProcessPowerSupplyFinishRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE,
				"handles power supply updates - finish",
				"handling new updates regarding provided power supply coming from Server");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		job = facts.get(JOB);
		jobInstance = facts.get(JOB_ID);
		final String type = facts.get(MESSAGE_TYPE);
		return type.equals(FINISH_JOB_ID);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		if (isJobStarted(job, agentProps.getServerJobs())) {
			agentProps.incrementJobCounter(jobInstance, FINISH);
		}
		logger.info("Finish the execution of the job {}", jobInstance);

		final RuleSetFacts jobRemoveFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		jobRemoveFacts.put(JOB, job);
		jobRemoveFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		controller.fire(jobRemoveFacts);
		agentProps.updateGUI();
	}
}
