package org.greencloud.agentsystem.strategies.deafult.rules.server.job.listening.jobprice.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_ENERGY_PRICE_RECEIVER_HANDLER_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceWithPrice;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessJobInstancePriceUpdateRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobInstancePriceUpdateRule.class);

	public ProcessJobInstancePriceUpdateRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_ENERGY_PRICE_RECEIVER_HANDLER_RULE,
				"handles job price update",
				"handling messages received from Green Source informing about job execution price");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobInstanceWithPrice jobInstanceWithPrice = facts.get(MESSAGE_CONTENT);
		final String jobId = jobInstanceWithPrice.getJobInstanceId().getJobId();

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received information about price for energy related to execution of job {}.", jobId);
		agentProps.updateJobEnergyCost(jobInstanceWithPrice);
	}

	@Override
	public AgentRule copy() {
		return new ProcessJobInstancePriceUpdateRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}

