package com.greencloud.connector.factory;

import static jade.wrapper.AgentController.ASYNC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.jrba.rulesengine.rest.RuleSetRestApi.addAgentNode;

import java.util.Map;

import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.regionalmanager.factory.RegionalManagerArgs;
import org.greencloud.commons.args.agent.scheduler.factory.SchedulerArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.exception.JadeControllerException;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.jrba.agentmodel.domain.args.AgentArgs;
import org.jrba.rulesengine.RulesController;
import org.jrba.utils.factory.AgentControllerFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.connector.gui.GuiController;

import jade.core.AID;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class EGCSControllerFactoryImpl extends AgentControllerFactoryImpl implements EGCSControllerFactory {

	private static final Logger logger = LoggerFactory.getLogger(EGCSControllerFactoryImpl.class);
	private final AgentNodeFactory agentNodeFactory;
	private final TimescaleDatabase timescaleDatabase;
	private final GuiController guiController;
	private String mainDFAddress;
	private String mainHostPlatformId;
	private Map<String, Map<String, Object>> systemKnowledge;

	public EGCSControllerFactoryImpl(final ContainerController containerController,
			final TimescaleDatabase timescaleDatabase,
			final GuiController guiController) {
		super(containerController);
		this.agentNodeFactory = new AgentNodeFactoryImpl();
		this.timescaleDatabase = timescaleDatabase;
		this.guiController = guiController;
		this.mainDFAddress = null;
		this.mainHostPlatformId = null;
		this.systemKnowledge = null;
	}

	public EGCSControllerFactoryImpl(final ContainerController containerController,
			final TimescaleDatabase timescaleDatabase,
			final GuiController guiController,
			final String mainDFAddress,
			final String mainHostPlatformId,
			final Map<String, Map<String, Object>> systemKnowledge) {
		this(containerController, timescaleDatabase, guiController);
		this.mainDFAddress = mainDFAddress;
		this.mainHostPlatformId = mainHostPlatformId;
		this.systemKnowledge = systemKnowledge;
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs) {
		return createController(agentArgs, null, false, null, null);
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs, final EGCSNode<?, ?> agentNode) {
		return createController(agentArgs, null, false, null, agentNode);
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs, final ScenarioStructureArgs scenario) {
		return createController(agentArgs, scenario, false, null, null);
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs, final ScenarioStructureArgs scenario,
			boolean isInformer, AID managingAgent) {
		return createController(agentArgs, scenario, isInformer, managingAgent, null);
	}

	private AgentController createController(final AgentArgs agentArgs, final ScenarioStructureArgs scenario,
			Boolean isInformer, AID managingAgent, final EGCSNode<?, ?> node) {
		final EGCSNode<?, ?> agentNode = isNull(node) ? agentNodeFactory.createAgentNode(agentArgs, scenario) : node;
		var agentController = (AgentController) null;

		try {
			logger.info("Created {} agent.", agentArgs.getName());
			if (agentArgs instanceof ClientArgs clientAgent) {
				agentController = createClientController(clientAgent);
			} else if (agentArgs instanceof ServerArgs serverAgent) {
				agentController = createServerController(serverAgent, isInformer, managingAgent);
			} else if (agentArgs instanceof RegionalManagerArgs regionalManagerArgs) {
				agentController = createRegionalManagerController(regionalManagerArgs, isInformer, managingAgent);
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
				addAgentNode(agentNode);
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
	public TimescaleDatabase getDatabase() {
		return timescaleDatabase;
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
						ofNullable(systemKnowledge) });
	}

	private AgentController createSchedulerController(final SchedulerArgs schedulerAgent, Boolean isInformer,
			AID managingAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(schedulerAgent.getName(),
				"org.greencloud.agentsystem.agents.scheduler.SchedulerAgent",
				new Object[] { schedulerAgent.getDeadlineWeight(),
						schedulerAgent.getCpuWeight(),
						schedulerAgent.getMaximumQueueSize(),
						ofNullable(systemKnowledge),
						isInformer,
						managingAgent });
	}

	private AgentController createRegionalManagerController(final RegionalManagerArgs regionalManagerArgs,
			Boolean isInformer,
			AID managingAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(regionalManagerArgs.getName(),
				"org.greencloud.agentsystem.agents.regionalmanager.RegionalManagerAgent",
				new Object[] { mainDFAddress,
						mainHostPlatformId,
						ofNullable(systemKnowledge),
						isInformer,
						managingAgent });
	}

	private AgentController createServerController(final ServerArgs serverAgent, Boolean isInformer,
			AID managingAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(serverAgent.getName(),
				"org.greencloud.agentsystem.agents.server.ServerAgent",
				new Object[] { serverAgent.getOwnerRegionalManager(),
						serverAgent.getPrice(),
						serverAgent.getMaxPower(),
						serverAgent.getIdlePower(),
						serverAgent.getJobProcessingLimit(),
						serverAgent.getResources(),
						ofNullable(systemKnowledge),
						isInformer,
						managingAgent });
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
						ofNullable(systemKnowledge),
						isInformer,
						managingAgent });
	}

	private AgentController createMonitoringController(final MonitoringArgs monitoringAgent, Boolean isInformer,
			AID managingAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(monitoringAgent.getName(),
				"org.greencloud.agentsystem.agents.monitoring.MonitoringAgent",
				new Object[] { monitoringAgent.getBadStubProbability(),
						ofNullable(systemKnowledge),
						isInformer,
						managingAgent });
	}
}
