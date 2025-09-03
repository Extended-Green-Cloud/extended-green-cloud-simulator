package org.greencloud.commons.args.agent.managing.agent;

import static org.greencloud.commons.args.agent.EGCSAgentType.MANAGING;

import java.util.List;

import org.greencloud.commons.args.agent.EGCSAgentProps;
import org.greencloud.commons.args.agent.managing.factory.ManagingAgentArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.domain.adaptation.goal.AdaptationGoal;
import org.jrba.agentmodel.domain.props.AgentProps;

import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Managing Agent
 */
@Getter
public class ManagingAgentProps extends EGCSAgentProps<ManagingAgentArgs> {

	protected ScenarioStructureArgs greenCloudStructure;
	@Setter
	protected List<AdaptationGoal> adaptationGoalList;

	/**
	 * Constructor
	 *
	 * @param agentName           name of the agent
	 * @param managingAgentArgs   managing agent arguments
	 * @param greenCloudStructure structure of executed scenario
	 */
	public ManagingAgentProps(final String agentName,
			final ManagingAgentArgs managingAgentArgs,
			final ScenarioStructureArgs greenCloudStructure) {
		super(MANAGING, agentName, managingAgentArgs);
		this.greenCloudStructure = greenCloudStructure;
	}
}
