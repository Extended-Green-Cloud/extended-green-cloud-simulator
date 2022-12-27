package runner;

import static java.io.File.separator;
import static runner.service.domain.ScenarioConstants.HOST_ID;
import static runner.service.domain.ScenarioConstants.HOST_NAME;
import static runner.service.domain.ScenarioConstants.MAIN_HOST;
import static runner.service.domain.ScenarioConstants.MULTI_CONTAINER;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.wrapper.StaleProxyException;
import runner.service.MultiContainerScenarioService;
import runner.service.SingleContainerScenarioService;

/**
 * Main method which runs the engine and the given scenario
 */
public class EngineRunner {

	private static final Logger logger = LoggerFactory.getLogger(EngineRunner.class);

	private static String scenarioName = "multipleCNAsScenario";

	private static boolean verify = false;
	private static String adaptationToVerify = "change_green_source_weight";
	private static String verifyScenario = "singleServerMultipleGreenSourcesScenario";

	private static boolean events = false;
	private static String eventsScenario = "triggerChangeWeight";

	private static String defaultScenarioDirectory = "";
	private static String verifyScenarioDirectory = "adaptation" + separator + adaptationToVerify + separator;

	public static void main(String[] args) throws ExecutionException, InterruptedException, StaleProxyException {
		logger.info("Passed arguments: {}", Arrays.stream(args).toList());
		if (args.length == 2 && args[0].equals("run")) {
			scenarioName = args[1];
			logger.info("Running Green Cloud on scenario {}.", scenarioName);
		}

		if (args.length == 3 && args[0].equals("verify")) {
			verify = true;
			adaptationToVerify = args[1];
			verifyScenario = args[2];
			logger.info("Running Green Cloud adaptation {} verify on scenario {}.", adaptationToVerify, verifyScenario);
		}

		if (args.length == 4 && args[0].equals("verify+events")) {
			verify = true;
			events = true;
			adaptationToVerify = args[1];
			verifyScenario = args[2];
			eventsScenario = args[3];
			logger.info("Running Green Cloud adaptation {} verify on scenario {} with events {}.", adaptationToVerify,
					verifyScenario, events);
		}

		// wait for GUI to set up
		Thread.sleep(5000);

		String scenarioPath = verify ? verifyScenarioDirectory : defaultScenarioDirectory;
		String scenarioFilePath = scenarioPath + (verify ? verifyScenario : scenarioName);
		Optional<String> scenarioEvents = Optional.empty();

		if (events) {
			scenarioEvents = Optional.of(verifyScenarioDirectory + eventsScenario);
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
