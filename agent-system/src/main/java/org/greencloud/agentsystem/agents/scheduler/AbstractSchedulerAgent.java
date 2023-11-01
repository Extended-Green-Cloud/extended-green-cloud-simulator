package org.greencloud.agentsystem.agents.scheduler;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.COMPUTE_JOB_PRIORITY_RULE;

import java.util.function.ToDoubleFunction;

import org.greencloud.agentsystem.agents.AbstractAgent;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;

/**
 * Abstract agent class storing the data regarding Scheduler Agent
 */
public abstract class AbstractSchedulerAgent extends AbstractAgent<SchedulerNode, SchedulerAgentProps> {

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
			final RuleSetFacts facts = new RuleSetFacts(rulesController.getLatestRuleSet().get());
			facts.put(RULE_TYPE, COMPUTE_JOB_PRIORITY_RULE);
			facts.put(JOB, clientJob);
			fireOnFacts(facts);
			return facts.get(RESULT);
		};
	}
}
