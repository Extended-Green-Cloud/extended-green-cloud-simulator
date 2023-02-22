package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DISCONNECT_GREEN_SOURCE;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.planner.plans.domain.AgentsTraffic;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.google.common.annotations.VisibleForTesting;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.managingsystem.planner.ImmutableChangeGreenSourceConnectionParameters;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of disconnecting a given Green Source from the
 * Server
 */
public class DisconnectGreenSourcePlan extends AbstractPlan {

	private Map<String, List<String>> greenSourcesWithServers;

	public DisconnectGreenSourcePlan(ManagingAgent managingAgent) {
		super(DISCONNECT_GREEN_SOURCE, managingAgent);
		this.greenSourcesWithServers = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some Green Sources which are connected to more than 1 Server
	 * 2. there are some alive Green Sources which do not have a disconnection procedure active (i.e. the disconnection
	 * procedure cannot be triggered for a Green Source which still has not completed previous disconnection)
	 * 4. the Servers which are taken into account to be disconnected with a Green Source
	 * contain at least 1 other Green Source (i.e. the Server will not be left without Green Sources)
	 *
	 * @return boolean value indicating if the plan is executable
	 */
	@Override
	public boolean isPlanExecutable() {
		final Map<String, List<String>> greenSourcesWithConnectedServers = managingAgent.getGreenCloudStructure()
				.getGreenEnergyAgentsArgs().stream()
				.filter(greenSource -> greenSource.getConnectedServers().size() > 1)
				.collect(toMap(AgentArgs::getName, GreenEnergyAgentArgs::getConnectedServers));

		// verify if there are Green Sources connected to more than 1 server
		if (greenSourcesWithConnectedServers.isEmpty()) {
			return false;
		}

		final Map<String, List<String>> greenSourcesForDisconnection =
				getGreenSourcesForDisconnection(greenSourcesWithConnectedServers);

		// verify if there are Green Sources complying with the conditions
		if (greenSourcesForDisconnection.isEmpty()) {
			return false;
		}

		greenSourcesWithServers = getGreenSourcesWithServersForDisconnection(greenSourcesForDisconnection);

		// verify if there are Servers available for Green Source disconnection
		return !greenSourcesWithServers.isEmpty();
	}

	/**
	 * Method creates adaptation plan which marks a given green source to be disconnected from the
	 * selected Server. It selects a Green Source with the lowest average traffic and disconnects the Server
	 * with the lowest traffic
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		if (greenSourcesWithServers.isEmpty()) {
			return null;
		}

		final List<AgentsTraffic> greenSourcesWithTraffic =
				getAverageTrafficForGreenSources(greenSourcesWithServers.keySet().stream().toList());
		final String selectedGreenSource = greenSourcesWithTraffic.stream()
				.min(comparingDouble(AgentsTraffic::value))
				.orElseThrow()
				.name();

		final List<String> serverAIDs = greenSourcesWithServers.get(selectedGreenSource).stream()
				.map(server -> new AID(server, AID.ISGUID).getName()).toList();

		final ToDoubleFunction<AgentData> getServerTraffic =
				agentData -> ((ServerMonitoringData) agentData.monitoringData()).getCurrentTraffic();
		final String selectedServer = managingAgent.monitor()
				.getAverageValuesForAgents(SERVER_MONITORING, serverAIDs, getServerTraffic)
				.entrySet().stream()
				.min(comparingDouble(Map.Entry::getValue))
				.orElseThrow()
				.getKey();

		targetAgent = new AID(selectedGreenSource, AID.ISGUID);
		actionParameters = ImmutableChangeGreenSourceConnectionParameters.builder()
				.serverName(selectedServer)
				.build();
		postActionHandler = () ->
				managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().stream()
						.filter(agent -> agent.getName().equals(selectedGreenSource.split("@")[0]))
						.forEach(greenSource -> greenSource.getConnectedServers().remove(selectedServer));

		return this;
	}

	@VisibleForTesting
	protected Map<String, List<String>> getGreenSourcesWithServersForDisconnection(
			final Map<String, List<String>> greenSourcesForDisconnection) {
		final List<String> serversWithMoreThanOneGreenSource =
				getServersWithNumberOfGreenSources(greenSourcesForDisconnection).entrySet()
						.stream()
						.filter(entry -> entry.getValue() > 1)
						.map(Map.Entry::getKey)
						.toList();

		final Function<Map.Entry<String, List<String>>, List<String>> getServersForGreenSource = entry ->
				entry.getValue().stream()
						.filter(server -> isNameInAIDs(serversWithMoreThanOneGreenSource, server))
						.map(server -> getAIDByName(serversWithMoreThanOneGreenSource, server))
						.toList();

		return greenSourcesForDisconnection.entrySet().stream()
				.filter(greenSource -> greenSource.getValue().stream()
						.anyMatch(server -> isNameInAIDs(serversWithMoreThanOneGreenSource, server)))
				.collect(toMap(Map.Entry::getKey, getServersForGreenSource));

	}

	@VisibleForTesting
	protected Map<String, List<String>> getGreenSourcesForDisconnection(final Map<String, List<String>> greenSources) {
		final List<String> consideredGreenSources = managingAgent.monitor()
				.getAliveAgentsIntersection(GREEN_SOURCE, greenSources.keySet().stream().toList());

		if (consideredGreenSources.isEmpty()) {
			return emptyMap();
		}

		final List<AgentData> greenSourcesFromDB = managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(singletonList(GREEN_SOURCE_MONITORING));
		final List<AgentData> agentsSatisfyingConditions = greenSourcesFromDB.stream()
				.filter(data -> isAgentAvailableToDisconnect(consideredGreenSources).test(data))
				.toList();
		final List<String> agentsNotInDatabase = managingAgent.monitor()
				.getAgentsNotPresentInTheDatabase(greenSourcesFromDB, consideredGreenSources);

		return concat(agentsSatisfyingConditions.stream().map(AgentData::aid), agentsNotInDatabase.stream())
				.collect(toMap(aid -> aid, aid -> greenSources.get(aid.split("@")[0])));
	}

	private Predicate<AgentData> isAgentAvailableToDisconnect(final List<String> consideredGreenSources) {
		return agentData ->
				!((GreenSourceMonitoringData) agentData.monitoringData()).isBeingDisconnected() &&
						consideredGreenSources.contains(agentData.aid());
	}

	private List<AgentsTraffic> getAverageTrafficForGreenSources(final List<String> greenSourcesForDisconnection) {
		final ToDoubleFunction<AgentData> getGreenSourceTraffic = agentData ->
				((GreenSourceMonitoringData) agentData.monitoringData()).getCurrentTraffic();
		return managingAgent.monitor()
				.getAverageValuesForAgents(GREEN_SOURCE_MONITORING, greenSourcesForDisconnection, getGreenSourceTraffic)
				.entrySet().stream()
				.map(entry -> new AgentsTraffic(entry.getKey(), entry.getValue()))
				.toList();
	}

	private Map<String, Long> getServersWithNumberOfGreenSources(
			final Map<String, List<String>> greenSourcesForDisconnection) {
		final List<String> consideredServers = greenSourcesForDisconnection.values().stream()
				.flatMap(Collection::stream)
				.toList();
		final List<String> aliveServers = managingAgent.monitor()
				.getAliveAgentsIntersection(SERVER, consideredServers).stream()
				.toList();

		return managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().stream()
				.flatMap(entry -> entry.getConnectedServers().stream()
						.map(server -> new AbstractMap.SimpleEntry<>(server, entry.getName())))
				.filter(entry -> isNameInAIDs(aliveServers, entry.getKey()))
				.collect(groupingBy(entry -> getAIDByName(aliveServers, entry.getKey()), TreeMap::new, counting()));
	}

	@VisibleForTesting
	protected Map<String, List<String>> getGreenSourcesWithServers() {
		return greenSourcesWithServers;
	}

	@VisibleForTesting
	protected void setGreenSourcesWithServers(
			Map<String, List<String>> greenSourcesWithServers) {
		this.greenSourcesWithServers = greenSourcesWithServers;
	}

	private String getAIDByName(List<String> aidList, String name) {
		return aidList.stream()
				.filter(server -> server.split("@")[0].equals(name))
				.findFirst()
				.orElseThrow();
	}

	private boolean isNameInAIDs(List<String> aidList, String server) {
		return aidList.stream().anyMatch(aid -> aid.split("@")[0].equals(server));
	}
}
