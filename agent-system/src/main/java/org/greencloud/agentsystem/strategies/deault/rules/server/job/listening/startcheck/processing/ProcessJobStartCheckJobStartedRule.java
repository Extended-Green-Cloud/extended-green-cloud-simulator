package org.greencloud.agentsystem.strategies.deault.rules.server.job.listening.startcheck.processing;

import static jade.lang.acl.ACLMessage.INFORM;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_HANDLE_STARTED_RULE;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;

import java.util.Map;
import java.util.Optional;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessJobStartCheckJobStartedRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public ProcessJobStartCheckJobStartedRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_HANDLER_RULE, JOB_STATUS_HANDLE_STARTED_RULE,
				"handles start check request - job started",
				"processing RMA message checking job start status");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final Optional<Map.Entry<ClientJob, JobExecutionStatusEnum>> jobInstanceOpt = facts.get(JOB_ID);

		jobInstanceOpt.ifPresent(jobEntry -> facts.put(JOB, jobEntry));
		return jobInstanceOpt.isPresent() && isJobStarted(jobInstanceOpt.orElseThrow().getValue());
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final Map.Entry<ClientJob, JobExecutionStatusEnum> jobInstanceEntry = facts.get(JOB);
		agent.send(prepareReply(facts.get(MESSAGE), jobInstanceEntry.getKey(), INFORM));
	}

	@Override
	public AgentRule copy() {
		return new ProcessJobStartCheckJobStartedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
