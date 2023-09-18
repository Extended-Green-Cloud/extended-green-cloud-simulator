package org.greencloud.managingsystem.service.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.EXECUTE_ACTION_PROTOCOL;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static com.greencloud.factory.constants.AgentControllerConstants.RUN_AGENT_DELAY;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.util.Optional.empty;
import static org.greencloud.managingsystem.service.executor.logs.ManagingAgentExecutorLog.EXECUTING_ADAPTATION_ACTION_LOG;

import java.util.List;
import java.util.Map;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.agent.behaviour.executor.WaitForSystemPlanExecutionConfirmation;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.SystemPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.goal.GoalEnum;
import com.google.common.annotations.VisibleForTesting;
import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.utils.messaging.MessageBuilder;
import com.greencloud.factory.AgentControllerFactory;
import com.gui.agents.managing.ManagingAgentNode;

import jade.core.Location;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;

/**
 * Service containing methods used in execution of the adaptation plan
 */
public class ExecutorService extends AbstractManagingService {

	private static final Logger logger = LoggerFactory.getLogger(ExecutorService.class);

	private AgentControllerFactory factory;

	public ExecutorService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	@VisibleForTesting
	protected ExecutorService(ManagingAgent managingAgent, AgentControllerFactory factory) {
		super(managingAgent);
		this.factory = factory;
	}

	/**
	 * Executes adaptation action defined within the provided adaptation plan
	 *
	 * @param adaptationPlan plan containing all necessary data to correctly execute adaptation action
	 */
	public void executeAdaptationAction(final AbstractPlan adaptationPlan) {
		final AdaptationAction actionToBeExecuted = getAdaptationAction(adaptationPlan.getAdaptationActionEnum(),
				adaptationPlan.getViolatedGoal());
		final Map<GoalEnum, Double> initialGoalQualities = managingAgent.monitor().getCurrentGoalQualities();

		logger.info(EXECUTING_ADAPTATION_ACTION_LOG, actionToBeExecuted.getAction());

		if (adaptationPlan instanceof SystemPlan systemAdaptationPlan) {
			executeAdaptationActionOnSystem(systemAdaptationPlan, actionToBeExecuted, initialGoalQualities);
		} else {
			executeAdaptationActionOnAgent(adaptationPlan, initialGoalQualities, actionToBeExecuted);
		}

		adaptationPlan.disablePlanAction().run();
	}

	private void executeAdaptationActionOnAgent(final AbstractPlan adaptationPlan,
			final Map<GoalEnum, Double> initialGoalQualities, final AdaptationAction actionToBeExecuted) {
		final ACLMessage adaptationActionRequest = MessageBuilder.builder(0)
				.withPerformative(REQUEST)
				.withConversationId(adaptationPlan.getAdaptationActionEnum().toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(adaptationPlan.getActionParameters())
				.withReceivers(adaptationPlan.getTargetAgent())
				.build();

		managingAgent.addBehaviour(new InitiateAdaptationActionRequest(managingAgent, adaptationActionRequest,
				initialGoalQualities, adaptationPlan.getPostActionHandler(), adaptationPlan.enablePlanAction(),
				actionToBeExecuted));
	}

	private void executeAdaptationActionOnSystem(final SystemPlan systemAdaptationPlan,
			final AdaptationAction actionToBeExecuted, final Map<GoalEnum, Double> initialGoalQualities) {
		final List<AgentController> createdAgents = createAgents(systemAdaptationPlan);
		final Location location = systemAdaptationPlan.getSystemAdaptationActionParameters().getAgentsTargetLocation();

		managingAgent.addBehaviour(WaitForSystemPlanExecutionConfirmation.create(managingAgent,
				systemAdaptationPlan.getAdaptationPlanInformer(), location.getName(), systemAdaptationPlan,
				initialGoalQualities, actionToBeExecuted));

		factory.runAgentControllers(createdAgents, RUN_AGENT_DELAY);
		managingAgent.move().moveContainers(location, createdAgents);
		((ManagingAgentNode) managingAgent.getAgentNode()).logNewAdaptation(actionToBeExecuted.getAction(),
				getCurrentTime(), empty());
	}

	private List<AgentController> createAgents(SystemPlan systemAdaptationPlan) {
		final List<AgentArgs> args = systemAdaptationPlan.getSystemAdaptationActionParameters().getAgentsArguments();
		final String informerAgent = systemAdaptationPlan.getAdaptationPlanInformer();
		managingAgent.move().addAgentsToStructure(args);
		return args.stream()
				.map(agentArgs -> {
					final boolean isInformer = agentArgs.getName().equals(informerAgent);
					return isInformer ?
							factory.createAgentController(agentArgs, managingAgent.getGreenCloudStructure(), true,
									managingAgent.getAID()) :
							factory.createAgentController(agentArgs, managingAgent.getGreenCloudStructure());
				})
				.toList();
	}

	public void setFactory(AgentControllerFactory factory) {
		this.factory = factory;
	}
}
