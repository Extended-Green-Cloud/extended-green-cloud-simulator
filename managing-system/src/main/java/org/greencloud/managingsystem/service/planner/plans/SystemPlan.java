package org.greencloud.managingsystem.service.planner.plans;

import org.greencloud.managingsystem.agent.ManagingAgent;

import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import com.database.knowledge.types.GoalType;
import org.greencloud.commons.args.adaptation.system.SystemAdaptationActionParameters;
import com.greencloud.connector.factory.AgentFactory;
import com.greencloud.connector.factory.AgentFactoryImpl;

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
	protected SystemPlan(AdaptationActionTypeEnum actionEnum, ManagingAgent managingAgent, GoalType violatedGoal) {
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
