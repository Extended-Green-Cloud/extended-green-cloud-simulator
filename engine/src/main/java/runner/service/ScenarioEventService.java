package runner.service;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.greencloud.commons.enums.event.EventTypeEnum.CLIENT_CREATION_EVENT;
import static org.greencloud.commons.enums.event.EventTypeEnum.GREEN_SOURCE_CREATION_EVENT;
import static org.greencloud.commons.enums.event.EventTypeEnum.SERVER_CREATION_EVENT;
import static org.greencloud.commons.utils.event.EventSelector.getEventsForType;
import static org.greencloud.commons.utils.event.EventValidator.validateClientCreationEvents;
import static org.greencloud.commons.utils.event.EventValidator.validateGreenSourceEvents;
import static org.greencloud.commons.utils.event.EventValidator.validateServerEvents;
import static org.jrba.rulesengine.rest.RuleSetRestApi.getAvailableRuleSets;
import static org.jrba.utils.file.FileReader.readFile;
import static org.jrba.utils.mapper.JsonMapper.getMapper;
import static runner.configuration.ScenarioConfiguration.eventFilePath;
import static runner.constants.EngineConstants.POWER_SHORTAGE_EVENT_DELAY;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.event.DisableServerEventArgs;
import org.greencloud.commons.args.event.EnableServerEventArgs;
import org.greencloud.commons.args.event.EventArgs;
import org.greencloud.commons.args.event.ModifyRuleSetEventArgs;
import org.greencloud.commons.args.event.NewClientEventArgs;
import org.greencloud.commons.args.event.NewGreenSourceCreationEventArgs;
import org.greencloud.commons.args.event.NewServerCreationEventArgs;
import org.greencloud.commons.args.event.PowerShortageEventArgs;
import org.greencloud.commons.args.event.ServerMaintenanceEventArgs;
import org.greencloud.commons.args.event.WeatherDropEventArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.exception.InvalidScenarioException;
import org.greencloud.gui.event.DisableServerEvent;
import org.greencloud.gui.event.EnableServerEvent;
import org.greencloud.gui.event.PowerShortageEvent;
import org.greencloud.gui.event.ServerMaintenanceEvent;
import org.greencloud.gui.event.WeatherDropEvent;
import org.jrba.rulesengine.ruleset.RuleSet;
import org.jrba.rulesengine.ruleset.domain.ModifyAgentRuleSetEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.greencloud.connector.factory.AgentFactory;
import com.greencloud.connector.factory.AgentFactoryImpl;
import com.greencloud.connector.factory.EGCSControllerFactory;

import jade.wrapper.AgentController;

/**
 * Service containing method used to handle events defined for the given scenario
 */
public class ScenarioEventService {

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
			final List<EventArgs> scenarioEvents = parseScenarioEvents(scenarioEventsFile);
			validateScenarioStructure(scenarioEvents);
			scheduleScenarioEvents(scenarioEvents);
		}
	}

	private List<EventArgs> parseScenarioEvents(final File scenarioEventsFile) {
		try {
			return getMapper().readValue(scenarioEventsFile, new TypeReference<>() {
			});
		} catch (IOException e) {
			throw new InvalidScenarioException(
					format("Failed to parse scenario events file \"%s\"", scenarioEventsFile), e);
		}
	}

	private void validateScenarioStructure(final List<EventArgs> scenarioEvents) {
		final List<NewClientEventArgs> newClientEvents =
				getEventsForType(scenarioEvents, CLIENT_CREATION_EVENT, NewClientEventArgs.class);
		final List<NewServerCreationEventArgs> newServersEvents =
				getEventsForType(scenarioEvents, SERVER_CREATION_EVENT, NewServerCreationEventArgs.class);
		final List<NewGreenSourceCreationEventArgs> newGreenSourceEvents =
				getEventsForType(scenarioEvents, GREEN_SOURCE_CREATION_EVENT, NewGreenSourceCreationEventArgs.class);

		validateServerEvents(newServersEvents);
		validateGreenSourceEvents(newGreenSourceEvents);
		validateClientCreationEvents(newClientEvents);
	}

	private void scheduleScenarioEvents(final List<EventArgs> eventArgs) {
		logger.info("Scheduling scenario events...");
		final ScheduledExecutorService executor = newSingleThreadScheduledExecutor();
		eventArgs.forEach(event -> executor.schedule(() -> runEvent(event), event.getOccurrenceTime(), SECONDS));
		scenarioService.factory.shutdownAndAwaitTermination(executor, 1, HOURS);
	}

	private void runEvent(final EventArgs event) {
		switch (event.getType()) {
			case CLIENT_CREATION_EVENT -> runNewClientEvent(event);
			case SERVER_CREATION_EVENT -> runNewServerEvent((NewServerCreationEventArgs) event);
			case GREEN_SOURCE_CREATION_EVENT -> runNewGreenSourceEvent((NewGreenSourceCreationEventArgs) event);
			case POWER_SHORTAGE_EVENT -> triggerPowerShortage((PowerShortageEventArgs) event);
			case WEATHER_DROP_EVENT -> triggerWeatherDrop((WeatherDropEventArgs) event);
			case ENABLE_SERVER_EVENT -> enableServer((EnableServerEventArgs) event);
			case DISABLE_SERVER_EVENT -> disableServer((DisableServerEventArgs) event);
			case MODIFY_RULE_SET -> modifyRuleSet((ModifyRuleSetEventArgs) event);
			case SERVER_MAINTENANCE_EVENT -> modifyServerResources((ServerMaintenanceEventArgs) event);
		}
	}

	private void runNewGreenSourceEvent(final NewGreenSourceCreationEventArgs event) {
		scenarioService.guiController.createNewGreenSourceEvent(event.getName(),
				event.getServer(),
				event.getLatitude(),
				event.getLongitude(),
				event.getPricePerPowerUnit(),
				event.getWeatherPredictionError(),
				event.getMaximumCapacity(),
				event.getEnergyType());
	}

	private void runNewServerEvent(final NewServerCreationEventArgs event) {
		scenarioService.guiController.createNewServerEvent(event.getName(),
				event.getRegionalManager(),
				event.getMaxPower(),
				event.getIdlePower(),
				event.getResources(),
				event.getJobProcessingLimit(),
				event.getPrice());
	}

	private void runNewClientEvent(final EventArgs event) {
		final EGCSControllerFactory factory = scenarioService.factory;
		final NewClientEventArgs newClientEvent = (NewClientEventArgs) event;
		final ClientArgs clientAgentArgs = agentFactory.createClientAgent(newClientEvent);

		final AgentController agentController = factory.createAgentController(clientAgentArgs,
				(ScenarioStructureArgs) null);
		factory.runAgentController(agentController, 0);
	}

	private void triggerPowerShortage(final PowerShortageEventArgs event) {
		final Instant eventOccurrence = Instant.now().plusSeconds(POWER_SHORTAGE_EVENT_DELAY);
		final PowerShortageEvent eventData = new PowerShortageEvent(eventOccurrence,
				event.isFinished(),
				event.getCause(),
				event.getAgentName());
		scenarioService.guiController.triggerPowerShortageEvent(eventData);
	}

	private void triggerWeatherDrop(final WeatherDropEventArgs event) {
		final WeatherDropEvent eventData = new WeatherDropEvent(Instant.now(),
				event.getDuration(),
				event.getAgentName());
		scenarioService.guiController.triggerWeatherDropEvent(eventData);
	}

	private void disableServer(final DisableServerEventArgs event) {
		final DisableServerEvent eventData = new DisableServerEvent(Instant.now(), event.getName());
		scenarioService.guiController.disableServerEvent(eventData);
	}

	private void enableServer(final EnableServerEventArgs event) {
		final EnableServerEvent eventData = new EnableServerEvent(Instant.now(), event.getName());
		scenarioService.guiController.enableServerEvent(eventData);
	}

	private void modifyRuleSet(final ModifyRuleSetEventArgs event) {
		final RuleSet ruleSet = getAvailableRuleSets().get(event.getRuleSetName());
		final ModifyAgentRuleSetEvent eventData = new ModifyAgentRuleSetEvent(
				event.getFullReplace(), ruleSet, event.getAgentName());

		scenarioService.guiController.modifySystemRuleSetEvent(eventData);
	}

	private void modifyServerResources(final ServerMaintenanceEventArgs event) {
		final ServerMaintenanceEvent eventData =
				new ServerMaintenanceEvent(Instant.now(), event.getName(), event.getResources());
		scenarioService.guiController.modifyServerResources(eventData);
	}

}
