package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.dividejob;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_DIVIDED;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_JOB_NEW_INSTANCE_CREATION_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import java.time.Instant;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessJobNewInstanceCreationRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public ProcessJobNewInstanceCreationRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_JOB_NEW_INSTANCE_CREATION_RULE,
				"create new job instances",
				"rule creates new instances of the given job");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobPowerShortageTransfer jobTransfer = facts.get(JOBS);
		final Instant startTime = facts.get(EVENT_TIME);
		final ClientJob job = facts.get(JOB);

		RuleSetFacts newFacts;

		if (nonNull(jobTransfer)) {
			newFacts = agentProps.divideJobForTransfer(jobTransfer, job, facts);
			controller.fire(newFacts);
		} else {
			newFacts = agentProps.divideJobForTransfer(job, startTime, facts);
			if (newFacts.asMap().containsKey(RULE_TYPE)) {
				controller.fire(newFacts);
			}
		}
		agentProps.updateGUI();
		facts.put(RESULT, newFacts.get(JOB_DIVIDED));
	}

	@Override
	public AgentRule copy() {
		return new ProcessJobNewInstanceCreationRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
