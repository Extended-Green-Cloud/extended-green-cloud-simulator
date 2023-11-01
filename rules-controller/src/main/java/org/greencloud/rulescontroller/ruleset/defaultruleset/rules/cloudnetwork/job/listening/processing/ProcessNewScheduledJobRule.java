package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.AGENTS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateCallForProposal;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
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
		facts.put(RULE_SET_IDX, controller.getLatestRuleSet().get());
		final ClientJob job = facts.get(MESSAGE_CONTENT);

		MDC.put(MDC_AGENT_NAME, agent.getLocalName());
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Evaluating available server resources for job {}!", job.getJobId());
		agentProps.addJob(job, facts.get(RULE_SET_IDX), PROCESSING);

		final Map<String, Resource> availableAggregatedResources = agentProps.getAvailableResources(job, null);
		if (!areSufficient(availableAggregatedResources, job.getRequiredResources())) {
			logger.info("Not enough CNA resources for job {}!", job.getJobId());
			handleRejectedJob(job, facts);
			return;
		}

		final List<AID> consideredServers = selectServersForJob(job);
		if (consideredServers.isEmpty()) {
			logger.info("No servers with enough resources for job {}!", job.getJobId());
			handleRejectedJob(job, facts);
			return;
		}

		facts.put(JOB, job);
		facts.put(AGENTS, consideredServers);
		agent.addBehaviour(InitiateCallForProposal.create(agent, facts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
	}

	private List<AID> selectServersForJob(final ClientJob job) {
		return agentProps.getOwnedActiveServers().stream()
				.filter(server -> {
					final Map<String, Resource> availableResources = agentProps.getAvailableResources(job, server);
					return areSufficient(availableResources, job.getRequiredResources());
				})
				.toList();
	}

	private void handleRejectedJob(final ClientJob job, final RuleSetFacts facts) {
		final RuleSetFacts jobRemovalFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		jobRemovalFacts.put(JOB, job);
		controller.fire(jobRemovalFacts);

		agentProps.updateGUI();
		agent.send(prepareRefuseReply(facts.get(MESSAGE)));
	}
}
