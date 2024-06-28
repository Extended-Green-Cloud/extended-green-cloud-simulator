package runner.service;

import static com.greencloud.connector.factory.constants.AgentControllerConstants.RUN_AGENT_DELAY;
import static org.jrba.utils.file.FileReader.readFile;
import static runner.configuration.EngineConfiguration.mainDFAddress;
import static runner.configuration.EngineConfiguration.mainHostPlatformId;
import static runner.configuration.ScenarioConfiguration.knowledgeFilePath;
import static runner.configuration.ScenarioConfiguration.scenarioFilePath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.jrba.agentmodel.domain.args.AgentArgs;

import com.greencloud.connector.factory.EGCSControllerFactoryImpl;

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
		final File scenarioStructureFile = readFile(scenarioFilePath);
		final File initialKnowledgeFile = readFile(knowledgeFilePath);
		systemKnowledge = parseKnowledgeStructure(initialKnowledgeFile);
		scenario = parseScenarioStructure(scenarioStructureFile);
		updateStrategyInformation();

		this.factory = new EGCSControllerFactoryImpl(mainContainer, timescaleDatabase, guiController, mainDFAddress,
				mainHostPlatformId, systemKnowledge);
		guiController.connectWithAgentFactory(factory);
	}

	@Override
	public void run() {
		startRuleSetAPI();
		if (Objects.nonNull(scenario.getAgentsArgs())) {
			AGENTS_TO_RUN.add(prepareManagingController(scenario.getManagingAgentArgs()));
			createAgents(List.of(scenario.getCentralManagerAgentArgs()), scenario);
			createAgents(scenario.getMonitoringAgentsArgs(), scenario);
			createAgents(scenario.getGreenEnergyAgentsArgs(), scenario);
			createAgents(scenario.getServerAgentsArgs(), scenario);
			createAgents(scenario.getRegionalManagerAgentsArgs(), scenario);
		}
		updateSystemStartTime();
		factory.runAgentControllers(AGENTS_TO_RUN, RUN_AGENT_DELAY);
		workloadGenerator.generateWorkloadForSimulation();
	}

	private void createAgents(List<?> agentArgsList, ScenarioStructureArgs scenario) {
		agentArgsList.forEach(agentArgs -> {
			var args = (AgentArgs) agentArgs;
			AGENTS_TO_RUN.add(factory.createAgentController(args, scenario));
		});
	}
}
