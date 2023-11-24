package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.AgentType.CLOUD_NETWORK;
import static org.greencloud.commons.constants.FactTypeConstants.AGENTS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.greencloud.rulescontroller.ruleset.RuleSetConstructor.constructRuleSetForCustomClientComparison;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateCallForProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.RuleSetRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class ProcessNewScheduledJobRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessNewScheduledJobRule.class);

	public ProcessNewScheduledJobRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE,
				"handles new scheduled jobs",
				"rule run when CNA processes new job received from CNA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return !agentProps.getOwnedActiveServers().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		facts.put(RULE_SET_IDX, controller.getLatestLongTermRuleSetIdx().get());
		final ClientJob job = facts.get(MESSAGE_CONTENT);
		int newRuleSetIdx = facts.get(RULE_SET_IDX);

		MDC.put(MDC_AGENT_NAME, agent.getLocalName());
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf(newRuleSetIdx));
		logger.info("Evaluating available server resources for job {}!", job.getJobId());

		final List<AID> consideredServers = agentProps.selectServersForJob(job);
		if (consideredServers.isEmpty()) {
			logger.info("No servers with enough resources for job {}!", job.getJobId());
			agentProps.updateGUI();
			agent.send(prepareRefuseReply(facts.get(MESSAGE)));
			return;
		}

		if (nonNull(job.getSelectionPreference())) {
			final String log = "Comparing Server offers using custom comparator";
			final String ruleSetName = "CUSTOM_CLIENT_COMPARATOR_" + job.getJobId();
			final RuleSetRest rules = constructRuleSetForCustomClientComparison(job.getSelectionPreference(),
					ruleSetName, log, job.getJobId(), CLOUD_NETWORK);
			newRuleSetIdx = controller.getLatestRuleSetIdx().get() + 1;

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf(newRuleSetIdx));
			logger.info("Client with job {} requested to use custom server comparison. Adding rule set {}",
					job.getJobId(), ruleSetName);

			final RuleSet modifications = new RuleSet(rules);
			controller.addModifiedTemporaryRuleSetFromCurrent(modifications, newRuleSetIdx);
		}
		agentProps.addJob(job, newRuleSetIdx, PROCESSING);

		final RuleSetFacts cfpFacts = new RuleSetFacts(newRuleSetIdx);
		cfpFacts.put(JOB, job);
		cfpFacts.put(AGENTS, consideredServers);
		cfpFacts.put(MESSAGE, facts.get(MESSAGE));
		agent.addBehaviour(InitiateCallForProposal.create(agent, cfpFacts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
	}
}
