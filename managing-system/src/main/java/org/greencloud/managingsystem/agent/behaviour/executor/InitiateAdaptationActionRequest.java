package org.greencloud.managingsystem.agent.behaviour.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.ACTION_FAILED_LOG;
import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.COMPLETED_ACTION_LOG;

import java.util.Objects;
import java.util.Optional;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Initiates the adaptation action request for the given agent and handles its execution result, either
 * successful or failed execution.
 */
public class InitiateAdaptationActionRequest extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateAdaptationActionRequest.class);

	private final ManagingAgent myManagingAgent;
	private final ManagingAgentNode managingAgentNode;
	private final AdaptationActionEnum adaptationActionType;
	private final AID targetAgent;
	private final Double initialGoalQuality;
	private final Runnable postActionHandler;

	public InitiateAdaptationActionRequest(Agent agent, ACLMessage message, Double initialGoalQuality,
			Runnable postActionHandler) {
		super(agent, message);
		this.adaptationActionType = AdaptationActionEnum.valueOf(message.getConversationId());
		this.targetAgent = (AID) message.getAllReceiver().next();
		this.initialGoalQuality = initialGoalQuality;
		this.myManagingAgent = (ManagingAgent) agent;
		this.managingAgentNode = (ManagingAgentNode) myManagingAgent.getAgentNode();
		this.postActionHandler = postActionHandler;
	}

	/**
	 * If the action is correctly executes Agent responds with an INFORM message. In that case
	 * a verification of the executed action must be scheduled. The {@link VerifyAdaptationActionResult}
	 * is scheduled after the period defined in VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS constant.
	 *
	 * @param inform message received from the target agent
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		logger.info(COMPLETED_ACTION_LOG, adaptationActionType, targetAgent);
		scheduleVerifyBehaviour();
		executePostAdaptationAction();
		managingAgentNode.logNewAdaptation(getAdaptationAction(adaptationActionType), getCurrentTime(),
				Optional.of(targetAgent.getLocalName()));
		myManagingAgent.removeBehaviour(this);
	}

	private void scheduleVerifyBehaviour() {
		var verifyingBehaviour = new VerifyAdaptationActionResult(myManagingAgent, getCurrentTime(),
				adaptationActionType, targetAgent, initialGoalQuality);
		myManagingAgent.addBehaviour(verifyingBehaviour);
	}

	private void executePostAdaptationAction() {
		if(Objects.nonNull(postActionHandler)) {
			postActionHandler.run();
		}
	}

	/**
	 * If the action fails it should be released immediately
	 *
	 * @param failure failure message received from the target agent
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		logger.info(ACTION_FAILED_LOG, adaptationActionType, targetAgent);
		myManagingAgent.getAgentNode().getDatabaseClient()
				.setAdaptationActionAvailability(getAdaptationAction(adaptationActionType).getActionId(), true);
		myManagingAgent.removeBehaviour(this);
	}
}
