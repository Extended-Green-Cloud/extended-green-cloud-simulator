package com.gui.agents;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionTypeEnum;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import com.gui.message.ImmutableIncrementCounterMessage;
import com.gui.message.ImmutableLogAdaptationActionMessage;
import com.gui.message.ImmutableRegisterManagingAgentMessage;
import com.gui.message.ImmutableUpdateSystemIndicatorsMessage;
import com.gui.message.domain.ImmutableAdaptationLog;

/**
 * Agent node class representing the managing agent
 */
public class ManagingAgentNode extends AbstractAgentNode {

	final double qualityThreshold;

	/**
	 * Managing agent node constructor
	 *
	 * @param args arguments provided for managing agent creation
	 */
	public ManagingAgentNode(ManagingAgentArgs args) {
		super(args.getName());

		this.qualityThreshold = args.getSystemQualityThreshold();
	}

	/**
	 * Method registers the managing agent by passing the established adaptation goals
	 *
	 * @param goals list of initial adaptation goals
	 */
	public void registerManagingAgent(final List<AdaptationGoal> goals) {
		webSocketClient.send(ImmutableRegisterManagingAgentMessage.builder()
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
		webSocketClient.send(ImmutableUpdateSystemIndicatorsMessage.builder()
				.systemIndicator(systemQualityIndicator)
				.data(goalQualityMap)
				.build());
	}

	/**
	 * Method sends log message to GUI informing about new adaptation
	 *
	 * @param action         performed adaptation action
	 * @param adaptationTime time when the action was performed
	 * @param agentName      optional parameter indicating the agent on which the adaptation was performed
	 */
	public void logNewAdaptation(final AdaptationAction action, final Instant adaptationTime,
			final Optional<String> agentName) {
		webSocketClient.send(ImmutableLogAdaptationActionMessage.builder()
				.data(ImmutableAdaptationLog.builder()
						.time(adaptationTime)
						.type(action.getType())
						.agentName(agentName.orElse(null))
						.description(action.getAction().getName())
						.build())
				.build());
		webSocketClient.send(ImmutableIncrementCounterMessage.builder()
				.type(getCounterToIncrement(action.getType()))
				.build());
	}

	private String getCounterToIncrement(final AdaptationActionTypeEnum actionType) {
		return switch (actionType) {
			case RECONFIGURE -> "INCREMENT_WEAK_ADAPTATIONS";
			case ADD_COMPONENT, REMOVE_COMPONENT -> "INCREMENT_STRONG_ADAPTATIONS";
			default -> throw new InvalidParameterException(String.format("Invalid action type %s", actionType));
		};
	}
}
