package runner;

import static java.io.File.separator;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEngineRunner {

	private static final Logger logger = LoggerFactory.getLogger(AbstractEngineRunner.class);

	private static final Integer GUI_SETUP_MILLISECONDS_DELAY = 5000;

	protected static String scenarioName = "complicatedScenarioNoWeatherChanging";

	protected static boolean verify = false;
	protected static String adaptationToVerify = "disable_server";
	protected static String verifyScenario = "disableServerScenario";

	protected static boolean events = false;
	protected static String eventsScenario = "disableServerEvents";

	protected static String defaultScenarioDirectory = "";
	protected static String verifyScenarioDirectory = "adaptation" + separator + adaptationToVerify + separator;

	protected static void parseArguments(String[] args, int startingIndex) {
		if (args.length == 2 + startingIndex && args[0].equals("run")) {
			scenarioName = args[1];
			logger.info("Running Green Cloud on scenario {}.", scenarioName);
		}

		if (args.length == 3 + startingIndex && args[0].equals("verify")) {
			verify = true;
			adaptationToVerify = args[1];
			verifyScenario = args[2];
			logger.info("Running Green Cloud adaptation {} verify on scenario {}.", adaptationToVerify, verifyScenario);
		}

		if (args.length == 4 + startingIndex && args[0].equals("verify+events")) {
			verify = true;
			events = true;
			adaptationToVerify = args[1];
			verifyScenario = args[2];
			eventsScenario = args[3];
			logger.info("Running Green Cloud adaptation {} verify on scenario {} with events {}.", adaptationToVerify,
					verifyScenario, events);
		}
	}

	protected static void runScenario(BiConsumer<String, Optional<String>> scenarioServiceToRun)
			throws InterruptedException {
		// wait for GUI to set up
		Thread.sleep(GUI_SETUP_MILLISECONDS_DELAY);

		String scenarioPath = verify ? verifyScenarioDirectory : defaultScenarioDirectory;
		String scenarioFilePath = scenarioPath + (verify ? verifyScenario : scenarioName);
		Optional<String> scenarioEvents = Optional.empty();

		if (events) {
			scenarioEvents = Optional.of(verifyScenarioDirectory + eventsScenario);
		}

		scenarioServiceToRun.accept(scenarioFilePath, scenarioEvents);
	}
}
