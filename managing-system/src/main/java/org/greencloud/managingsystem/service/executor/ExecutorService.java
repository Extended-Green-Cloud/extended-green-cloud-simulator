package org.greencloud.managingsystem.service.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.yellowpages.YellowPagesService.search;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.CNA_SERVICE_TYPE;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.ANNOUNCE_NETWORK_CHANGE_PROTOCOL;
import static com.greencloud.commons.managingsystem.executor.ExecutorMessageTemplates.EXECUTE_ACTION_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.greencloud.managingsystem.agent.behaviour.executor.VerifyAdaptationActionResult.createForSystemAction;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.greencloud.managingsystem.service.executor.jade.AgentControllerFactory;
import org.greencloud.managingsystem.service.executor.jade.AgentRunner;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.SystemPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.goal.GoalEnum;
import com.google.common.annotations.VisibleForTesting;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.message.MessageBuilder;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;
import jade.core.Location;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;

/**
 * Service containing methods used in execution of the adaptation plan
 */
public class ExecutorService extends AbstractManagingService {

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
	public void executeAdaptationAction(final AbstractPlan adaptationPlan) {
		final AdaptationAction actionToBeExecuted = getAdaptationAction(adaptationPlan.getAdaptationActionEnum());
		final Double initialGoalQuality = getInitialGoalQuality(actionToBeExecuted.getGoal());

		if (adaptationPlan instanceof SystemPlan systemAdaptationPlan) {
			logger.info("Executing system plan!");
			this.executeAdaptationActionOnSystem(systemAdaptationPlan, actionToBeExecuted, initialGoalQuality);
		} else {
			this.executeAdaptationActionOnAgent(adaptationPlan, initialGoalQuality);
		}
	}

	private void executeAdaptationActionOnAgent(final AbstractPlan adaptationPlan, final Double initialGoalQuality) {
		final ACLMessage adaptationActionRequest = MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withConversationId(adaptationPlan.getAdaptationActionEnum().toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(adaptationPlan.getActionParameters())
				.withReceivers(adaptationPlan.getTargetAgent())
				.build();
		adaptationPlan.disablePlanAction().run();
		managingAgent.addBehaviour(new InitiateAdaptationActionRequest(managingAgent, adaptationActionRequest,
				initialGoalQuality, adaptationPlan.getPostActionHandler(), adaptationPlan.enablePlanAction()));
	}

	private double getInitialGoalQuality(final GoalEnum targetGoal) {
		var goalQuality = managingAgent.monitor().getGoalService(targetGoal).computeCurrentGoalQuality();

		if (goalQuality == DATA_NOT_AVAILABLE_INDICATOR) {
			throw new IllegalStateException("Goal quality must be present to initiate action, this should not happen.");
		}

		return goalQuality;
	}

	private void executeAdaptationActionOnSystem(SystemPlan systemAdaptationPlan, AdaptationAction actionToBeExecuted,
			Double initialGoalQuality) {
		final List<AgentController> createdAgents = createAgents(systemAdaptationPlan);
		final Location location = systemAdaptationPlan.getSystemAdaptationActionParameters().getAgentsTargetLocation();
		agentRunner.runAgents(createdAgents);
		managingAgent.move().moveContainers(location, createdAgents);
		announceNetworkChange();
		systemAdaptationPlan.disablePlanAction().run();
		((ManagingAgentNode) managingAgent.getAgentNode()).logNewAdaptation(actionToBeExecuted.getAction(),
				getCurrentTime(), Optional.empty());
		managingAgent.addBehaviour(createForSystemAction(managingAgent, getCurrentTime(),
				systemAdaptationPlan.getAdaptationActionEnum(), initialGoalQuality,
				systemAdaptationPlan.enablePlanAction()));
	}

	private List<AgentController> createAgents(SystemPlan systemAdaptationPlan) {
		List<AgentArgs> args = systemAdaptationPlan.getSystemAdaptationActionParameters().getAgentsArguments();
		managingAgent.move().addAgentsToStructure(args);
		return args.stream()
				.map(agentRunner::runAgentController)
				.toList();
	}

	private void announceNetworkChange() {
		Set<AID> cloudNetworkAgents = search(managingAgent, CNA_SERVICE_TYPE);
		ACLMessage announcementMessage = MessageBuilder.builder()
				.withPerformative(INFORM)
				.withMessageProtocol(ANNOUNCE_NETWORK_CHANGE_PROTOCOL)
				.withStringContent(ANNOUNCE_NETWORK_CHANGE_PROTOCOL)
				.withReceivers(cloudNetworkAgents)
				.build();
		managingAgent.send(announcementMessage);
	}


}
