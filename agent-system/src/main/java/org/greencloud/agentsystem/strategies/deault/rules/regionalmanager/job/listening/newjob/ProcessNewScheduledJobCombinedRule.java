package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.newjob;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.newjob.processing.ProcessNewScheduledJobNoServersRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.listening.newjob.processing.ProcessNewScheduledJobRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessNewScheduledJobCombinedRule extends AgentCombinedRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessNewScheduledJobCombinedRule.class);

	public ProcessNewScheduledJobCombinedRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handles new scheduled jobs",
				"rule run when RMA processes new job received from CBA");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNewScheduledJobNoServersRule(controller),
				new ProcessNewScheduledJobRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(RuleSetFacts facts) {
		final ClientJob job = facts.get(MESSAGE_CONTENT);

		MDC.put(MDC_AGENT_NAME, agent.getLocalName());
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf(controller.getLatestLongTermRuleSetIdx().get()));
		logger.info("Evaluating available server resources for job {}!", job.getJobId());

		if (!agentProps.getOwnedActiveServers().isEmpty() && agentProps.selectServersForJob(job).isEmpty()) {
			logger.info("No servers with enough resources for job {}!", job.getJobId());
			agentProps.updateGUI();
			agent.send(prepareRefuseReply(facts.get(MESSAGE)));

			return false;
		}
		return true;
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewScheduledJobCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
