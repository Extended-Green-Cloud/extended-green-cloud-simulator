package runner;

import static runner.configuration.EngineConfiguration.localHostIp;
import static runner.configuration.EngineConfiguration.readSystemProperties;
import static runner.configuration.ScenarioConfiguration.readScenarioProperties;
import static runner.constants.EngineConstants.GUI_SETUP_MILLISECONDS_DELAY;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
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

	public static void main(String[] args) throws InterruptedException {
		logger.info("Passed arguments: {}", Arrays.stream(args).toList());
		retrieveLocalHostIp();
		readSystemProperties();
		readScenarioProperties();

		// wait for GUI to set up
		Thread.sleep(GUI_SETUP_MILLISECONDS_DELAY);
		runMultiContainerService();
	}

	public static void runMultiContainerService() {
		try {
			final MultiContainerScenarioService scenarioService = new MultiContainerScenarioService();
			scenarioService.run();
		} catch (StaleProxyException | ExecutionException | InterruptedException exception) {
			Thread.currentThread().interrupt();
			logger.error("Failed to run scenario due to exception {}", exception.getMessage());
		}
	}

	private static void retrieveLocalHostIp() {
		try {
			localHostIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			logger.warn("Couldn't retrieve localhostIp");
		}
	}
}
