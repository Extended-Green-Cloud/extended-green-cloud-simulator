package runner.service;

import static com.greencloud.commons.args.agent.client.ClientTimeType.SIMULATION;
import static com.greencloud.commons.args.event.EventTypeEnum.NEW_CLIENT_EVENT;
import static java.util.concurrent.TimeUnit.SECONDS;
import static runner.constants.EngineConstants.POWER_SHORTAGE_EVENT_DELAY;
import static runner.domain.ScenarioConfiguration.eventFilePath;

import java.io.File;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.commons.args.agent.client.ClientAgentArgs;
import com.greencloud.commons.args.agent.client.ImmutableClientAgentArgs;
import com.greencloud.commons.args.event.EventArgs;
import com.greencloud.commons.args.event.newclient.NewClientEventArgs;
import com.greencloud.commons.args.event.powershortage.PowerShortageEventArgs;
import com.greencloud.commons.exception.InvalidScenarioEventStructure;
import com.greencloud.commons.scenario.ScenarioEventsArgs;
import com.gui.event.domain.PowerShortageEvent;

import jade.wrapper.AgentController;
import runner.factory.AgentControllerFactory;

/**
 * Service containing method used to handle events defined for the given scenario
 */
public class ScenarioEventService {

	private static final Logger logger = LoggerFactory.getLogger(ScenarioEventService.class);

	private final AbstractScenarioService scenarioService;

	/**
	 * Default constructor
	 *
	 * @param abstractScenarioService scenario service
	 */
	public ScenarioEventService(final AbstractScenarioService abstractScenarioService) {
		this.scenarioService = abstractScenarioService;
	}

	/**
	 * Method is responsible for reading and scheduling scenario events
	 *
	 * @param factory agent controller specified for events that require agents creation
	 */
	public void runScenarioEvents(final AgentControllerFactory factory) {
		if (eventFilePath.isPresent()) {
			final File scenarioEventsFile = scenarioService.readFile(eventFilePath.get());
			final ScenarioEventsArgs scenarioEvents = scenarioService.parseScenarioEvents(scenarioEventsFile);
			validateScenarioStructure(scenarioEvents);
			scheduleScenarioEvents(scenarioEvents.getEventArgs(), factory);
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

	private void scheduleScenarioEvents(final List<EventArgs> eventArgs, final AgentControllerFactory factory) {
		logger.info("Scheduling scenario events...");
		final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		eventArgs.forEach(event -> scheduledExecutor.schedule(() -> runEvent(event, factory), event.getOccurrenceTime(),
				SECONDS));
		scenarioService.shutdownAndAwaitTermination(scheduledExecutor);
	}

	private void runEvent(final EventArgs event, final AgentControllerFactory factory) {
		switch (event.getType()) {
			case NEW_CLIENT_EVENT -> runNewClientEvent(event, factory);
			case POWER_SHORTAGE_EVENT -> triggerPowerShortage(event);
		}
	}

	private void runNewClientEvent(final EventArgs event, final AgentControllerFactory factory) {
		final NewClientEventArgs newClientEvent = (NewClientEventArgs) event;
		final ClientAgentArgs clientAgentArgs = ImmutableClientAgentArgs.builder()
				.name(newClientEvent.getName())
				.jobId(String.valueOf(newClientEvent.getJobId()))
				.power(String.valueOf(newClientEvent.getPower()))
				.start(String.valueOf(newClientEvent.getStart()))
				.end(String.valueOf(newClientEvent.getEnd()))
				.deadline(String.valueOf(newClientEvent.getDeadline()))
				.timeType(SIMULATION)
				.build();
		final AgentController agentController = scenarioService.runAgentController(clientAgentArgs, null, factory);
		scenarioService.runAgent(agentController, 0);
	}

	private void triggerPowerShortage(final EventArgs event) {
		final PowerShortageEventArgs powerShortageArgs = (PowerShortageEventArgs) event;
		final Instant eventOccurrence = Instant.now().plusSeconds(POWER_SHORTAGE_EVENT_DELAY);
		final PowerShortageEvent eventData = new PowerShortageEvent(eventOccurrence,
				powerShortageArgs.getNewMaximumCapacity(), powerShortageArgs.isFinished(),
				powerShortageArgs.getCause());
		scenarioService.guiController.triggerPowerShortageEvent(eventData, powerShortageArgs.getAgentName());
	}
}
