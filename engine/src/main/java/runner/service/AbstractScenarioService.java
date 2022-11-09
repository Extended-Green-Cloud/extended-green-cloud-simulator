package runner.service;

import static jade.core.Runtime.instance;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static runner.service.domain.ContainerTypeEnum.CLIENTS_CONTAINER_ID;
import static runner.service.domain.ScenarioConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.client.ClientAgentArgs;
import com.greencloud.commons.args.agent.client.ImmutableClientAgentArgs;
import com.gui.controller.GuiController;
import com.gui.controller.GuiControllerImpl;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import runner.domain.ScenarioEventsArgs;
import runner.domain.ScenarioStructureArgs;
import runner.factory.AgentControllerFactory;
import runner.service.domain.exception.InvalidScenarioException;
import runner.service.domain.exception.JadeContainerException;
import runner.service.domain.exception.JadeControllerException;

/**
 * Abstract class serving as common base to Single and Multi Scenario Services.
 * It handles creation of Main and Agent's Containers as well as Agent's Controllers.
 * It is also responsible for running Agent's and Agent's clients.
 */
public abstract class AbstractScenarioService {

	private static final Logger logger = LoggerFactory.getLogger(AbstractScenarioService.class);

	private static final Long GRAPH_INITIALIZATION_PAUSE = 7L;
	private static final Integer RUN_CLIENT_AGENT_PAUSE = 150;
	private static final Integer RUN_AGENT_PAUSE = 100;

	protected static final XmlMapper xmlMapper = new XmlMapper();
	protected static final ExecutorService executorService = Executors.newCachedThreadPool();
	protected final ScenarioEventService eventService;

	protected final GuiController guiController;
	protected final String scenarioStructureFileName;
	protected final String scenarioEventsFileName;
	protected final Runtime jadeRuntime;
	protected final ContainerController mainContainer;

	/**
	 * Constructor called by {@link MultiContainerScenarioService} and {@link SingleContainerScenarioService}
	 * Launches gui and the main controller. In case of MultiContainer case runs environment only for the main host.
	 *
	 * @param scenarioStructureFileName name of the XML scenario document containing network structure
	 * @param scenarioEventsFileName    (optional) name of the XML scenario document containing list of events triggered during scenario execution
	 */
	protected AbstractScenarioService(String scenarioStructureFileName, Optional<String> scenarioEventsFileName)
			throws ExecutionException, InterruptedException, StaleProxyException {
		this.guiController = new GuiControllerImpl("ws://localhost:8080/");
		this.eventService = new ScenarioEventService(this);
		this.scenarioStructureFileName = scenarioStructureFileName;
		this.scenarioEventsFileName = scenarioEventsFileName.orElse(null);
		this.jadeRuntime = instance();

		executorService.execute(guiController);
		mainContainer = runMainController();
		runJadeGui();
	}

	/**
	 * Runs remote AgentContainer with GUI.
	 *
	 * @param scenarioStructureFileName name of the XML scenario document
	 * @param hostId                    number of the host id
	 * @param mainHostIp                IP address of the main host
	 */
	protected AbstractScenarioService(String scenarioStructureFileName, Integer hostId, String mainHostIp,
			Optional<String> scenarioEventsFileName) {
		this.guiController = new GuiControllerImpl(format("ws://%s:8080/", mainHostIp));
		this.eventService = new ScenarioEventService(this);
		this.scenarioStructureFileName = scenarioStructureFileName;
		this.scenarioEventsFileName = scenarioEventsFileName.orElse(null);
		this.jadeRuntime = instance();

		executorService.execute(guiController);
		mainContainer = runAgentsContainer(hostId.toString(), mainHostIp);
	}

	protected File readFile(final String fileName) {
		URL resource = getClass().getClassLoader().getResource(RESOURCE_SCENARIO_PATH + fileName + ".xml");
		try {
			return new File(resource.toURI());
		} catch (URISyntaxException | NullPointerException e) {
			throw new InvalidScenarioException("Invalid scenario file name.", e);
		}
	}

	protected ScenarioStructureArgs parseScenarioStructure(File scenarioStructureFile) {
		try {
			return xmlMapper.readValue(scenarioStructureFile, ScenarioStructureArgs.class);
		} catch (IOException e) {
			throw new InvalidScenarioException(format("Failed to parse scenario structure file \"%s\".xml.",
					scenarioStructureFileName), e);
		}
	}

	protected ScenarioEventsArgs parseScenarioEvents(File scenarioEventsFile) {
		try {
			return xmlMapper.readValue(scenarioEventsFile, ScenarioEventsArgs.class);
		} catch (IOException e) {
			throw new InvalidScenarioException(format("Failed to parse scenario events file \"%s\".xml.",
					scenarioEventsFileName), e);
		}
	}

	protected AgentController runAgentController(AgentArgs args, ScenarioStructureArgs scenario,
			AgentControllerFactory factory) {
		final AgentController agentController;
		try {
			agentController = factory.createAgentController(args);
			var agentNode = factory.createAgentNode(args, scenario);
			guiController.addAgentNodeToGraph(agentNode);
			agentController.putO2AObject(guiController, AgentController.ASYNC);
			agentController.putO2AObject(agentNode, AgentController.ASYNC);
			logger.info("Created {} agent.", args.getName());
		} catch (StaleProxyException e) {
			throw new JadeControllerException("Failed to run agent controller", e);
		}
		return agentController;
	}

	protected void runClientAgents(long agentsNumber, AgentControllerFactory factory) {
		var random = ThreadLocalRandom.current();
		LongStream.rangeClosed(1, agentsNumber).forEach(idx -> {
			final int randomPower = MIN_JOB_POWER + random.nextInt(MAX_JOB_POWER);
			final int randomStart = START_TIME_MIN + random.nextInt(START_TIME_MAX);
			final int randomEnd = randomStart + 1 + random.nextInt(END_TIME_MAX);
			final int randomDeadline = randomEnd + 3 + random.nextInt(DEADLINE_MAX);
			final ClientAgentArgs clientAgentArgs = ImmutableClientAgentArgs.builder()
					.name(format("Client%d", idx))
					.jobId(String.valueOf(idx))
					.power(String.valueOf(randomPower))
					.start(String.valueOf(randomStart))
					.end(String.valueOf(randomEnd))
					.deadline(String.valueOf(randomDeadline))
					.build();
			final AgentController agentController = runAgentController(clientAgentArgs, null, factory);
			runAgent(agentController, RUN_CLIENT_AGENT_PAUSE);
		});
	}

	protected void runAgents(List<AgentController> controllers) {
		var scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.schedule(() -> controllers.forEach(controller -> runAgent(controller, RUN_AGENT_PAUSE)),
				GRAPH_INITIALIZATION_PAUSE, SECONDS);
		shutdownAndAwaitTermination(scheduledExecutor);
	}

	protected void runAgent(AgentController controller, long pause) {
		try {
			controller.start();
			controller.activate();
			TimeUnit.MILLISECONDS.sleep(pause);
		} catch (StaleProxyException | InterruptedException e) {
			throw new JadeControllerException("Failed to run agent controller", e);
		}
	}

	protected void shutdownAndAwaitTermination(ExecutorService executorService) {
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

	private ContainerController runMainController() throws ExecutionException, InterruptedException {
		final Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, "Main-Container");
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		profile.setParameter(Profile.MAIN_PORT, "6996");
		return executorService.submit(() -> jadeRuntime.createMainContainer(profile)).get();
	}

	protected ContainerController runAgentsContainer(String containerName, String host) {
		var profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, "6996");
		try {
			return executorService.submit(() -> jadeRuntime.createAgentContainer(profile)).get();
		} catch (InterruptedException | ExecutionException e) {
			if (containerName.equals(CLIENTS_CONTAINER_ID.toString())) {
				throw new JadeContainerException("Failed to create Agent Clients container", e);
			}
			throw new JadeContainerException("Failed to create CloudNetwork container", e);
		}
	}

	private void runJadeGui() throws StaleProxyException {
		final AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
		final AgentController sniffer = mainContainer.createNewAgent("sniffeur", "jade.tools.sniffer.Sniffer",
				new Object[0]);
		rma.start();
		sniffer.start();
	}
}
