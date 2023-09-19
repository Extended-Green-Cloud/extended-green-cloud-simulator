package runner;

import static org.greencloud.rulescontroller.rest.StrategyRestApi.startRulesControllerRest;
import static runner.configuration.EngineConfiguration.readSystemProperties;
import static runner.configuration.ScenarioConfiguration.readScenarioProperties;
import static runner.constants.EngineConstants.GUI_SETUP_MILLISECONDS_DELAY;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.wrapper.StaleProxyException;
import runner.service.SingleContainerScenarioService;

/**
 * Main method which runs the engine on a single host and the given scenario
 */
public class EngineRunner {

	private static final Logger logger = LoggerFactory.getLogger(EngineRunner.class);

	public static void main(String[] args) throws InterruptedException {
		logger.info("Passed arguments: {}", Arrays.stream(args).toList());
		readSystemProperties();
		readScenarioProperties();

		// wait for GUI to set up
		Thread.sleep(GUI_SETUP_MILLISECONDS_DELAY);
		startRulesControllerRest();
		runSingleContainerService();
	}

	public static void runSingleContainerService() {
		try {
			var scenarioService = new SingleContainerScenarioService();
			scenarioService.run();
		} catch (StaleProxyException | ExecutionException | InterruptedException exception) {
			Thread.currentThread().interrupt();
			logger.error("Failed to run scenario due to exception {}", exception.getMessage());
		}

	}
}
