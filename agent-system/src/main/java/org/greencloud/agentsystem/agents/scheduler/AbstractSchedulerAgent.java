package org.greencloud.agentsystem.agents.scheduler;

import static com.greencloud.commons.domain.strategy.FactType.JOB;
import static com.greencloud.commons.domain.strategy.FactType.RESULT;
import static com.greencloud.commons.domain.strategy.FactType.RULE_TYPE;
import static com.greencloud.commons.domain.strategy.RuleType.COMPUTE_JOB_PRIORITY_RULE;

import java.util.function.ToDoubleFunction;

import org.greencloud.agentsystem.agents.AbstractAgent;

import com.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.strategy.facts.StrategyFacts;
import com.gui.agents.SchedulerNode;

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
			final StrategyFacts facts = new StrategyFacts(rulesController.getLatestStrategy().get());
			facts.put(RULE_TYPE, COMPUTE_JOB_PRIORITY_RULE);
			facts.put(JOB, clientJob);
			fireOnFacts(facts);
			return facts.get(RESULT);
		};
	}
}
