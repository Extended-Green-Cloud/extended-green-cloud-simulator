package org.greencloud.managingsystem.service.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.yellowpages.YellowPagesService.search;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.CNA_SERVICE_TYPE;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.ANNOUNCE_NETWORK_CHANGE_PROTOCOL;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.agent.behaviour.executor.VerifyAdaptationActionResult;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.greencloud.managingsystem.service.executor.jade.AgentControllerFactory;
import org.greencloud.managingsystem.service.executor.jade.AgentRunner;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.SystemPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.goal.GoalEnum;
import com.google.common.annotations.VisibleForTesting;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.message.MessageBuilder;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;
import jade.core.Location;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Service containing methods used in execution of the adaptation plan
 */
public class ExecutorService extends AbstractManagingService {

	private static final Integer SYSTEM_ADAPTATION_PLAN_VERIFY_DELAY = 20;

	private static final Logger logger = LoggerFactory.getLogger(ExecutorService.class);

	private final AgentRunner agentRunner;

	public ExecutorService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
		var agentControllerFactory = new AgentControllerFactory(this.managingAgent.getGreenCloudController());
		agentRunner = new AgentRunner(this.managingAgent, agentControllerFactory);
	}

	@VisibleForTesting
	protected ExecutorService(ManagingAgent managingAgent, AgentRunner agentRunner) {
		super(managingAgent);
		this.agentRunner = agentRunner;
	}

	/**
	 * Executes adaptation action defined within the provided adaptation plan
	 *
	 * @param adaptationPlan plan containing all necessary data to correctly execute adaptation action
	 */
	public void executeAdaptationAction(AbstractPlan adaptationPlan) {
		AdaptationAction actionToBeExecuted = getAdaptationAction(adaptationPlan.getAdaptationActionEnum());
		Double initialGoalQuality = getInitialGoalQuality(actionToBeExecuted.getGoal());
		if (adaptationPlan instanceof SystemPlan systemAdaptationPlan) {
			logger.info("Executing system plan!");
			this.executeAdaptationActionOnSystem(systemAdaptationPlan, actionToBeExecuted, initialGoalQuality);
		} else {
			this.executeAdaptationActionOnAgent(adaptationPlan, actionToBeExecuted, initialGoalQuality);
		}
	}

	private void executeAdaptationActionOnAgent(AbstractPlan adaptationPlan, AdaptationAction actionToBeExecuted,
			Double initialGoalQuality) {
		ACLMessage adaptationActionRequest = MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withConversationId(adaptationPlan.getAdaptationActionEnum().toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(adaptationPlan.getActionParameters())
				.withReceivers(adaptationPlan.getTargetAgent())
				.build();
		disableAdaptationAction(actionToBeExecuted);
		managingAgent.addBehaviour(new InitiateAdaptationActionRequest(managingAgent, adaptationActionRequest,
				initialGoalQuality, adaptationPlan.getPostActionHandler()));
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
					.setAdaptationActionAvailability(3, false);
		}
		if (adaptationAction.getAction() == AdaptationActionEnum.INCREASE_POWER_PRIORITY) {
			managingAgent.getAgentNode().getDatabaseClient()
					.setAdaptationActionAvailability(2, false);
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

	private void executeAdaptationActionOnSystem(SystemPlan systemAdaptationPlan, AdaptationAction actionToBeExecuted,
			Double initialGoalQuality) {
		List<AgentController> createdAgents = createAgents(systemAdaptationPlan);
		agentRunner.runAgents(createdAgents);
		moveContainers(systemAdaptationPlan, createdAgents);
		announceNetworkChange();
		disableAdaptationAction(actionToBeExecuted);
		((ManagingAgentNode) managingAgent.getAgentNode()).logNewAdaptation(
				getAdaptationAction(actionToBeExecuted.getAction()), getCurrentTime(), Optional.empty());
		managingAgent.addBehaviour(new VerifyAdaptationActionResult(managingAgent, getCurrentTime(),
				systemAdaptationPlan.getAdaptationActionEnum(), null, initialGoalQuality,
				SYSTEM_ADAPTATION_PLAN_VERIFY_DELAY));
	}

	private List<AgentController> createAgents(SystemPlan systemAdaptationPlan) {
		List<AgentArgs> args = systemAdaptationPlan.getSystemAdaptationActionParameters().getAgentsArguments();
		managingAgent.addAgentsToStructure(args);
		return args.stream()
				.map(agentRunner::runAgentController)
				.toList();
	}

	private void announceNetworkChange() {
		Set<AID> cloudNetworkAgents = search(managingAgent, CNA_SERVICE_TYPE);
		ACLMessage announcementMessage = MessageBuilder.builder()
				.withPerformative(INFORM)
				.withMessageProtocol(ANNOUNCE_NETWORK_CHANGE_PROTOCOL)
				.withReceivers(cloudNetworkAgents)
				.build();
		managingAgent.send(announcementMessage);
	}

	private void moveContainers(SystemPlan systemAdaptationPlan, List<AgentController> createdAgents) {
		Location targetContainer = systemAdaptationPlan.getSystemAdaptationActionParameters().getAgentsTargetLocation();
		if (!targetContainer.getName().equals("Main-Container")) {
			createdAgents.forEach(agentController -> moveAgentController(agentController, targetContainer));
		}
	}

	private void moveAgentController(AgentController agentController, Location targetContainer) {
		try {
			agentController.move(targetContainer);
		} catch (StaleProxyException e) {
			throw new RuntimeException(e);
		}
	}
}
