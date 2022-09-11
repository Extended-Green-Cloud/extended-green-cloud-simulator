package runner.service;

import static runner.service.domain.ScenarioConstants.CLIENT_NUMBER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import runner.domain.AgentArgs;
import runner.domain.ScenarioArgs;
import runner.factory.AgentControllerFactory;
import runner.factory.AgentControllerFactoryImpl;

/**
 * Service used in running the scenarios on a single physical host.
 */
public class SingleContainerScenarioService extends AbstractScenarioService implements Runnable {

	private static final List<AgentController> AGENTS_TO_RUN = new ArrayList<>();

	private final AgentControllerFactory factory;

	/**
	 * Runs single scenario service with a single controller on a single physical host.
	 * @param fileName name of the XML scenario document
	 */
	public SingleContainerScenarioService(String fileName)
			throws StaleProxyException, ExecutionException, InterruptedException {
		super(fileName);
		this.factory = new AgentControllerFactoryImpl(mainContainer);
	}

	@Override
	public void run() {
		final File scenarioFile = readFile(fileName);
		final ScenarioArgs scenario = parseScenario(scenarioFile);
		if (Objects.nonNull(scenario.getAgentsArgs())) {
			createAgents(scenario.getMonitoringAgentsArgs(), scenario);
			createAgents(scenario.getGreenEnergyAgentsArgs(), scenario);
			createAgents(scenario.getServerAgentsArgs(), scenario);
			createAgents(scenario.getCloudNetworkAgentsArgs(), scenario);
		}
		guiController.createEdges();
		runAgents(AGENTS_TO_RUN);
		runClientAgents(CLIENT_NUMBER, scenario, factory);
	}

	private void createAgents(List<?> agentArgsList, ScenarioArgs scenario) {
		agentArgsList.forEach(agentArgs -> {
			var args = (AgentArgs) agentArgs;
			AGENTS_TO_RUN.add(runAgentController(args, scenario, factory));
		});
	}
}
