package runner;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static runner.domain.EngineConstants.databaseHostIp;
import static runner.domain.EngineConstants.hostId;
import static runner.domain.EngineConstants.localHostIp;
import static runner.domain.EngineConstants.mainHost;
import static runner.domain.EngineConstants.mainHostIp;
import static runner.domain.EngineConstants.websocketHostIp;

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
public class MultiEngineRunner extends AbstractEngineRunner {

	private static final Logger logger = LoggerFactory.getLogger(MultiEngineRunner.class);

	public static void main(String[] args) throws InterruptedException {
		logger.info("Passed arguments: {}", Arrays.stream(args).toList());
		String[] multiHostArguments = args[0].split("_");
		parseMultiHostArgs(multiHostArguments);
		parseArguments(args, 1);
		runScenario(MultiEngineRunner::runMultiContainerService);
	}

	public static void runMultiContainerService(String scenarioStructure, Optional<String> scenarioEvents) {
		MultiContainerScenarioService scenarioService;
		if (mainHost) {
			try {
				scenarioService = new MultiContainerScenarioService(scenarioStructure);
				scenarioService.run();
			} catch (StaleProxyException | ExecutionException | InterruptedException exception) {
				Thread.currentThread().interrupt();
				logger.error("Failed to run scenario due to exception {}", exception.getMessage());
			}
		} else {
			scenarioService = new MultiContainerScenarioService(scenarioStructure, scenarioEvents, hostId, mainHostIp);
			scenarioService.run();
		}
	}

	private static void parseMultiHostArgs(String[] multiHostArgs) {
		if (multiHostArgs.length != 6) {
			throw new IllegalStateException("Can't run multi container Green Cloud without required arguments");
		}

		for (String arg : multiHostArgs) {
			String[] argKeyValue = arg.split("=");
			switch (argKeyValue[0]) {
				case "mainHost" -> mainHost = parseBoolean(argKeyValue[1]);
				case "hostId" -> hostId = parseInt(argKeyValue[1]);
				case "localHostIp" -> localHostIp = argKeyValue[1];
				case "mainHostIp" -> mainHostIp = argKeyValue[1];
				case "databaseIp" -> databaseHostIp = argKeyValue[1];
				case "websocketIp" -> websocketHostIp = argKeyValue[1];
				default -> throw new IllegalStateException("Provided unsupported parameter key!");
			}
		}
	}
}
