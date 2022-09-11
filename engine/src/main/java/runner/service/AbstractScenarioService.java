package runner.service;

import static jade.core.Runtime.instance;
import static java.util.concurrent.TimeUnit.SECONDS;
import static runner.service.domain.ScenarioConstants.END_TIME_MAX;
import static runner.service.domain.ScenarioConstants.MAX_JOB_POWER;
import static runner.service.domain.ScenarioConstants.MIN_JOB_POWER;
import static runner.service.domain.ScenarioConstants.RESOURCE_SCENARIO_PATH;
import static runner.service.domain.ScenarioConstants.START_TIME_MAX;
import static runner.service.domain.ScenarioConstants.START_TIME_MIN;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gui.controller.GUIController;
import com.gui.controller.GUIControllerImpl;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import runner.domain.AgentArgs;
import runner.domain.ClientAgentArgs;
import runner.domain.ImmutableClientAgentArgs;
import runner.domain.ScenarioArgs;
import runner.factory.AgentControllerFactory;

public abstract class AbstractScenarioService {

	private static final Logger logger = LoggerFactory.getLogger(AbstractScenarioService.class);

	private static final Long GRAPH_INITIALIZATION_PAUSE = 7L;

	protected static final XmlMapper xmlMapper = new XmlMapper();
	protected static final ExecutorService executorService = Executors.newCachedThreadPool();

	protected final GUIController guiController;
	protected final String fileName;
	protected final Runtime jadeRuntime;
	protected final ContainerController mainContainer;

	protected AbstractScenarioService(String fileName)
			throws ExecutionException, InterruptedException, StaleProxyException {
		this.guiController = new GUIControllerImpl();
		this.fileName = fileName;
		this.jadeRuntime = instance();

		executorService.execute(guiController);
		mainContainer = runMainController();
		runJadeGui();
	}

	protected File readFile(final String fileName) {
		URL resource = getClass().getClassLoader().getResource(RESOURCE_SCENARIO_PATH + fileName + ".xml");
		try {
			return new File(resource.toURI());
		} catch (URISyntaxException | NullPointerException e) {
			logger.error("Invalid scenario name.");
			throw new RuntimeException();
		}
	}

	protected ScenarioArgs parseScenario(File scenarioFile) {
		try {
			return xmlMapper.readValue(scenarioFile, ScenarioArgs.class);
		} catch (IOException e) {
			logger.error("Failed to parse scenario file, {}.", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	protected AgentController runAgentController(AgentArgs args, ScenarioArgs scenario,
			AgentControllerFactory factory) {
		final AgentController agentController;
		try {
			agentController = factory.createAgentController(args);
			var agentNode = factory.createAgentNode(args, scenario);
			guiController.addAgentNodeToGraph(agentNode);
			agentController.putO2AObject(guiController, AgentController.ASYNC);
			agentController.putO2AObject(agentNode, AgentController.ASYNC);
		} catch (StaleProxyException e) {
			logger.error("Failed to run agent controller with exception {}.", e.getMessage());
			throw new RuntimeException(e);
		}
		return agentController;
	}

	protected void runClientAgents(long agentsNumber, ScenarioArgs scenario, AgentControllerFactory factory) {
		var random = ThreadLocalRandom.current();
		LongStream.rangeClosed(1, agentsNumber).forEach(idx -> {
			final int randomPower = MIN_JOB_POWER + random.nextInt(MAX_JOB_POWER);
			final int randomStart = START_TIME_MIN + random.nextInt(START_TIME_MAX);
			final int randomEnd = randomStart + 1 + random.nextInt(END_TIME_MAX);
			final ClientAgentArgs clientAgentArgs =
					ImmutableClientAgentArgs.builder()
							.name(String.format("Client%d", idx))
							.jobId(String.valueOf(idx))
							.power(String.valueOf(randomPower))
							.start(String.valueOf(randomStart))
							.end(String.valueOf(randomEnd))
							.build();
			final AgentController agentController = runAgentController(clientAgentArgs, scenario, factory);
			try {
				agentController.start();
				agentController.activate();
				TimeUnit.MILLISECONDS.sleep(250);
			} catch (StaleProxyException | InterruptedException e) {
				logger.error("Failed to run client agent controller with exception {}.", e.getMessage());
				throw new RuntimeException(e);
			}
		});
	}

	protected void runAgents(List<AgentController> controllers) {
		var scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.schedule(() -> controllers.forEach(this::runAgent), GRAPH_INITIALIZATION_PAUSE, SECONDS);
		shutdownAndAwaitTermination(scheduledExecutor);
	}

	private void shutdownAndAwaitTermination(ExecutorService executorService) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException ie) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	private void runAgent(AgentController controller) {
		try {
			controller.start();
			controller.activate();
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (StaleProxyException | InterruptedException e) {
			logger.error("Failed to run agent controller with exception {}.", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private ContainerController runMainController() throws ExecutionException, InterruptedException {
		final Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, "Main-Container");
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		profile.setParameter(Profile.MAIN_PORT, "6996");
		return executorService.submit(() -> jadeRuntime.createMainContainer(profile)).get();
	}

	private void runJadeGui() throws StaleProxyException {
		final AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
		rma.start();
	}
}
