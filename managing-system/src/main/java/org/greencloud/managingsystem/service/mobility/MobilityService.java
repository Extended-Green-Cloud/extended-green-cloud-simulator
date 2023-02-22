package org.greencloud.managingsystem.service.mobility;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static org.greencloud.managingsystem.service.mobility.logs.ManagingAgentMobilityLog.FOUND_CONTAINERS_LOG;
import static org.greencloud.managingsystem.service.mobility.logs.ManagingAgentMobilityLog.NO_LOCATION_LOG;

import java.util.ArrayList;
import java.util.List;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
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
			if (args instanceof ServerAgentArgs serverAgentArgs) {
				managingAgent.getGreenCloudStructure().getServerAgentsArgs().add(serverAgentArgs);
			}
			if (args instanceof GreenEnergyAgentArgs greenEnergyAgentArgs) {
				managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().add(greenEnergyAgentArgs);
			}
			if (args instanceof MonitoringAgentArgs monitoringAgentArgs) {
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
	public Location getContainerLocations(final String containerName) {
		return managingAgent.getContainersLocations().stream()
				.filter(location -> location.getName().contains(containerName))
				.findFirst()
				.orElseGet(() -> {
					logger.warn(NO_LOCATION_LOG);
					return null;
				});
	}

	/**
	 * Method retrieved the location of agent container which corresponds to the given Cloud Network Agent
	 *
	 * @param candidateCloudNetwork - name of the cloud network agent of interest
	 * @return location of the container
	 */
	public Location findTargetLocation(final String candidateCloudNetwork) {
		if (managingAgent.getContainersLocations() == null) {
			managingAgent.setContainersLocations(findContainersLocations());
		}
		var cloudNetworkContainer = getContainerLocations(candidateCloudNetwork);
		return isNull(cloudNetworkContainer)
				? getContainerLocations("Main-Container")
				: cloudNetworkContainer;
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
			agentController.move(targetContainer);
		} catch (StaleProxyException e) {
			throw new RuntimeException(e);
		}
	}

	private List<Location> findContainersLocations() {
		prepareAndSendPlatformLocationsQuery();
		final Result result = receiveLocationsResponseFromAms();
		final List<Location> list = getLocationsFromQueryResult(result);

		final String locationsString = list.stream()
				.map(Location::getName).collect(joining(", "));
		logger.info(FOUND_CONTAINERS_LOG, list.size(), locationsString);
		return list;
	}

	private void prepareAndSendPlatformLocationsQuery() {
		final Action queryLocations = new Action(managingAgent.getAMS(), new QueryPlatformLocationsAction());
		final ACLMessage locationsRequest = new ACLMessage(ACLMessage.REQUEST);
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
	private Result receiveLocationsResponseFromAms() {
		final ACLMessage response = managingAgent.blockingReceive(
				and(MatchSender(managingAgent.getAMS()), MatchPerformative(INFORM)));
		try {
			return (Result) managingAgent.getContentManager().extractContent(response);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
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
