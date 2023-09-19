package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.dividejob;

import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOBS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_DIVIDED;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_JOB_NEW_INSTANCE_CREATION_RULE;
import static java.util.Objects.nonNull;

import java.time.Instant;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

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
	public void executeRule(final StrategyFacts facts) {
		final JobPowerShortageTransfer jobTransfer = facts.get(JOBS);
		final Instant startTime = facts.get(EVENT_TIME);
		final ClientJob job = facts.get(JOB);

		StrategyFacts newFacts;

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
}
