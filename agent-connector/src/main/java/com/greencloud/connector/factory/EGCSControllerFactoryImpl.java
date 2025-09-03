package com.greencloud.connector.factory;

import static jade.wrapper.AgentController.ASYNC;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.jrba.rulesengine.rest.RuleSetRestApi.addAgentNode;

import java.util.Map;
import java.util.Optional;

import org.greencloud.commons.args.agent.ImmutableEGCSAgentControllerArgs;
import org.greencloud.commons.args.agent.centralmanager.factory.CentralManagerArgs;
import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.regionalmanager.factory.RegionalManagerArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.exception.JadeControllerException;
import org.greencloud.commons.exception.UnknownAgentTypeException;
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
import lombok.Getter;

@SuppressWarnings("rawtypes")
public class EGCSControllerFactoryImpl extends AgentControllerFactoryImpl implements EGCSControllerFactory {

	private static final Logger logger = LoggerFactory.getLogger(EGCSControllerFactoryImpl.class);

	@Getter
	private final TimescaleDatabase timescaleDatabase;
	private final AgentNodeFactory agentNodeFactory;
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
		return createController(agentArgs, null, false, empty(), null);
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs, final EGCSNode agentNode) {
		return createController(agentArgs, null, false, empty(), agentNode);
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs, final ScenarioStructureArgs scenario) {
		return createController(agentArgs, scenario, false, empty(), null);
	}

	@Override
	public AgentController createAgentController(final AgentArgs agentArgs, final ScenarioStructureArgs scenario,
			boolean isInformer, AID managingAgent) {
		return createController(agentArgs, scenario, isInformer, of(managingAgent), null);
	}

	@Override
	public void connectAgentControllerToRulesController(final AgentController agentController, final EGCSNode agentNode)
			throws StaleProxyException {
		final RulesController<?, ?> rulesController = new RulesController<>();
		agentNode.setDatabaseClient(timescaleDatabase);
		guiController.addAgentNodeToGraph(agentNode);
		addAgentNode(agentNode);
		agentController.putO2AObject(guiController, ASYNC);
		agentController.putO2AObject(agentNode, ASYNC);
		agentController.putO2AObject(rulesController, ASYNC);
	}

	private AgentController createController(final AgentArgs agentArgs,
			final ScenarioStructureArgs scenario,
			final Boolean isInformer,
			final Optional<AID> managingAgent,
			final EGCSNode node) {
		try {
			logger.info("Creating {} agent.", agentArgs.getName());

			final EGCSNode agentNode = ofNullable(node).orElse(agentNodeFactory.createAgentNode(agentArgs, scenario));
			final AgentController agentController = switch (agentArgs) {
				case ClientArgs client -> createClientController(client);
				case ServerArgs server -> createServerController(server, isInformer, managingAgent);
				case RegionalManagerArgs rma -> createRegionalManagerController(rma, isInformer, managingAgent);
				case GreenEnergyArgs greenEnergy -> createGreenSourceController(greenEnergy, isInformer, managingAgent);
				case MonitoringArgs monitoring -> createMonitoringController(monitoring, isInformer, managingAgent);
				case CentralManagerArgs cma -> createCentralManagerController(cma, isInformer, managingAgent);
				default -> throw new UnknownAgentTypeException();
			};
			connectAgentControllerToRulesController(agentController, agentNode);
			return agentController;
		} catch (StaleProxyException e) {
			throw new JadeControllerException("Failed to run agent controller", e);
		}
	}

	private AgentController createClientController(final ClientArgs clientAgent)
			throws StaleProxyException {
		return containerController.createNewAgent(clientAgent.getName(),
				"org.greencloud.agentsystem.agents.client.ClientAgent",
				new Object[] { ImmutableEGCSAgentControllerArgs.builder()
						.agentArgs(clientAgent)
						.mainDFAddress(mainDFAddress)
						.mainHostPlatformId(mainHostPlatformId)
						.systemKnowledge(ofNullable(systemKnowledge))
						.isInformer(false)
						.managingAgent(empty())
						.build() });
	}

	private AgentController createCentralManagerController(final CentralManagerArgs cmaAgent, final Boolean isInformer,
			final Optional<AID> managingAgent) throws StaleProxyException {
		return containerController.createNewAgent(cmaAgent.getName(),
				"org.greencloud.agentsystem.agents.centralmanager.CentralManagerAgent",
				new Object[] { ImmutableEGCSAgentControllerArgs.builder()
						.agentArgs(cmaAgent)
						.mainDFAddress(empty())
						.mainHostPlatformId(empty())
						.systemKnowledge(ofNullable(systemKnowledge))
						.isInformer(isInformer)
						.managingAgent(managingAgent)
						.build() });
	}

	private AgentController createRegionalManagerController(final RegionalManagerArgs regionalManagerArgs,
			final Boolean isInformer, final Optional<AID> managingAgent) throws StaleProxyException {
		return containerController.createNewAgent(regionalManagerArgs.getName(),
				"org.greencloud.agentsystem.agents.regionalmanager.RegionalManagerAgent",
				new Object[] { ImmutableEGCSAgentControllerArgs.builder()
						.agentArgs(regionalManagerArgs)
						.mainDFAddress(mainDFAddress)
						.mainHostPlatformId(mainHostPlatformId)
						.systemKnowledge(ofNullable(systemKnowledge))
						.isInformer(isInformer)
						.managingAgent(managingAgent)
						.build() });
	}

	private AgentController createServerController(final ServerArgs serverAgent, final Boolean isInformer,
			final Optional<AID> managingAgent) throws StaleProxyException {
		return containerController.createNewAgent(serverAgent.getName(),
				"org.greencloud.agentsystem.agents.server.ServerAgent",
				new Object[] { ImmutableEGCSAgentControllerArgs.builder()
						.agentArgs(serverAgent)
						.mainDFAddress(empty())
						.mainHostPlatformId(empty())
						.systemKnowledge(ofNullable(systemKnowledge))
						.isInformer(isInformer)
						.managingAgent(managingAgent)
						.build() });
	}

	private AgentController createGreenSourceController(final GreenEnergyArgs greenEnergyAgent,
			final Boolean isInformer, final Optional<AID> managingAgent) throws StaleProxyException {
		return containerController.createNewAgent(greenEnergyAgent.getName(),
				"org.greencloud.agentsystem.agents.greenenergy.GreenEnergyAgent",
				new Object[] { ImmutableEGCSAgentControllerArgs.builder()
						.agentArgs(greenEnergyAgent)
						.mainDFAddress(empty())
						.mainHostPlatformId(empty())
						.systemKnowledge(ofNullable(systemKnowledge))
						.isInformer(isInformer)
						.managingAgent(managingAgent)
						.build() });
	}

	private AgentController createMonitoringController(final MonitoringArgs monitoringAgent, final Boolean isInformer,
			final Optional<AID> managingAgent) throws StaleProxyException {
		return containerController.createNewAgent(monitoringAgent.getName(),
				"org.greencloud.agentsystem.agents.monitoring.MonitoringAgent",
				new Object[] { ImmutableEGCSAgentControllerArgs.builder()
						.agentArgs(monitoringAgent)
						.mainDFAddress(empty())
						.mainHostPlatformId(empty())
						.systemKnowledge(ofNullable(systemKnowledge))
						.isInformer(isInformer)
						.managingAgent(managingAgent)
						.build() });
	}
}
