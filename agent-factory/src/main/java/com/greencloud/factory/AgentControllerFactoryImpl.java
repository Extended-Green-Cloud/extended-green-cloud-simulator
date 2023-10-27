package com.greencloud.factory;

import static com.greencloud.factory.constants.AgentControllerConstants.GRAPH_INITIALIZATION_DELAY;
import static jade.wrapper.AgentController.ASYNC;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.agent.cloudnetwork.factory.CloudNetworkArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.scheduler.factory.SchedulerArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.exception.JadeControllerException;
import org.greencloud.rulescontroller.RulesController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.egcs.EGCSNode;
import com.gui.controller.GuiController;

import jade.core.AID;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class AgentControllerFactoryImpl implements AgentControllerFactory {

	private static final Logger logger = LoggerFactory.getLogger(AgentControllerFactoryImpl.class);
	private final ContainerController containerController;
	private AgentNodeFactory agentNodeFactory;
	private TimescaleDatabase timescaleDatabase;
	private GuiController guiController;
	private String mainDFAddress;
	private String mainHostPlatformId;
	private Map<String, Map<String, Object>> systemKnowledge;

	public AgentControllerFactoryImpl(final ContainerController containerController) {
		this.containerController = containerController;
	}

	public AgentControllerFactoryImpl(final ContainerController containerController,
			final TimescaleDatabase timescaleDatabase,
			final GuiController guiController) {
		this.agentNodeFactory = new AgentNodeFactoryImpl();
		this.containerController = containerController;
		this.timescaleDatabase = timescaleDatabase;
		this.guiController = guiController;
		this.mainDFAddress = null;
		this.mainHostPlatformId = null;
		this.systemKnowledge = null;
	}

	public AgentControllerFactoryImpl(final ContainerController containerController,
			final TimescaleDatabase timescaleDatabase,
			final GuiController guiController,
			final String mainDFAddress,
			final String mainHostPlatformId,
			final Map<String, Map<String, Object>> systemKnowledge) {
		this.agentNodeFactory = new AgentNodeFactoryImpl();
		this.containerController = containerController;
		this.timescaleDatabase = timescaleDatabase;
		this.guiController = guiController;
		this.mainDFAddress = mainDFAddress;
		this.mainHostPlatformId = mainHostPlatformId;
		this.systemKnowledge = systemKnowledge;
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs, final ScenarioStructureArgs scenario) {
		return createController(agentArgs, scenario, false, null);
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs, final ScenarioStructureArgs scenario,
			boolean isInformer, AID managingAgent) {
		return createController(agentArgs, scenario, isInformer, managingAgent);
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs, final String className) {
		final RulesController<?, ?> rulesController = new RulesController<>();

		try {
			final List<Object> argumentsToPass = new ArrayList<>(singletonList((Object) rulesController));
			final Object[] agentArguments = agentArgs.getObjectArray();
			argumentsToPass.addAll(Arrays.asList(agentArguments));

			return containerController.createNewAgent(agentArgs.getName(), className, argumentsToPass.toArray());
		} catch (StaleProxyException e) {
			Thread.currentThread().interrupt();
			throw new JadeControllerException("Failed to run custom agent controller", e);
		}
	}

	private AgentController createController(final AgentArgs agentArgs, final ScenarioStructureArgs scenario,
			Boolean isInformer, AID managingAgent) {
		final EGCSNode<?, ?> agentNode = agentNodeFactory.createAgentNode(agentArgs, scenario);
		var agentController = (AgentController) null;

		try {
			logger.info("Created {} agent.", agentArgs.getName());
			if (agentArgs instanceof ClientArgs clientAgent) {
				agentController = createClientController(clientAgent);
			} else if (agentArgs instanceof ServerArgs serverAgent) {
				agentController = createServerController(serverAgent, isInformer, managingAgent);
			} else if (agentArgs instanceof CloudNetworkArgs cloudNetworkAgent) {
				agentController = createCloudNetworkController(cloudNetworkAgent, isInformer, managingAgent);
			} else if (agentArgs instanceof GreenEnergyArgs greenEnergyAgent) {
				agentController = createGreenSourceController(greenEnergyAgent, isInformer, managingAgent);
			} else if (agentArgs instanceof MonitoringArgs monitoringAgent) {
				agentController = createMonitoringController(monitoringAgent, isInformer, managingAgent);
			} else if (agentArgs instanceof SchedulerArgs schedulerAgent) {
				agentController = createSchedulerController(schedulerAgent, isInformer, managingAgent);
			}

			if (nonNull(agentController)) {
				final RulesController<?, ?> rulesController = new RulesController<>();
				agentNode.setDatabaseClient(timescaleDatabase);
				guiController.addAgentNodeToGraph(agentNode);
				agentController.putO2AObject(guiController, ASYNC);
				agentController.putO2AObject(agentNode, ASYNC);
				agentController.putO2AObject(rulesController, ASYNC);
			}

			return agentController;
		} catch (StaleProxyException e) {
			throw new JadeControllerException("Failed to run agent controller", e);
		}
	}

	@Override
	public void runAgentControllers(final List<AgentController> controllers, final long agentRunDelay) {
		var scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.schedule(
				() -> controllers.forEach(controller -> runAgentController(controller, agentRunDelay)),
				GRAPH_INITIALIZATION_DELAY, SECONDS);
		shutdownAndAwaitTermination(scheduledExecutor);
	}

	@Override
	public void runAgentController(final AgentController controller, long pause) {
		try {
			controller.start();
			controller.activate();
			TimeUnit.MILLISECONDS.sleep(pause);
		} catch (StaleProxyException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new JadeControllerException("Failed to run agent controller", e);
		}
	}

	@Override
	public void shutdownAndAwaitTermination(final ExecutorService executorService) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException ie) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	private AgentController createClientController(final ClientArgs clientAgent)
			throws StaleProxyException {
		final String startDate = clientAgent.formatClientTime(0);
		final String endDate = clientAgent.formatClientTime(clientAgent.getJob().getDuration());
		final String deadline = clientAgent.formatClientDeadline();

		return containerController.createNewAgent(clientAgent.getName(),
				"org.greencloud.agentsystem.agents.client.ClientAgent",
				new Object[] { mainDFAddress,
						mainHostPlatformId,
						startDate,
						endDate,
						deadline,
						clientAgent.getJob(),
						clientAgent.getJobId(),
						systemKnowledge });
	}

	private AgentController createSchedulerController(final SchedulerArgs schedulerAgent, Boolean isInformer,
			AID managingAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(schedulerAgent.getName(),
				"org.greencloud.agentsystem.agents.scheduler.SchedulerAgent",
				new Object[] { schedulerAgent.getDeadlineWeight(),
						schedulerAgent.getCpuWeight(),
						schedulerAgent.getMaximumQueueSize(),
						systemKnowledge,
						isInformer,
						managingAgent});
	}

	private AgentController createCloudNetworkController(final CloudNetworkArgs cloudNetworkAgent, Boolean isInformer,
			AID managingAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(cloudNetworkAgent.getName(),
				"org.greencloud.agentsystem.agents.cloudnetwork.CloudNetworkAgent",
				new Object[] { mainDFAddress, mainHostPlatformId, systemKnowledge, isInformer, managingAgent });
	}

	private AgentController createServerController(final ServerArgs serverAgent, Boolean isInformer,
			AID managingAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(serverAgent.getName(),
				"org.greencloud.agentsystem.agents.server.ServerAgent",
				new Object[] { serverAgent.getOwnerCloudNetwork(),
						serverAgent.getPrice(),
						serverAgent.getMaxPower(),
						serverAgent.getIdlePower(),
						serverAgent.getJobProcessingLimit(),
						serverAgent.getResources(),
						systemKnowledge,
						isInformer,
						managingAgent});
	}

	private AgentController createGreenSourceController(final GreenEnergyArgs greenEnergyAgent, Boolean isInformer,
			AID managingAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(greenEnergyAgent.getName(),
				"org.greencloud.agentsystem.agents.greenenergy.GreenEnergyAgent",
				new Object[] { greenEnergyAgent.getMonitoringAgent(),
						greenEnergyAgent.getOwnerSever(),
						greenEnergyAgent.getMaximumCapacity(),
						greenEnergyAgent.getPricePerPowerUnit(),
						greenEnergyAgent.getLatitude(),
						greenEnergyAgent.getLongitude(),
						greenEnergyAgent.getEnergyType(),
						greenEnergyAgent.getWeatherPredictionError(),
						systemKnowledge,
						isInformer,
						managingAgent});
	}

	private AgentController createMonitoringController(final MonitoringArgs monitoringAgent, Boolean isInformer,
			AID managingAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(monitoringAgent.getName(),
				"org.greencloud.agentsystem.agents.monitoring.MonitoringAgent",
				new Object[] { monitoringAgent.getBadStubProbability(), systemKnowledge, isInformer, managingAgent });
	}
}
