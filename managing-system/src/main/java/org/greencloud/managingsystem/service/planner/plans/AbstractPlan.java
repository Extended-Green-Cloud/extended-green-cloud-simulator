package org.greencloud.managingsystem.service.planner.plans;

import static com.greencloud.commons.agent.AgentType.SCHEDULER;

import java.util.function.Predicate;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;

import jade.core.AID;

/**
 * Abstract class which should be extended by each adaptation plan class
 */
public abstract class AbstractPlan {

	protected final ManagingAgent managingAgent;
	protected final AdaptationActionEnum adaptationActionEnum;
	protected final Predicate<AgentData> getAliveSchedulerPredicate = agentData -> {
		var healthData = ((HealthCheck) agentData.monitoringData());
		return healthData.alive() && healthData.agentType().equals(SCHEDULER);
	};
	protected AdaptationActionParameters actionParameters;
	protected AID targetAgent;

	protected Runnable postActionHandler;

	/**
	 * Default abstract constructor
	 *
	 * @param actionEnum    type of adaptation action
	 * @param managingAgent managing agent executing the action
	 */
	protected AbstractPlan(AdaptationActionEnum actionEnum, ManagingAgent managingAgent) {
		this.adaptationActionEnum = actionEnum;
		this.managingAgent = managingAgent;
	}

	/**
	 * Abstract method verifies if the plan can be executed taking into account specific
	 * constraints and the current state of the system
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	public abstract boolean isPlanExecutable();

	/**
	 * Abstract method used for creation of the adaptation plan
	 *
	 * @return prepared adaptation plan
	 */
	public abstract AbstractPlan constructAdaptationPlan();

	public AID getTargetAgent() {
		return targetAgent;
	}

	public AdaptationActionEnum getAdaptationActionEnum() {
		return adaptationActionEnum;
	}

	public AdaptationActionParameters getActionParameters() {
		return actionParameters;
	}

	public Runnable getPostActionHandler() {
		return postActionHandler;
	}
}
