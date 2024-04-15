package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.errorhandling.listening.processing;

import static jade.lang.acl.ACLMessage.REFUSE;
import static java.lang.String.valueOf;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENTS;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLE_TRANSFER_NO_SERVERS_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLE_TRANSFER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.NO_SERVER_AVAILABLE_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessTransferRequestNoAgentsRule extends AgentBasicRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(ProcessTransferRequestNoAgentsRule.class);

	protected ProcessTransferRequestNoAgentsRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_HANDLE_TRANSFER_RULE,
				LISTEN_FOR_JOB_TRANSFER_HANDLE_TRANSFER_NO_SERVERS_RULE,
				"transfer job handler - no available agents",
				"handles request to transfer job from one Server to another when there are no agents");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final List<AID> agents = facts.get(AGENTS);
		return agents.isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final ACLMessage request = facts.get(MESSAGE);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));

		logger.info("No servers available. Passing the information to client and server");
		agent.send(prepareStringReply(request, NO_SERVER_AVAILABLE_CAUSE_MESSAGE, REFUSE));
	}
}

