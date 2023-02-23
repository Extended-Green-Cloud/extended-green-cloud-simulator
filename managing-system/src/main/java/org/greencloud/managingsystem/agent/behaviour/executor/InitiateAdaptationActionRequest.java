package org.greencloud.managingsystem.agent.behaviour.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static org.greencloud.managingsystem.agent.behaviour.executor.VerifyAdaptationActionResult.createForAgentAction;
import static org.greencloud.managingsystem.agent.behaviour.executor.logs.ManagingExecutorLog.ACTION_FAILED_LOG;
import static org.greencloud.managingsystem.agent.behaviour.executor.logs.ManagingExecutorLog.COMPLETED_ACTION_LOG;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.goal.GoalEnum;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Initiates the adaptation action request for the given agent and handles its execution result - either
 * successful or failed execution.
 */
public class InitiateAdaptationActionRequest extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateAdaptationActionRequest.class);

	private final ManagingAgent myManagingAgent;
	private final ManagingAgentNode managingAgentNode;
	private final AdaptationActionEnum adaptationActionType;
	private final AID targetAgent;
	private final Map<GoalEnum, Double> initialGoalQualities;
	private final Runnable postActionHandler;
	private final Runnable enablePlanAction;

	/**
	 * Behaviour constructor
	 *
	 * @param agent                - managing agent that executes the behaviour
	 * @param message              - request of adaptation that is to be sent to the targeted agent
	 * @param initialGoalQualities - values of the goal qualities before performing the adaptation
	 * @param postActionHandler    - action performed after adaptation is finished
	 * @param enablePlanAction     - method performed in order to enable availability of an action corresponding to
	 *                             given plan
	 */
	public InitiateAdaptationActionRequest(Agent agent, ACLMessage message, Map<GoalEnum, Double> initialGoalQualities,
			Runnable postActionHandler, Runnable enablePlanAction) {
		super(agent, message);
		this.adaptationActionType = AdaptationActionEnum.valueOf(message.getConversationId());
		this.targetAgent = (AID) message.getAllReceiver().next();
		this.myManagingAgent = (ManagingAgent) agent;
		this.managingAgentNode = (ManagingAgentNode) myManagingAgent.getAgentNode();
		this.initialGoalQualities = initialGoalQualities;
		this.postActionHandler = postActionHandler;
		this.enablePlanAction = enablePlanAction;
	}

	/**
	 * Method handles INFORM message coming from given adapted agent which tells that the
	 * adaptation was successfully executed.
	 * Method schedules the {@link VerifyAdaptationActionResult} behaviour that will be responsible for
	 * verifying the adaptation outcome.
	 *
	 * @param inform message received from the target agent
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		var actionExecutionTime = getCurrentTime();
		logger.info(COMPLETED_ACTION_LOG, adaptationActionType, actionExecutionTime, targetAgent);

		if (Objects.nonNull(postActionHandler)) {
			postActionHandler.run();
		}
		var verifyingBehaviour = createForAgentAction(myManagingAgent, actionExecutionTime,
				adaptationActionType, targetAgent, initialGoalQualities, enablePlanAction);

		myManagingAgent.addBehaviour(verifyingBehaviour);
		managingAgentNode.logNewAdaptation(adaptationActionType, actionExecutionTime,
				Optional.of(targetAgent.getLocalName()));
	}

	/**
	 * Method handles FAILURE message coming from given agent selected for adaptation which tells that the
	 * adaptation execution has failed.
	 *
	 * @param failure failure message received from the target agent
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		logger.info(ACTION_FAILED_LOG, adaptationActionType, targetAgent);
		myManagingAgent.getAgentNode().getDatabaseClient()
				.setAdaptationActionAvailability(getAdaptationAction(adaptationActionType).getActionId(), true);
	}
}
