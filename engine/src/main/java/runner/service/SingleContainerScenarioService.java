package runner.service;

import static com.greencloud.factory.constants.AgentControllerConstants.RUN_AGENT_DELAY;
import static runner.configuration.EngineConfiguration.mainDFAddress;
import static runner.configuration.EngineConfiguration.mainHostPlatformId;
import static runner.configuration.ScenarioConfiguration.eventFilePath;
import static runner.configuration.ScenarioConfiguration.scenarioFilePath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.greencloud.factory.AgentControllerFactoryImpl;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Service used in running the scenarios on a single physical host.
 */
public class SingleContainerScenarioService extends AbstractScenarioService implements Runnable {

	private static final List<AgentController> AGENTS_TO_RUN = new ArrayList<>();

	/**
	 * Runs single scenario service with a single controller on a single physical host.
	 */
	public SingleContainerScenarioService()
			throws StaleProxyException, ExecutionException, InterruptedException {
		super();
		this.factory = new AgentControllerFactoryImpl(mainContainer, timescaleDatabase, guiController, mainDFAddress,
				mainHostPlatformId);
	}

	@Override
	public void run() {
		final File scenarioStructureFile = readFile(scenarioFilePath);
		scenario = parseScenarioStructure(scenarioStructureFile);
		if (Objects.nonNull(scenario.getAgentsArgs())) {
			createAgents(List.of(scenario.getManagingAgentArgs()), scenario);
			createAgents(List.of(scenario.getSchedulerAgentArgs()), scenario);
			createAgents(scenario.getMonitoringAgentsArgs(), scenario);
			createAgents(scenario.getGreenEnergyAgentsArgs(), scenario);
			createAgents(scenario.getServerAgentsArgs(), scenario);
			createAgents(scenario.getCloudNetworkAgentsArgs(), scenario);
		}
		updateSystemStartTime();
		factory.runAgentControllers(AGENTS_TO_RUN, RUN_AGENT_DELAY);

		if (eventFilePath.isEmpty()) {
			runClientAgents();
		} else {
			eventService.runScenarioEvents();
		}
	}

	private void createAgents(List<?> agentArgsList, ScenarioStructureArgs scenario) {
		agentArgsList.forEach(agentArgs -> {
			var args = (AgentArgs) agentArgs;
			AGENTS_TO_RUN.add(factory.createAgentController(args, scenario));
		});
	}
}
