package org.greencloud.managingsystem.service.mobility;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.exception.MapperException;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.managingsystem.agents.ManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.greencloud.managingsystem.service.executor.ExecutorService;
import org.jetbrains.annotations.Nullable;
import org.jrba.agentmodel.domain.args.AgentArgs;

import com.database.knowledge.domain.agent.AMSData;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Location;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Service containing methods responsible for moving agents between containers
 */
@SuppressWarnings("unchecked")
public class MobilityService extends AbstractManagingService {

	private static final LogBuilder log = constructLogBuilder(MobilityService.class);

	private final Map<Location, AID> containersLocations;

	public MobilityService(ManagingAgent managingAgent) {
		super(managingAgent);
		this.containersLocations = findContainersLocations();
	}

	/**
	 * Method updates the cloud network structure by extending it with selected agents
	 *
	 * @param agentArgs - list of agents that are to be added to the cloud network structure
	 */
	public void addAgentsToStructure(final List<AgentArgs> agentArgs) {
		final ScenarioStructureArgs systemStructure = managingAgent.getProperties().getGreenCloudStructure();

		agentArgs.forEach(args -> {
			switch (args) {
				case ServerArgs serverArgs -> systemStructure.getServerAgentsArgs().add(serverArgs);
				case GreenEnergyArgs greenEnergyArgs -> systemStructure.getGreenEnergyAgentsArgs().add(greenEnergyArgs);
				case MonitoringArgs monitoringArgs -> systemStructure.getMonitoringAgentsArgs().add(monitoringArgs);
				default -> log.messageDebug("Unsupported agent type: {} for mobility.", args.getClass().getName());
			}
		});
	}

	/**
	 * Method retrieved the location of agent container which corresponds to the given Regional Manager Agent
	 *
	 * @param candidateRegionalManager - name of the regional manager agent of interest
	 * @return location of the container
	 */
	public Map.Entry<Location, AID> findTargetLocation(final String candidateRegionalManager) {
		var regionalManagerContainer = getContainerLocations(candidateRegionalManager);
		return isNull(regionalManagerContainer)
				? getContainerLocations("Main-Container")
				: regionalManagerContainer;
	}

	/**
	 * Method returns a container with a given name
	 *
	 * @param containerName name of the container that is to be returned
	 * @return Location of a container with given name
	 */
	public Map.Entry<Location, AID> getContainerLocations(final String containerName) {
		return containersLocations.entrySet().stream()
				.filter(location -> location.getKey().getName().contains(containerName))
				.findFirst()
				.orElseGet(() -> {
					log.messageWarn("Couldn't find target container for a new server!");
					return null;
				});
	}

	/**
	 * Method moves given agents to specific container
	 *
	 * @param targetContainer container to which the agents are going to be moved
	 * @param createdAgents   agents that are going to be moved
	 */
	public void moveContainers(final Location targetContainer, final List<AgentController> createdAgents) {
		if (!targetContainer.getName().equals("Main-Container")) {
			createdAgents.forEach(agentController -> moveAgentController(agentController, targetContainer));
		}
	}

	private void moveAgentController(AgentController agentController, Location targetContainer) {
		try {
			log.messageInfo("Moving {} agent controller to container {}",
					agentController.getName(), targetContainer.getName());
			agentController.move(targetContainer);
		} catch (StaleProxyException e) {
			throw new MapperException(e);
		}
	}

	private Map<Location, AID> findContainersLocations() {
		final List<AMSData> amsAgents = getAMSData();

		if (amsAgents.isEmpty()) {
			return findContainerLocally();
		}

		final Map<Location, AID> map = amsAgents.stream()
				.map(amsAgent -> {
					final AID ams = new AID(amsAgent.name(), AID.ISGUID);
					ams.addAddresses(amsAgent.address());
					prepareAndSendPlatformLocationsQuery(ams);
					final Result result = receiveLocationsResponseFromAms(ams);

					return new AbstractMap.SimpleEntry<>(getLocationsFromQueryResult(result), ams);
				})
				.flatMap(entry -> entry.getKey().stream()
						.map(location -> new AbstractMap.SimpleEntry<>(location, entry.getValue())))
				.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

		final String locationsString = map.keySet().stream().map(Location::getName).collect(joining(", "));
		log.messageInfo("Found {} containers: {}.", map.size(), locationsString);

		return map;
	}

	private Map<Location, AID> findContainerLocally() {
		final AID ams = new AID(managingAgent.getAMS().getName(), AID.ISGUID);
		prepareAndSendPlatformLocationsQuery(ams);
		final Result result = receiveLocationsResponseFromAms(ams);
		final List<Location> locations = getLocationsFromQueryResult(result);

		final String locationsString = locations.stream().map(Location::getName).collect(joining(", "));
		log.messageInfo("Found {} containers: {}.", locations.size(), locationsString);

		return locations.stream().collect(toMap(location -> location, _ -> ams));
	}

	private void prepareAndSendPlatformLocationsQuery(final AID ams) {
		final Action queryLocations = new Action(ams, new QueryPlatformLocationsAction());
		final ACLMessage locationsRequest = new ACLMessage(REQUEST);
		locationsRequest.setLanguage(new SLCodec().getName());
		locationsRequest.setOntology(MobilityOntology.getInstance().getName());

		try {
			managingAgent.getContentManager().fillContent(locationsRequest, queryLocations);
			locationsRequest.addReceiver(queryLocations.getActor());
			managingAgent.send(locationsRequest);
		} catch (Exception exception) {
			log.messageError("Failed to send location query to AMS: {}", exception.getMessage());
		}
	}

	@Nullable
	private Result receiveLocationsResponseFromAms(final AID ams) {
		final ACLMessage response = managingAgent.blockingReceive(
				and(MatchSender(ams), MatchPerformative(INFORM)));
		try {
			return (Result) managingAgent.getContentManager().extractContent(response);
		} catch (Exception exception) {
			log.messageError("Failed to extract content from AMS response: {}", exception.getMessage());
			return null;
		}
	}

	private List<AMSData> getAMSData() {
		if (nonNull(managingAgent.getAgentNode())) {
			return managingAgent.getAgentNode().getDatabaseClient().readAMSData();
		}
		return emptyList();
	}

	private List<Location> getLocationsFromQueryResult(@Nullable Result result) {
		if (isNull(result)) {
			return emptyList();
		}
		final List<Location> locations = new ArrayList<>();
		final jade.util.leap.Iterator it = result.getItems().iterator();

		it.forEachRemaining(location -> locations.add((Location) location));
		return locations;
	}
}
