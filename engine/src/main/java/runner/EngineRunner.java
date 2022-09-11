package runner;

import java.util.concurrent.ExecutionException;

import jade.wrapper.StaleProxyException;
import runner.service.MultiContainerScenarioService;
import runner.service.SingleContainerScenarioService;

/**
 * Main method which runs the engine and the given scenario
 */
public class EngineRunner {

	public static void main(String[] args) throws ExecutionException, InterruptedException, StaleProxyException {
		String scenario = "complicatedScenarioNoWeatherChanging";

		//new SingleContainerScenarioService(scenario).run();
		new MultiContainerScenarioService(scenario).run();
	}
}
