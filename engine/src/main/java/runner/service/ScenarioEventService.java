package runner.service;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.greencloud.commons.enums.event.EventTypeEnum.NEW_CLIENT_EVENT;
import static runner.configuration.ScenarioConfiguration.eventFilePath;
import static runner.constants.EngineConstants.POWER_SHORTAGE_EVENT_DELAY;
import static runner.utils.FileReader.readFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.event.EventArgs;
import org.greencloud.commons.args.event.NewClientEventArgs;
import org.greencloud.commons.args.event.PowerShortageEventArgs;
import org.greencloud.commons.args.scenario.ScenarioEventsArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.greencloud.commons.exception.InvalidScenarioException;
import org.greencloud.gui.event.PowerShortageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.greencloud.connector.factory.AgentControllerFactory;
import com.greencloud.connector.factory.AgentFactory;
import com.greencloud.connector.factory.AgentFactoryImpl;

import jade.wrapper.AgentController;

/**
 * Service containing method used to handle events defined for the given scenario
 */
public class ScenarioEventService {

	protected static final XmlMapper xmlMapper = new XmlMapper();
	private static final Logger logger = LoggerFactory.getLogger(ScenarioEventService.class);

	private final AbstractScenarioService scenarioService;
	private final AgentFactory agentFactory;

	/**
	 * Default constructor
	 *
	 * @param abstractScenarioService scenario service
	 */
	public ScenarioEventService(final AbstractScenarioService abstractScenarioService) {
		this.scenarioService = abstractScenarioService;
		this.agentFactory = new AgentFactoryImpl();
	}

	/**
	 * Method is responsible for reading and scheduling scenario events
	 */
	public void runScenarioEvents() {
		if (eventFilePath.isPresent()) {
			final File scenarioEventsFile = readFile(eventFilePath.get());
			final ScenarioEventsArgs scenarioEvents = parseScenarioEvents(scenarioEventsFile);
			validateScenarioStructure(scenarioEvents);
			scheduleScenarioEvents(scenarioEvents.getEventArgs());
		}
	}

	private ScenarioEventsArgs parseScenarioEvents(final File scenarioEventsFile) {
		try {
			return xmlMapper.readValue(scenarioEventsFile, ScenarioEventsArgs.class);
		} catch (IOException e) {
			throw new InvalidScenarioException(
					format("Failed to parse scenario events file \"%s\"", scenarioEventsFile), e);
		}
	}

	private void validateScenarioStructure(final ScenarioEventsArgs scenarioEvents) {
		final List<NewClientEventArgs> newClientEvents = scenarioEvents.getEventArgs().stream()
				.filter(eventArgs -> eventArgs.getType().equals(NEW_CLIENT_EVENT))
				.map(NewClientEventArgs.class::cast)
				.toList();
		validateClientDuplicates(newClientEvents);
	}

	private void validateClientDuplicates(final List<NewClientEventArgs> clientEventArgs) {
		final Set<String> clientNameSet = new HashSet<>();
		final Set<Integer> jobIdSet = new HashSet<>();

		clientEventArgs.forEach(client -> {
			if (!clientNameSet.add(client.getName())) {
				throw new InvalidScenarioEventStructure(
						String.format("Clients must have unique names. Duplicated client name: %s", client.getName()));
			}
			if (!jobIdSet.add(client.getJobId())) {
				throw new InvalidScenarioEventStructure(
						String.format("Specified job ids must be unique. Duplicated job id: %d", client.getJobId()));
			}
		});
	}

	private void scheduleScenarioEvents(final List<EventArgs> eventArgs) {
		logger.info("Scheduling scenario events...");
		final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();
		eventArgs.forEach(event -> executor.schedule(() -> runEvent(event), event.getOccurrenceTime(), SECONDS));
		scenarioService.factory.shutdownAndAwaitTermination(executor);
	}

	private void runEvent(final EventArgs event) {
		switch (event.getType()) {
			case NEW_CLIENT_EVENT -> runNewClientEvent(event);
			case POWER_SHORTAGE_EVENT -> triggerPowerShortage(event);
		}
	}

	private void runNewClientEvent(final EventArgs event) {
		final AgentControllerFactory factory = scenarioService.factory;
		final NewClientEventArgs newClientEvent = (NewClientEventArgs) event;
		final ClientArgs clientAgentArgs = agentFactory.createClientAgent(newClientEvent);

		final AgentController agentController = factory.createAgentController(clientAgentArgs,
				(ScenarioStructureArgs) null);
		factory.runAgentController(agentController, 0);
	}

	private void triggerPowerShortage(final EventArgs event) {
		final PowerShortageEventArgs powerShortageArgs = (PowerShortageEventArgs) event;
		final Instant eventOccurrence = Instant.now().plusSeconds(POWER_SHORTAGE_EVENT_DELAY);
		final PowerShortageEvent eventData = new PowerShortageEvent(eventOccurrence, powerShortageArgs.isFinished(),
				powerShortageArgs.getCause(), powerShortageArgs.getAgentName());
		scenarioService.guiController.triggerPowerShortageEvent(eventData);
	}
}
