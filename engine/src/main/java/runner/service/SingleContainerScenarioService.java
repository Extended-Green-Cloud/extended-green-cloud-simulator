package runner.service;

import static com.greencloud.application.utils.TimeUtils.setSystemStartTime;
import static runner.service.domain.ScenarioConstants.CLIENT_NUMBER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.scenario.ScenarioStructureArgs;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
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
	 *
	 * @param scenarioStructureFileName name of the XML scenario document containing network structure
	 * @param scenarioEventsFileName    (optional) name of the XML scenario document containing list of events triggered during scenario execution
	 */
	public SingleContainerScenarioService(String scenarioStructureFileName, Optional<String> scenarioEventsFileName)
			throws StaleProxyException, ExecutionException, InterruptedException {
		super(scenarioStructureFileName, scenarioEventsFileName);
		this.factory = new AgentControllerFactoryImpl(mainContainer);
	}

	@Override
	public void run() {
		final File scenarioStructureFile = readFile(scenarioStructureFileName);
		scenario = parseScenarioStructure(scenarioStructureFile);
		if (Objects.nonNull(scenario.getAgentsArgs())) {
			createAgents(List.of(scenario.getManagingAgentArgs()), scenario);
			createAgents(List.of(scenario.getSchedulerAgentArgs()), scenario);
			createAgents(scenario.getMonitoringAgentsArgs(), scenario);
			createAgents(scenario.getGreenEnergyAgentsArgs(), scenario);
			createAgents(scenario.getServerAgentsArgs(), scenario);
			createAgents(scenario.getCloudNetworkAgentsArgs(), scenario);
		}
		runAgents(AGENTS_TO_RUN);
		setSystemStartTime();

		if (Objects.isNull(scenarioEventsFileName)) {
			runClientAgents(CLIENT_NUMBER, factory);
		} else {
			eventService.runScenarioEvents(factory);
		}
	}

	private void createAgents(List<?> agentArgsList, ScenarioStructureArgs scenario) {
		agentArgsList.forEach(agentArgs -> {
			var args = (AgentArgs) agentArgs;
			AGENTS_TO_RUN.add(runAgentController(args, scenario, factory));
		});
	}
}
