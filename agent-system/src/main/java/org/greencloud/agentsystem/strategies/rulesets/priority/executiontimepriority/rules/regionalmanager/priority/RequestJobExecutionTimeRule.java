package org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.regionalmanager.priority;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.Double.parseDouble;
import static java.util.Comparator.comparingDouble;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ASK_FOR_FASTEST_EXECUTION_TIME;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.REQUEST_JOB_EXECUTION_TIME;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareFailureReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;

import java.util.Collection;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;

import jade.lang.acl.ACLMessage;

public class RequestJobExecutionTimeRule extends AgentRequestRule<RegionalManagerAgentProps, RMANode> {

	public RequestJobExecutionTimeRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withMessageProtocol(REQUEST_JOB_EXECUTION_TIME)
				.withObjectContent(job)
				.withReceivers(agentProps.getOwnedActiveServers())
				.build();
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final RuleSetFacts facts) {
		final ACLMessage fastestExecutionTime = ofNullable(informs)
				.filter(messages -> !messages.isEmpty())
				.flatMap(messages -> messages.stream().min(comparingDouble(msg -> parseDouble(msg.getContent()))))
				.orElse(null);

		ofNullable(fastestExecutionTime).ifPresentOrElse(
				msg -> processBestMessage(msg, facts),
				() -> handleNoMessages(facts));
	}

	private void processBestMessage(final ACLMessage msg, final RuleSetFacts facts) {
		final double estimatedExecutionTime = parseDouble(msg.getContent());
		agent.send(MessageBuilder.builder(facts.get(RULE_SET_IDX))
				.copy(((ACLMessage) facts.get(MESSAGE)).createReply())
				.withObjectContent(estimatedExecutionTime)
				.withPerformative(INFORM)
				.build());
	}

	private void handleNoMessages(final RuleSetFacts facts) {
		agent.send(prepareFailureReply(facts.get(MESSAGE)));
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(ASK_FOR_FASTEST_EXECUTION_TIME,
				"ask Servers for estimations of the fastest job execution time",
				"evaluate job execution time options to evaluate possibly the fastest option");
	}

	@Override
	public AgentRule copy() {
		return new RequestJobExecutionTimeRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
