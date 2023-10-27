package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.dividejob;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_DIVIDED;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_JOB_DIVISION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.rulescontroller.ruleset.RuleSetSelector.SELECT_BY_FACTS_IDX;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.job.transfer.JobDivided;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessGreenSourceJobDivisionRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessGreenSourceJobDivisionRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_JOB_DIVISION_RULE,
				"divides job instances into 2",
				"rule produces 2 job instances and initiates their execution");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobDivided<ServerJob> jobInstances = facts.get(JOB_DIVIDED);
		final ServerJob affectedJob = jobInstances.getSecondInstance();
		final ServerJob nonAffectedJob = jobInstances.getFirstInstance();

		agentProps.incrementJobCounter(mapToJobInstanceId(affectedJob), ACCEPTED);

		final RuleSetFacts jobManualFinish = new RuleSetFacts(facts.get(RULE_SET_IDX));
		jobManualFinish.put(JOB, nonAffectedJob);
		agent.addBehaviour(
				ScheduleOnce.create(agent, jobManualFinish, JOB_MANUAL_FINISH_RULE, controller, SELECT_BY_FACTS_IDX));
	}
}
