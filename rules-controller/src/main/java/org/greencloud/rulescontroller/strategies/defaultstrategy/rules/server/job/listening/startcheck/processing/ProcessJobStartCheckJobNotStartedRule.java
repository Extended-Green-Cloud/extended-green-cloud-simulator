package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.job.listening.startcheck.processing;

import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_HANDLE_NOT_STARTED_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static jade.lang.acl.ACLMessage.REFUSE;

import java.util.Map;
import java.util.Optional;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class ProcessJobStartCheckJobNotStartedRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public ProcessJobStartCheckJobNotStartedRule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_HANDLER_RULE, JOB_STATUS_HANDLE_NOT_STARTED_RULE,
				"handles start check request - job not started",
				"processing CNA message checking job start status");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final Optional<Map.Entry<ClientJob, JobExecutionStatusEnum>> jobInstanceOpt = facts.get(JOB_ID);
		return jobInstanceOpt.isPresent() && !isJobStarted(jobInstanceOpt.orElseThrow().getValue());
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		agent.send(prepareStringReply(facts.get(MESSAGE), "JOB HAS NOT STARTED", REFUSE));
	}
}
