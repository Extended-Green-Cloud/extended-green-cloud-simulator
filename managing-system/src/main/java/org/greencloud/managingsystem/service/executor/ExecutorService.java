package org.greencloud.managingsystem.service.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_PROTOCOL;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.SystemPlan;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.goal.GoalEnum;
import com.greencloud.commons.message.MessageBuilder;

import jade.lang.acl.ACLMessage;

/**
 * Service containing methods used in execution of the adaptation plan
 */
public class ExecutorService extends AbstractManagingService {

	public ExecutorService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	/**
	 * Executes adaptation action defined within the provided adaptation plan
	 *
	 * @param adaptationPlan plan containing all necessary data to correctly execute adaptation action
	 */
	public void executeAdaptationAction(AbstractPlan adaptationPlan) {
		if (adaptationPlan instanceof SystemPlan) {
			this.executeAdaptationActionOnSystem(adaptationPlan);
		} else {
			this.executeAdaptationActionOnAgent(adaptationPlan);
		}
	}

	private void executeAdaptationActionOnSystem(AbstractPlan abstractPlan) {
		// TODO implement handling system adaptations
		// 1. create Agent
		// 2. inform Network about Agent creation
		// 3. in case of multi container service send message to created agent with additional data to move itself
		//    to correct container
	}

	private void executeAdaptationActionOnAgent(AbstractPlan adaptationPlan) {
		ACLMessage adaptationActionRequest = MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withConversationId(adaptationPlan.getAdaptationActionEnum().toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(adaptationPlan.getActionParameters())
				.withReceivers(adaptationPlan.getTargetAgent())
				.build();
		AdaptationAction actionToBeExecuted = getAdaptationAction(adaptationPlan.getAdaptationActionEnum());
		Double initialGoalQuality = getInitialGoalQuality(actionToBeExecuted.getGoal());
		disableAdaptationAction(actionToBeExecuted);
		managingAgent.addBehaviour(new InitiateAdaptationActionRequest(managingAgent, adaptationActionRequest,
				initialGoalQuality));
	}

	/**
	 * Disables executed adaptation action performed within the plan until the results od the adaptation
	 * action are verified.
	 *
	 * @param adaptationAction adaptation action to be disabled
	 */
	private void disableAdaptationAction(AdaptationAction adaptationAction) {
		if (adaptationAction.getAction() == AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY) {
			managingAgent.getAgentNode().getDatabaseClient()
					.setAdaptationActionAvailability(
							getAdaptationAction(AdaptationActionEnum.INCREASE_POWER_PRIORITY).getActionId(), false);
		} else if (adaptationAction.getAction() == AdaptationActionEnum.INCREASE_POWER_PRIORITY) {
			managingAgent.getAgentNode().getDatabaseClient()
					.setAdaptationActionAvailability(
							getAdaptationAction(AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY).getActionId(), false);
		}
		managingAgent.getAgentNode().getDatabaseClient()
				.setAdaptationActionAvailability(adaptationAction.getActionId(), false);
	}

	private double getInitialGoalQuality(GoalEnum targetGoal) {
		var goalQuality = managingAgent.monitor().getGoalService(targetGoal).readCurrentGoalQuality();

		if (goalQuality == DATA_NOT_AVAILABLE_INDICATOR) {
			throw new IllegalStateException("Goal quality must be present to initiate action, this should not happen.");
		}

		return goalQuality;
	}
}
