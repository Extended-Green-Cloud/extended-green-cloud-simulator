package runner.service;

import static runner.service.domain.ScenarioConstants.CLIENT_NUMBER;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import runner.domain.CloudNetworkArgs;
import runner.domain.GreenEnergyAgentArgs;
import runner.domain.MonitoringAgentArgs;
import runner.domain.ScenarioArgs;
import runner.domain.ServerAgentArgs;
import runner.factory.AgentControllerFactory;
import runner.factory.AgentControllerFactoryImpl;

public class MultiContainerScenarioService extends AbstractScenarioService implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(MultiContainerScenarioService.class);

	/**
	 * Service constructor
	 */
	public MultiContainerScenarioService(String fileName)
			throws StaleProxyException, ExecutionException, InterruptedException {
		super(fileName);
	}

	@Override
	public void run() {
		File scenarioFile = readFile(fileName);
		ScenarioArgs scenario = parseScenario(scenarioFile);
		List<AgentController> controllers = runCloudNetworkContainers(scenario);
		guiController.createEdges();
		runAgents(controllers);
		AgentControllerFactory clientFactory = new AgentControllerFactoryImpl(runAgentsContainer("clientsContainer"));
		runClientAgents(CLIENT_NUMBER, scenario, clientFactory);
	}

	private List<AgentController> runCloudNetworkContainers(ScenarioArgs scenario) {
		var cloudNetworkArgs = scenario.getCloudNetworkAgentsArgs();
		var serversArgs = scenario.getServerAgentsArgs();
		var sourcesArgs = scenario.getGreenEnergyAgentsArgs();
		var monitorsArgs = scenario.getMonitoringAgentsArgs();

		return cloudNetworkArgs.stream()
				.map(arg -> addAgentsToContainer(arg, scenario, serversArgs, sourcesArgs, monitorsArgs))
				.flatMap(Collection::stream)
				.toList();
	}

	private List<AgentController> addAgentsToContainer(CloudNetworkArgs cloudNetworkArgs, ScenarioArgs scenario,
			List<ServerAgentArgs> serversArgs, List<GreenEnergyAgentArgs> sourcesArgs,
			List<MonitoringAgentArgs> monitorsArgs) {
		var container = runAgentsContainer(cloudNetworkArgs.getName());
		var factory = new AgentControllerFactoryImpl(container);
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

	private ContainerController runAgentsContainer(String containerName) {
		var profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		profile.setParameter(Profile.MAIN_PORT, "6996");
		try {
			return executorService.submit(() -> jadeRuntime.createAgentContainer(profile)).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Failed to create CloudNetworkContainer container with exception {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
