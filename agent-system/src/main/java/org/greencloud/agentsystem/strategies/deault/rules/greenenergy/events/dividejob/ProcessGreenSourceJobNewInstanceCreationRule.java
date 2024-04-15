package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.dividejob;

import static java.util.Objects.nonNull;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_DIVIDED;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_JOB_NEW_INSTANCE_CREATION_RULE;

import java.time.Instant;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;

public class ProcessGreenSourceJobNewInstanceCreationRule
		extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessGreenSourceJobNewInstanceCreationRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
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
		final ServerJob job = facts.get(JOB);

		RuleSetFacts newFacts;

		if (nonNull(jobTransfer)) {
			newFacts = agentProps.divideJobForPowerShortage(jobTransfer, job, facts);
			controller.fire(newFacts);
		} else {
			newFacts = agentProps.divideJobForPowerShortage(job, startTime, facts);
			if (newFacts.asMap().containsKey(RULE_TYPE)) {
				controller.fire(newFacts);
			}
		}
		agentProps.updateGUI();
		facts.put(RESULT, newFacts.get(JOB_DIVIDED));
	}
}
