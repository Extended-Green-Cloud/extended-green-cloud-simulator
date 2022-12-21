package runner;

import static java.io.File.separator;
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

	private static final String SCENARIO_NAME = "multipleCNAsScenario";

	private static final boolean VERIFY = false;
	private static final String ADAPTATION_TO_VERIFY = "change_green_source_weight";
	private static final String VERIFY_SCENARIO = "singleServerMultipleGreenSourcesScenario";

	private static final boolean EVENTS = false;
	private static final String EVENTS_SCENARIO = "triggerChangeWeight";

	private static final String DEFAULT_SCENARIO_DIRECTORY = "";
	private static final String VERIFY_SCENARIO_DIRECTORY = "adaptation" + separator + ADAPTATION_TO_VERIFY + separator;

	public static void main(String[] args) throws ExecutionException, InterruptedException, StaleProxyException {
		String scenarioPath = VERIFY ? VERIFY_SCENARIO_DIRECTORY : DEFAULT_SCENARIO_DIRECTORY;
		String scenarioFilePath = scenarioPath + (VERIFY ? VERIFY_SCENARIO : SCENARIO_NAME);
		Optional<String> scenarioEvents = Optional.empty();

		if (EVENTS) {
			scenarioEvents = Optional.of(VERIFY_SCENARIO_DIRECTORY + EVENTS_SCENARIO);
		}

		if (MULTI_CONTAINER) {
			runMultiContainerService(scenarioFilePath, scenarioEvents);
		} else {
			runSingleContainerService(scenarioFilePath, scenarioEvents);
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
