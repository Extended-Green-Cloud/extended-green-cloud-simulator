package com.gui.agents.managing;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static org.greencloud.commons.args.agent.AgentType.MANAGING;
import static com.gui.websocket.WebSocketConnections.getManagingSystemSocket;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.action.AdaptationActionTypeEnum;
import com.database.knowledge.domain.goal.AdaptationGoal;
import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.args.agent.managing.ManagingAgentArgs;

import com.gui.agents.egcs.EGCSNode;
import com.gui.message.ImmutableIncrementCounterMessage;
import com.gui.message.ImmutableLogAdaptationActionMessage;
import com.gui.message.ImmutableRegisterManagingAgentMessage;
import com.gui.message.ImmutableUpdateAdaptationActionMessage;
import com.gui.message.ImmutableUpdateSystemIndicatorsMessage;
import com.gui.message.domain.ImmutableAdaptationAction;
import com.gui.message.domain.ImmutableAdaptationLog;
import com.gui.message.domain.ImmutableGoalQuality;

/**
 * Agent node class representing the managing agent
 */
public class ManagingAgentNode extends EGCSNode<ManagingAgentArgs, AgentProps> {

	/**
	 * Managing agent node constructor
	 *
	 * @param args arguments provided for managing agent creation
	 */
	public ManagingAgentNode(ManagingAgentArgs args) {
		super(args, MANAGING);
	}

	@Override
	public void addToGraph() {
		// managing agent should not be added to the graph
	}

	/**
	 * Method registers the managing agent by passing the established adaptation goals
	 *
	 * @param goals list of initial adaptation goals
	 */
	public void registerManagingAgent(final List<AdaptationGoal> goals) {
		getManagingSystemSocket().send(ImmutableRegisterManagingAgentMessage.builder()
				.data(goals)
				.build());
	}

	/**
	 * Method updates current values of the quality indicators
	 *
	 * @param systemQualityIndicator overall system quality
	 * @param goalQualityMap         quality values for each adaptation goal
	 */
	public void updateQualityIndicators(final double systemQualityIndicator,
			final Map<Integer, Double> goalQualityMap) {
		getManagingSystemSocket().send(ImmutableUpdateSystemIndicatorsMessage.builder()
				.systemIndicator(systemQualityIndicator)
				.data(goalQualityMap)
				.build());
	}

	/**
	 * Method updates information about given adaptation action
	 *
	 * @param adaptationAction new adaptation action statistics
	 */
	public void updateAdaptationAction(final AdaptationAction adaptationAction) {
		final List<ImmutableGoalQuality> goalQualities = adaptationAction.getActionResults().entrySet().stream()
				.map(entry -> ImmutableGoalQuality.builder()
						.avgQuality(entry.getValue().diff())
						.name(entry.getKey().name().replace("_", " "))
						.build())
				.toList();
		final ImmutableAdaptationAction action = ImmutableAdaptationAction.builder()
				.avgGoalQualities(goalQualities)
				.goal(adaptationAction.getGoal().name().replace("_", " "))
				.name(adaptationAction.getAction().getName())
				.runsNo(adaptationAction.getRuns())
				.avgDuration(adaptationAction.getExecutionDuration())
				.build();

		getManagingSystemSocket().send(ImmutableUpdateAdaptationActionMessage.builder()
				.data(action)
				.build());
	}

	/**
	 * Method sends log message to GUI informing about new adaptation
	 *
	 * @param action         performed adaptation action type
	 * @param adaptationTime time when the action was performed
	 * @param agentName      optional parameter indicating the agent on which the adaptation was performed
	 */
	public void logNewAdaptation(final AdaptationActionEnum action, final Instant adaptationTime,
			final Optional<String> agentName) {
		var adaptationAction = getAdaptationAction(action).get(0);

		getManagingSystemSocket().send(ImmutableLogAdaptationActionMessage.builder()
				.data(ImmutableAdaptationLog.builder()
						.time(adaptationTime)
						.type(adaptationAction.getType())
						.agentName(agentName.orElse(null))
						.description(adaptationAction.getAction().getName())
						.build())
				.build());
		getManagingSystemSocket().send(ImmutableIncrementCounterMessage.builder()
				.type(getCounterToIncrement(adaptationAction.getType()))
				.build());
	}

	private String getCounterToIncrement(final AdaptationActionTypeEnum actionType) {
		return switch (actionType) {
			case RECONFIGURE -> "INCREMENT_WEAK_ADAPTATIONS";
			case ADD_COMPONENT, REMOVE_COMPONENT -> "INCREMENT_STRONG_ADAPTATIONS";
		};
	}

	@Override
	public void updateGUI(final AgentProps props) {
		// no updates to GUI
	}

	@Override
	public void saveMonitoringData(final AgentProps props) {
		// managing agent does not report data
	}
}
