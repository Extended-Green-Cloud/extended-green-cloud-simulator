package org.greencloud.managingsystem.service.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.greencloud.connector.factory.constants.AgentControllerConstants.RUN_AGENT_DELAY;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.util.Optional.empty;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.EXECUTE_ACTION_PROTOCOL;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.managingsystem.agents.ManagingAgent;
import org.greencloud.managingsystem.behaviours.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.behaviours.executor.WaitForSystemPlanExecutionConfirmation;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.SystemPlan;
import org.jrba.agentmodel.domain.args.AgentArgs;
import org.jrba.utils.messages.MessageBuilder;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.types.GoalType;
import com.greencloud.connector.factory.EGCSControllerFactory;

import jade.core.Location;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;

/**
 * Service containing methods used in execution of the adaptation plan
 */
public class ExecutorService extends AbstractManagingService {

	private static final LogBuilder log = constructLogBuilder(ExecutorService.class);

	private final EGCSControllerFactory factory;

	/**
	 * Default constructor.
	 *
	 * @param managingAgent managing agent
	 * @param factory       agent controller factory
	 */
	public ExecutorService(final ManagingAgent managingAgent, final EGCSControllerFactory factory) {
		super(managingAgent);
		this.factory = factory;
	}

	/**
	 * Executes adaptation action defined within the provided adaptation plan.
	 *
	 * @param adaptationPlan plan containing all necessary data to correctly execute adaptation action
	 */
	public void executeAdaptationAction(final AbstractPlan adaptationPlan) {
		final AdaptationAction actionToBeExecuted = getAdaptationAction(adaptationPlan.getAdaptationActionEnum(),
				adaptationPlan.getViolatedGoal());
		final Map<GoalType, Double> initialGoalQualities = managingAgent.monitor().getCurrentGoalQualities();

		log.messageInfo("Executing adaptation action {}.", actionToBeExecuted.getAction());

		switch (adaptationPlan) {
			case SystemPlan systemPlan -> executeSystemAdaptation(systemPlan, actionToBeExecuted, initialGoalQualities);
			default -> executeAgentAdaptation(adaptationPlan, initialGoalQualities, actionToBeExecuted);
		}
		adaptationPlan.disablePlanAction().run();
	}

	private void executeAgentAdaptation(final AbstractPlan adaptationPlan,
			final Map<GoalType, Double> initialGoalQualities, final AdaptationAction actionToBeExecuted) {
		final ACLMessage adaptationActionRequest = MessageBuilder.builder(0, REQUEST)
				.withConversationId(adaptationPlan.getAdaptationActionEnum().toString())
				.withMessageProtocol(EXECUTE_ACTION_PROTOCOL)
				.withObjectContent(adaptationPlan.getActionParameters())
				.withReceivers(adaptationPlan.getTargetAgent())
				.build();

		managingAgent.addBehaviour(new InitiateAdaptationActionRequest(managingAgent, adaptationActionRequest,
				initialGoalQualities, adaptationPlan.getPostActionHandler(), adaptationPlan.enablePlanAction(),
				actionToBeExecuted));
	}

	private void executeSystemAdaptation(final SystemPlan systemAdaptationPlan,
			final AdaptationAction actionToBeExecuted, final Map<GoalType, Double> initialGoalQualities) {
		final List<AgentController> createdAgents = createAgents(systemAdaptationPlan);
		final Location location = systemAdaptationPlan.getSystemAdaptationActionParameters().getAgentsTargetLocation();

		managingAgent.addBehaviour(WaitForSystemPlanExecutionConfirmation.create(managingAgent,
				systemAdaptationPlan.getAdaptationPlanInformer(), location.getName(), systemAdaptationPlan,
				initialGoalQualities, actionToBeExecuted));

		factory.runAgentControllers(createdAgents, RUN_AGENT_DELAY);
		managingAgent.move().moveContainers(location, createdAgents);
		managingAgent.getAgentNode().logNewAdaptation(actionToBeExecuted.getAction(), getCurrentTime(), empty());
	}

	private List<AgentController> createAgents(SystemPlan systemAdaptationPlan) {
		final List<AgentArgs> agentArguments = systemAdaptationPlan.getSystemAdaptationActionParameters()
				.getAgentsArguments();
		final String informerAgent = systemAdaptationPlan.getAdaptationPlanInformer();
		final ScenarioStructureArgs greenCloudStructure = managingAgent.getProperties().getGreenCloudStructure();
		managingAgent.move().addAgentsToStructure(agentArguments);

		return agentArguments.stream().map(agentArgs -> {
			final boolean isInformer = agentArgs.getName().equals(informerAgent);
			return isInformer ?
					factory.createAgentController(agentArgs, greenCloudStructure, true, managingAgent.getAID()) :
					factory.createAgentController(agentArgs, greenCloudStructure);
		}).toList();
	}
}
