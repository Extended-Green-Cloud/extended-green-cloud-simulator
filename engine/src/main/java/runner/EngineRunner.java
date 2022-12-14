package runner;

import static runner.service.domain.ScenarioConstants.HOST_ID;
import static runner.service.domain.ScenarioConstants.HOST_NAME;
import static runner.service.domain.ScenarioConstants.MAIN_HOST;
import static runner.service.domain.ScenarioConstants.MULTI_CONTAINER;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import jade.wrapper.StaleProxyException;
import runner.service.MultiContainerScenarioService;
import runner.service.SingleContainerScenarioService;

/**
 * Main method which runs the engine and the given scenario
 */
public class EngineRunner {

	// optional directory in which the scenario files are placed
	private static final String SCENARIO_DIRECTORY =
			"\\adaptation\\connectgreensource\\";
			//"";

	public static void main(String[] args) throws ExecutionException, InterruptedException, StaleProxyException {
		String scenarioStructure = SCENARIO_DIRECTORY + "simpleTestConnectGreenSourceScenario";
		Optional<String> scenarioEvents =
				//Optional.empty();
				Optional.of(SCENARIO_DIRECTORY + "simpleTestConnectGreenSourceEvents");

		if (MULTI_CONTAINER) {
			runMultiContainerService(scenarioStructure, scenarioEvents);
		} else {
			runSingleContainerService(scenarioStructure, scenarioEvents);
		}
	}

	public static void runSingleContainerService(String scenarioStructure, Optional<String> scenarioEvents)
			throws StaleProxyException, ExecutionException, InterruptedException {
		var scenarioService = new SingleContainerScenarioService(scenarioStructure, scenarioEvents);
		scenarioService.run();
	}

	public static void runMultiContainerService(String scenarioStructure, Optional<String> scenarioEvents)
			throws StaleProxyException, ExecutionException, InterruptedException {
		MultiContainerScenarioService scenarioService;
		if (MAIN_HOST) {
			scenarioService = new MultiContainerScenarioService(scenarioStructure);
		} else {
			scenarioService = new MultiContainerScenarioService(scenarioStructure, scenarioEvents, HOST_ID, HOST_NAME);
		}
		scenarioService.run();
	}
}
