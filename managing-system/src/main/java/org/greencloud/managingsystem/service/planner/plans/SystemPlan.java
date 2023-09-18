package org.greencloud.managingsystem.service.planner.plans;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.goal.GoalEnum;
import org.greencloud.commons.args.adaptation.system.SystemAdaptationActionParameters;
import com.greencloud.factory.AgentFactory;
import com.greencloud.factory.AgentFactoryImpl;

/**
 * Abstract class used for the plans that are executed over entire agent system (i.e. plans that modify the structure of
 * the system) rather than the ones which target particular agents
 */
public abstract class SystemPlan extends AbstractPlan {

	protected final AgentFactory agentFactory;
	protected String adaptationPlanInformer;

	/**
	 * Default abstract constructor
	 *
	 * @param actionEnum    type of adaptation action
	 * @param managingAgent managing agent executing the action
	 * @param violatedGoal  violated goal
	 */
	protected SystemPlan(AdaptationActionEnum actionEnum, ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(actionEnum, managingAgent, violatedGoal);
		agentFactory = new AgentFactoryImpl();
	}

	public SystemAdaptationActionParameters getSystemAdaptationActionParameters() {
		return (SystemAdaptationActionParameters) this.actionParameters;
	}

	public String getAdaptationPlanInformer() {
		return adaptationPlanInformer;
	}
}
