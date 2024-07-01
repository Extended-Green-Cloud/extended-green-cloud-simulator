package org.greencloud.agentsystem.strategies.rulesets.allocation.budgetdeadlineonestep.rules.regionalmanager.job.listening.allocation;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.BUDGET;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.DURATION;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.START_TIME;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForRMADataPreparation;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.messaging.factory.RequestMessageFactory.requestDataForAllocationAllocatedJobs;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class RequestServersForJobExecutionEstimationRule extends AgentRequestRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(RequestServersForJobExecutionEstimationRule.class);

	public RequestServersForJobExecutionEstimationRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_REQUEST_RULE,
				"sends request for job execution estimation to servers.",
				"when next job is to be allocated, RMA asks servers to estimate its execution.");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		return requestDataForAllocationAllocatedJobs(facts, agentProps.getOwnedActiveServers());
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final RuleSetFacts facts) {
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Preparing information about regional job execution estimations.");

		final RuleSetFacts dataFacts = constructFactsForRMADataPreparation(facts.get(RULE_SET_IDX),
				List.of(BUDGET, DURATION, START_TIME), informs, facts.get(JOBS));
		controller.fire(dataFacts);

		agent.send(prepareReply(facts.get(MESSAGE), dataFacts.get(RESULT), facts.get(MESSAGE_TYPE)));
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Server {} refused to execute given job.", refuse.getSender().getLocalName());
	}

	@Override
	public AgentRule copy() {
		return new RequestServersForJobExecutionEstimationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
