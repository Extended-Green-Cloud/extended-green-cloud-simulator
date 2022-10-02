package runner;

import static runner.service.domain.ScenarioConstants.HOST_ID;
import static runner.service.domain.ScenarioConstants.HOST_NAME;
import static runner.service.domain.ScenarioConstants.MAIN_HOST;
import static runner.service.domain.ScenarioConstants.MULTI_CONTAINER;

import java.util.concurrent.ExecutionException;

import jade.wrapper.StaleProxyException;
import runner.service.MultiContainerScenarioService;
import runner.service.SingleContainerScenarioService;

/**
 * Main method which runs the engine and the given scenario
 */
public class EngineRunner {

	public static void main(String[] args) throws ExecutionException, InterruptedException, StaleProxyException {
		String scenario = "multipleServersScenario";

		if (MULTI_CONTAINER) {
			runMultiContainerService(scenario);
		} else {
			runSingleContainerService(scenario);
		}
	}

	public static void runSingleContainerService(String scenario)
			throws StaleProxyException, ExecutionException, InterruptedException {
		var scenarioService = new SingleContainerScenarioService(scenario);
		scenarioService.run();
	}

	public static void runMultiContainerService(String scenario)
			throws StaleProxyException, ExecutionException, InterruptedException {
		MultiContainerScenarioService scenarioService;
		if (MAIN_HOST) {
			scenarioService = new MultiContainerScenarioService(scenario);
		} else {
			scenarioService = new MultiContainerScenarioService(scenario, HOST_ID, HOST_NAME);
		}
		scenarioService.run();
	}
}
