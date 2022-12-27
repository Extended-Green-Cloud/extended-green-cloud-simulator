package runner;

import static java.io.File.separator;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static runner.EngineConstants.databaseHostName;
import static runner.EngineConstants.hostId;
import static runner.EngineConstants.hostName;
import static runner.EngineConstants.mainHost;
import static runner.EngineConstants.websocketHostName;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.wrapper.StaleProxyException;
import runner.service.MultiContainerScenarioService;

/**
 * Main method which runs the engine on a multiple hosts and the given scenario
 */
public class MultiEngineRunner {

	private static final Logger logger = LoggerFactory.getLogger(MultiEngineRunner.class);

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
		String[] multiHostArguments;

		if (args.length == 1) {
			multiHostArguments = args[0].split(";");
			parseMultiHostArgs(multiHostArguments);
		}

		if (args.length == 3 && args[0].equals("run")) {
			scenarioName = args[1];
			logger.info("Running Green Cloud on scenario {}.", scenarioName);
		}

		if (args.length == 4 && args[0].equals("verify")) {
			verify = true;
			adaptationToVerify = args[1];
			verifyScenario = args[2];
			logger.info("Running Green Cloud adaptation {} verify on scenario {}.", adaptationToVerify, verifyScenario);
		}

		if (args.length == 5 && args[0].equals("verify+events")) {
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

		runMultiContainerService(scenarioFilePath, scenarioEvents);
	}

	public static void runMultiContainerService(String scenarioStructure, Optional<String> scenarioEvents)
			throws StaleProxyException, ExecutionException, InterruptedException {
		MultiContainerScenarioService scenarioService;
		if (mainHost) {
			scenarioService = new MultiContainerScenarioService(scenarioStructure);
		} else {
			scenarioService = new MultiContainerScenarioService(scenarioStructure, scenarioEvents, hostId, hostName);
		}
		scenarioService.run();
	}

	private static void parseMultiHostArgs(String[] multiHostArgs) {
		if (multiHostArgs.length != 5) {
			throw new IllegalStateException("Can't run multi container Green Cloud without required arguments");
		}

		mainHost = parseBoolean(multiHostArgs[0]);
		hostId = parseInt(multiHostArgs[1]);
		hostName = multiHostArgs[2];
		databaseHostName = multiHostArgs[3];
		websocketHostName = multiHostArgs[4];
	}
}
