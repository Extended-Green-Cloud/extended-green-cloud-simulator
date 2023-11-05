package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.RuleType.JOB_ENERGY_PRICE_RECEIVER_HANDLER_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.instance.JobInstanceWithPrice;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessJobPriceUpdateRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessJobPriceUpdateRule.class);

	public ProcessJobPriceUpdateRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_ENERGY_PRICE_RECEIVER_HANDLER_RULE,
				"handles job price update",
				"handling messages received from Server informing about job execution price");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobInstanceWithPrice jobInstanceWithPrice = facts.get(MESSAGE_CONTENT);
		final String jobId = jobInstanceWithPrice.getJobInstanceId().getJobId();

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received information about cost {} related to execution of job {} (instance: {}).",
				jobInstanceWithPrice.getPrice(), jobId, jobInstanceWithPrice.getJobInstanceId());
		agentProps.updatePriceForJob(jobId, jobInstanceWithPrice.getPrice());
	}
}

