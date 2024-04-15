package org.greencloud.agentsystem.agents.scheduler;

import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_JOB_PRIORITY_RULE;

import java.util.function.ToDoubleFunction;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

/**
 * Abstract agent class storing the data regarding Scheduler Agent
 */
public abstract class AbstractSchedulerAgent extends EGCSAgent<SchedulerNode, SchedulerAgentProps> {

	/**
	 * Default constructor.
	 */
	protected AbstractSchedulerAgent() {
		super();
		this.properties = new SchedulerAgentProps(getName());
	}

	/**
	 * Method defines way of calculating the job priority
	 */
	public final ToDoubleFunction<ClientJob> getJobPriority() {
		return clientJob -> {
			final RuleSetFacts facts = new RuleSetFacts(rulesController.getLatestLongTermRuleSetIdx().get());
			facts.put(RULE_TYPE, COMPUTE_JOB_PRIORITY_RULE);
			facts.put(JOB, clientJob);
			fireOnFacts(facts);
			return facts.get(RESULT);
		};
	}
}
