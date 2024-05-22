package runner.service;

import static com.greencloud.connector.factory.constants.AgentControllerConstants.RUN_AGENT_DELAY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.jrba.utils.file.FileReader.readFile;
import static runner.configuration.EngineConfiguration.containerId;
import static runner.configuration.EngineConfiguration.locationId;
import static runner.configuration.EngineConfiguration.mainDFAddress;
import static runner.configuration.EngineConfiguration.mainHost;
import static runner.configuration.EngineConfiguration.mainHostPlatformId;
import static runner.configuration.EngineConfiguration.newPlatform;
import static runner.configuration.ScenarioConfiguration.knowledgeFilePath;
import static runner.configuration.ScenarioConfiguration.scenarioFilePath;
import static runner.configuration.enums.ContainerTypeEnum.CLIENTS_CONTAINER_ID;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.regionalmanager.factory.RegionalManagerArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.jrba.agentmodel.domain.args.AgentArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.connector.factory.EGCSControllerFactoryImpl;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * Scenario service responsible for running Green Cloud dispersed among multiple hosts.
 * Each host is responsible for running for single Regional Manager. Additionally, to that
 */
public class MultiContainerScenarioService extends AbstractScenarioService implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(MultiContainerScenarioService.class);

	/**
	 * Service's constructor for the main host. Main host is responsible for running Main-Container
	 * which contains within itself Jade's RMA, SNIFFER, DFService.
	 */
	public MultiContainerScenarioService()
			throws StaleProxyException, ExecutionException, InterruptedException {
		super();
		final ContainerController container = mainHost ? mainContainer : agentContainer;
		final File scenarioFile = readFile(scenarioFilePath);
		final File initialKnowledgeFile = readFile(knowledgeFilePath);
		systemKnowledge = parseKnowledgeStructure(initialKnowledgeFile);
		scenario = parseScenarioStructure(scenarioFile);

		this.factory = new EGCSControllerFactoryImpl(container, timescaleDatabase, guiController, mainDFAddress,
				mainHostPlatformId, systemKnowledge);
		guiController.connectWithAgentFactory(factory);
	}

	/**
	 * Runs AgentContainer. For example host with id = 1 would run all agents for first RMA declared
	 * in the XML scenario document. A special case is host with id = 0, such host is responsible
	 * for generating Client Agents.
	 */
	@Override
	public void run() {
		startRuleSetAPI();
		updateSystemStartTime();

		if (mainHost) {
			runCommonAgentContainers(scenario);
			return;
		}

		if (nonNull(locationId) && locationId.contains(CLIENTS_CONTAINER_ID.getName())) {
			workloadGenerator.generateWorkloadForSimulation();
		} else {
			final List<AgentController> controllers = runRegionalManagerContainers(scenario);
			if (controllers.isEmpty()) {
				logger.info("No agents to be run! Make sure that you passed a correct configuration.");
			} else {
				factory.runAgentControllers(controllers, RUN_AGENT_DELAY);
			}
		}
	}

	private void runCommonAgentContainers(final ScenarioStructureArgs scenario) {
		final AgentController cmaController = factory.createAgentController(scenario.getCentralManagerAgentArgs(),
				scenario);
		final AgentController managingAgentController = prepareManagingController(scenario.getManagingAgentArgs());

		factory.runAgentController(cmaController, RUN_AGENT_DELAY);
		factory.runAgentController(managingAgentController, RUN_AGENT_DELAY);
	}

	private List<AgentController> runRegionalManagerContainers(final ScenarioStructureArgs scenario) {
		var clouds = selectRegionalManagersForContainers();
		var servers = selectServersForContainer(clouds);
		var sources = selectGreenSourcesForContainer(servers);
		var monitors = selectMonitoringForContainer(sources);

		var controllers = new ArrayList<AgentController>();
		controllers.addAll(monitors.stream().map(m -> factory.createAgentController(m, scenario)).toList());
		controllers.addAll(sources.stream().map(s -> factory.createAgentController(s, scenario)).toList());
		controllers.addAll(servers.stream().map(s -> factory.createAgentController(s, scenario)).toList());

		if (newPlatform || isNull(containerId)) {
			controllers.addAll(clouds.stream().map(cloud -> factory.createAgentController(cloud, scenario)).toList());
		}
		return controllers;
	}

	private List<RegionalManagerArgs> selectRegionalManagersForContainers() {
		final List<RegionalManagerArgs> regionalManagerArgs = scenario.getRegionalManagerAgentsArgs();

		return regionalManagerArgs.stream()
				.filter(rmaArgs -> Objects.equals(rmaArgs.getLocationId(), locationId))
				.toList();
	}

	private List<ServerArgs> selectServersForContainer(final List<RegionalManagerArgs> regionalManagerArgs) {
		final List<ServerArgs> serverAgentsArgs = scenario.getServerAgentsArgs();

		return serverAgentsArgs.stream()
				.filter(serverArgs -> regionalManagerArgs.stream().map(AgentArgs::getName).toList()
						.contains(serverArgs.getOwnerRegionalManager()))
				.filter(serverArgs -> Objects.equals(serverArgs.getContainerId(), containerId))
				.toList();
	}

	private List<GreenEnergyArgs> selectGreenSourcesForContainer(final List<ServerArgs> serverAgentArgs) {
		final List<GreenEnergyArgs> greenEnergyAgentArgs = scenario.getGreenEnergyAgentsArgs();

		return greenEnergyAgentArgs.stream()
				.filter(sourceArgs ->
						serverAgentArgs.stream().map(AgentArgs::getName).toList().contains(sourceArgs.getOwnerSever()))
				.toList();
	}

	private List<MonitoringArgs> selectMonitoringForContainer(
			final List<GreenEnergyArgs> greenEnergyAgentArgs) {
		final List<MonitoringArgs> monitoringAgentArgs = scenario.getMonitoringAgentsArgs();

		return monitoringAgentArgs.stream()
				.filter(monitorArgs -> greenEnergyAgentArgs.stream().map(GreenEnergyArgs::getMonitoringAgent)
						.toList()
						.contains(monitorArgs.getName()))
				.toList();
	}
}
