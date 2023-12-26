package org.greencloud.managingsystem.service.mobility;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.service.mobility.logs.ManagingAgentMobilityLog.FOUND_CONTAINERS_LOG;
import static org.greencloud.managingsystem.service.mobility.logs.ManagingAgentMobilityLog.MOVE_CONTAINER_LOG;
import static org.greencloud.managingsystem.service.mobility.logs.ManagingAgentMobilityLog.NO_LOCATION_LOG;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.exception.MapperException;
import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Service containing methods connected with modification of agent structure,
 * particularly moving agents between containers
 */
public class MobilityService extends AbstractManagingService {

	private static final Logger logger = LoggerFactory.getLogger(MobilityService.class);

	public MobilityService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	/**
	 * Method updates the cloud network structure by extending it with selected agents
	 *
	 * @param agentArgs - list of agents that are to be added to the cloud network structure
	 */
	public void addAgentsToStructure(final List<AgentArgs> agentArgs) {
		for (AgentArgs args : agentArgs) {
			if (args instanceof ServerArgs serverAgentArgs) {
				managingAgent.getGreenCloudStructure().getServerAgentsArgs().add(serverAgentArgs);
			}
			if (args instanceof GreenEnergyArgs greenEnergyAgentArgs) {
				managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().add(greenEnergyAgentArgs);
			}
			if (args instanceof MonitoringArgs monitoringAgentArgs) {
				managingAgent.getGreenCloudStructure().getMonitoringAgentsArgs().add(monitoringAgentArgs);
			}
		}
	}

	/**
	 * Method returns a container with a given name
	 *
	 * @param containerName name of the container that is to be returned
	 * @return Location of a container with given name
	 */
	public Map.Entry<Location, AID> getContainerLocations(final String containerName) {
		return managingAgent.getContainersLocations().entrySet().stream()
				.filter(location -> location.getKey().getName().contains(containerName))
				.findFirst()
				.orElseGet(() -> {
					logger.warn(NO_LOCATION_LOG);
					return null;
				});
	}

	/**
	 * Method retrieved the location of agent container which corresponds to the given Regional Manager Agent
	 *
	 * @param candidateRegionalManager - name of the regional manager agent of interest
	 * @return location of the container
	 */
	public Map.Entry<Location, AID> findTargetLocation(final String candidateRegionalManager) {
		if (managingAgent.getContainersLocations() == null) {
			managingAgent.setContainersLocations(findContainersLocations());
		}
		var regionalManagerContainer = getContainerLocations(candidateRegionalManager);
		return isNull(regionalManagerContainer)
				? getContainerLocations("Main-Container")
				: regionalManagerContainer;
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
			logger.info(MOVE_CONTAINER_LOG, agentController.getName(), targetContainer.getName());
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
		logger.info(FOUND_CONTAINERS_LOG, map.size(), locationsString);
		return map;
	}

	private Map<Location, AID> findContainerLocally() {
		final AID ams = new AID(managingAgent.getAMS().getName(), AID.ISGUID);
		prepareAndSendPlatformLocationsQuery(ams);
		final Result result = receiveLocationsResponseFromAms(ams);
		final List<Location> locations = getLocationsFromQueryResult(result);

		final String locationsString = locations.stream().map(Location::getName).collect(joining(", "));
		logger.info(FOUND_CONTAINERS_LOG, locations.size(), locationsString);

		return locations.stream().collect(toMap(location -> location, location -> ams));
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
			exception.printStackTrace();
		}
	}

	@Nullable
	private Result receiveLocationsResponseFromAms(final AID ams) {
		final ACLMessage response = managingAgent.blockingReceive(
				and(MatchSender(ams), MatchPerformative(INFORM)));
		try {
			return (Result) managingAgent.getContentManager().extractContent(response);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	private List<AMSData> getAMSData() {
		if (Objects.nonNull(managingAgent.getAgentNode())) {
			return managingAgent.getAgentNode().getDatabaseClient().readAMSData();
		}
		return emptyList();
	}

	private List<Location> getLocationsFromQueryResult(@Nullable Result result) {
		if (isNull(result)) {
			return emptyList();
		}
		List<Location> locations = new ArrayList<>();
		jade.util.leap.Iterator it = result.getItems().iterator();
		while (it.hasNext()) {
			Location loc = (Location) it.next();
			locations.add(loc);
		}
		return locations;
	}
}
