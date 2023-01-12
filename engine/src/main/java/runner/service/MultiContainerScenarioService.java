package runner.service;

import static com.greencloud.application.utils.TimeUtils.setSystemStartTime;
import static runner.service.domain.ContainerTypeEnum.CLIENTS_CONTAINER_ID;
import static runner.service.domain.ScenarioConstants.CLIENT_NUMBER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.greencloud.commons.args.agent.cloudnetwork.CloudNetworkArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.scenario.ScenarioStructureArgs;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import runner.factory.AgentControllerFactory;
import runner.factory.AgentControllerFactoryImpl;

/**
 * Scenario service responsible for running Green Cloud dispersed among multiple hosts.
 * Each host is responsible for running for single Cloud Network. Additionally, to that
 */
public class MultiContainerScenarioService extends AbstractScenarioService implements Runnable {

	private boolean mainHost = true;
	private int hostId;

	/**
	 * Service's constructor for the main host. Main host is responsible for running Main-Container
	 * which contains within itself Jade's RMA, SNIFFER, DFService.
	 *
	 * @param scenarioStructureFileName name of the XML scenario document containing network structure
	 */
	public MultiContainerScenarioService(String scenarioStructureFileName)
			throws StaleProxyException, ExecutionException, InterruptedException {
		super(scenarioStructureFileName, Optional.empty());
	}

	/**
	 * Service's constructor for the remote host. Creates an AgentContainer which contains
	 * all agents underlying to the CloudNetworkAgent corresponding to
	 * the given host id.
	 *
	 * @param fileName               name of the XML scenario document
	 * @param hostId                 number of the host id
	 * @param mainHostIp             IP address of the main host
	 * @param scenarioEventsFileName (optional) name of the XML scenario document containing list of events triggered during scenario execution
	 */
	public MultiContainerScenarioService(String fileName, Optional<String> scenarioEventsFileName, Integer hostId,
			String mainHostIp) {
		super(fileName, hostId, mainHostIp, scenarioEventsFileName);
		mainHost = false;
		this.hostId = hostId;
	}

	/**
	 * Runs AgentContainer. For example host with id = 1 would run all agents for first CNA declared
	 * in the XML scenario document. A special case is host with id = 0, such host is responsible
	 * for generating Client Agents.
	 */
	@Override
	public void run() {
		File scenarioFile = readFile(scenarioStructureFileName);
		scenario = parseScenarioStructure(scenarioFile);
		setSystemStartTime(timescaleDatabase.readSystemStartTime());

		if (mainHost) {
			runCommonAgentContainers(scenario);
			return;
		}

		if (hostId == CLIENTS_CONTAINER_ID.ordinal()) {
			if (Objects.nonNull(scenarioEventsFileName)) {
				var factory = new AgentControllerFactoryImpl(mainContainer);
				eventService.runScenarioEvents(factory);
			} else {
				runClients();
			}
		} else {
			List<AgentController> controllers = runCloudNetworkContainers(scenario, hostId);
			runAgents(controllers);
		}
	}

	private void runClients() {
		AgentControllerFactory clientFactory = new AgentControllerFactoryImpl(mainContainer);
		runClientAgents(CLIENT_NUMBER, clientFactory);
	}

	private void runCommonAgentContainers(ScenarioStructureArgs scenario) {
		final AgentControllerFactory factory = new AgentControllerFactoryImpl(mainContainer);
		final AgentController schedulerController = runAgentController(scenario.getSchedulerAgentArgs(),
				scenario, factory);
		final AgentController managingAgentController = runAgentController(scenario.getManagingAgentArgs(),
				scenario, factory);
		runAgent(schedulerController, RUN_AGENT_PAUSE);
		runAgent(managingAgentController, RUN_AGENT_PAUSE);
	}

	private List<AgentController> runCloudNetworkContainers(ScenarioStructureArgs scenario, Integer hostId) {
		var cloudNetworkArgs = scenario.getCloudNetworkAgentsArgs();
		var serversArgs = scenario.getServerAgentsArgs();
		var sourcesArgs = scenario.getGreenEnergyAgentsArgs();
		var monitorsArgs = scenario.getMonitoringAgentsArgs();

		return addAgentsToContainer(cloudNetworkArgs.get(hostId - 1), scenario, serversArgs, sourcesArgs, monitorsArgs);
	}

	private List<AgentController> addAgentsToContainer(CloudNetworkArgs cloudNetworkArgs,
			ScenarioStructureArgs scenario,
			List<ServerAgentArgs> serversArgs, List<GreenEnergyAgentArgs> sourcesArgs,
			List<MonitoringAgentArgs> monitorsArgs) {
		var factory = new AgentControllerFactoryImpl(mainContainer);
		var servers = serversArgs.stream()
				.filter(server -> server.getOwnerCloudNetwork().equals(cloudNetworkArgs.getName()))
				.toList();
		var sources = sourcesArgs.stream()
				.filter(source -> servers.stream().anyMatch(server -> server.getName().equals(source.getOwnerSever())))
				.toList();
		var monitors = monitorsArgs.stream()
				.filter(monitor -> sources.stream()
						.anyMatch(source -> source.getMonitoringAgent().equals(monitor.getName())))
				.toList();
		var controllers = new ArrayList<AgentController>();
		controllers.addAll(monitors.stream().map(m -> runAgentController(m, scenario, factory)).toList());
		controllers.addAll(sources.stream().map(s -> runAgentController(s, scenario, factory)).toList());
		controllers.addAll(servers.stream().map(s -> runAgentController(s, scenario, factory)).toList());
		controllers.add(runAgentController(cloudNetworkArgs, scenario, factory));
		return controllers;
	}
}
