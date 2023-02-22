package org.greencloud.managingsystem.service.planner.plans;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.greencloud.commons.agentfactory.AgentFactory;
import com.greencloud.commons.agentfactory.AgentFactoryImpl;
import com.greencloud.commons.managingsystem.planner.SystemAdaptationActionParameters;

/**
 * Abstract class used for the plans that are executed over entire agent system (i.e. plans that modify the structure of
 * the system) rather than the ones which target particular agents
 */
public abstract class SystemPlan extends AbstractPlan {

	protected final AgentFactory agentFactory;

	/**
	 * Default abstract constructor
	 *
	 * @param actionEnum    type of adaptation action
	 * @param managingAgent managing agent executing the action
	 */
	protected SystemPlan(AdaptationActionEnum actionEnum, ManagingAgent managingAgent) {
		super(actionEnum, managingAgent);
		agentFactory = new AgentFactoryImpl();
	}

	public SystemAdaptationActionParameters getSystemAdaptationActionParameters() {
		return (SystemAdaptationActionParameters) this.actionParameters;
	}
}
