package runner.service;

import static runner.service.domain.ScenarioConstants.CLIENTS_CONTAINER_ID;
import static runner.service.domain.ScenarioConstants.CLIENT_NUMBER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.greencloud.commons.args.cloudnetwork.CloudNetworkArgs;
import com.greencloud.commons.args.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.server.ServerAgentArgs;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import runner.domain.ScenarioArgs;
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
	 * @param fileName name of the XML scenario document
	 */
	public MultiContainerScenarioService(String fileName)
			throws StaleProxyException, ExecutionException, InterruptedException {
		super(fileName);
	}

	/**
	 * Service's constructor for the remote host. Creates an AgentContainer which contains
	 * all agents underlying to the CloudNetworkAgent corresponding to
	 * the given host id.
	 *
	 * @param fileName   name of the XML scenario document
	 * @param hostId     number of the host id
	 * @param mainHostIp IP address of the main host
	 */
	public MultiContainerScenarioService(String fileName, Integer hostId, String mainHostIp) {
		super(fileName, hostId, mainHostIp);
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
		if (mainHost) {
			return;
		}

		File scenarioFile = readFile(fileName);
		ScenarioArgs scenario = parseScenario(scenarioFile);

		if (hostId == CLIENTS_CONTAINER_ID) {
			runClients(scenario);
		} else {
			List<AgentController> controllers = runCloudNetworkContainers(scenario, hostId);
			runAgents(controllers);
		}
	}

	private void runClients(ScenarioArgs scenario) {
		AgentControllerFactory clientFactory = new AgentControllerFactoryImpl(mainContainer);
		runClientAgents(CLIENT_NUMBER, scenario, clientFactory);
	}

	private List<AgentController> runCloudNetworkContainers(ScenarioArgs scenario, Integer hostId) {
		var cloudNetworkArgs = scenario.getCloudNetworkAgentsArgs();
		var serversArgs = scenario.getServerAgentsArgs();
		var sourcesArgs = scenario.getGreenEnergyAgentsArgs();
		var monitorsArgs = scenario.getMonitoringAgentsArgs();

		return addAgentsToContainer(cloudNetworkArgs.get(hostId - 1), scenario, serversArgs, sourcesArgs, monitorsArgs);
	}

	private List<AgentController> addAgentsToContainer(CloudNetworkArgs cloudNetworkArgs, ScenarioArgs scenario,
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
