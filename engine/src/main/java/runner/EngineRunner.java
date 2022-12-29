package runner;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.wrapper.StaleProxyException;
import runner.service.SingleContainerScenarioService;

/**
 * Main method which runs the engine on a single host and the given scenario
 */
public class EngineRunner extends AbstractEngineRunner {

	private static final Logger logger = LoggerFactory.getLogger(EngineRunner.class);

	public static void main(String[] args) throws InterruptedException {
		logger.info("Passed arguments: {}", Arrays.stream(args).toList());
		parseArguments(args, 0);
		runScenario(EngineRunner::runSingleContainerService);
	}

	public static void runSingleContainerService(String scenarioStructure, Optional<String> scenarioEvents) {
		try {
			var scenarioService = new SingleContainerScenarioService(scenarioStructure, scenarioEvents);
			scenarioService.run();
		} catch (StaleProxyException | ExecutionException | InterruptedException exception) {
			Thread.currentThread().interrupt();
			logger.error("Failed to run scenario due to exception {}", exception.getMessage());
		}

	}
}
