package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CONNECT_GREEN_SOURCE;
import static com.database.knowledge.domain.agent.DataType.AVAILABLE_GREEN_ENERGY;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_LONG_TIME_PERIOD;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.planner.domain.AgentsGreenPower;
import org.greencloud.managingsystem.service.planner.domain.AgentsTraffic;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.greensource.AvailableGreenEnergy;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.google.common.annotations.VisibleForTesting;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.managingsystem.planner.ImmutableConnectGreenSourceParameters;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of connecting new Green Source with given Server
 */
public class ConnectGreenSourcePlan extends AbstractPlan {

	private static final double SERVER_TRAFFIC_THRESHOLD = 0.9;
	private static final double GREEN_SOURCE_TRAFFIC_THRESHOLD = 0.5;
	private static final double GREEN_SOURCE_POWER_THRESHOLD = 0.7;

	private Map<AgentsGreenPower, List<AgentsTraffic>> connectableServersForGreenSource;

	public ConnectGreenSourcePlan(ManagingAgent managingAgent) {
		super(CONNECT_GREEN_SOURCE, managingAgent);
		connectableServersForGreenSource = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some GS alive in the system
	 * 2. there are some Green Sources which are connected to the Servers from one CNA and for which the
	 * average traffic during last 15s was not greater than 50% (i.e. idle green sources)
	 * 3. the available power for these Green Sources during last 15s was on average equal to at least 70% of
	 * maximum capacity (i.e. green sources were not on idle due to bad weather conditions)
	 * 4. there are some Servers in the Cloud Network which traffic is less than 90% (i.e. not all servers are using
	 * all operational power)
	 * 5. the list of Servers (satisfying the above thresholds) to which a Green Source (which also satisfy
	 * above thresholds) may connect, is not empty (i.e. the Green Sources can be connected to new Servers)
	 *
	 * @return boolean value indicating if the plan is executable
	 */
	@Override
	public boolean isPlanExecutable() {
		// verifying which server complies with a thresholds
		final Map<String, List<AgentsTraffic>> serversForCloudNetworks = getAvailableServersMap();
		if (serversForCloudNetworks.isEmpty()) {
			return false;
		}

		// verifying which green sources comply with the thresholds
		final Map<String, Set<AgentsGreenPower>> greenSourcesForCloudNetworks = getAvailableGreenSourcesMap();
		if (greenSourcesForCloudNetworks.isEmpty()) {
			return false;
		}

		// verifying if green sources complying with thresholds can connect to new servers
		connectableServersForGreenSource =
				getConnectableServersForGreenSources(serversForCloudNetworks, greenSourcesForCloudNetworks);

		return !connectableServersForGreenSource.isEmpty();
	}

	/**
	 * Method constructs plan which connects an additional green source to the given server
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		if (connectableServersForGreenSource.isEmpty()) {
			return null;
		}

		final AgentsGreenPower selectedGreenSource =
				connectableServersForGreenSource.keySet().stream()
						.min(comparingDouble(AgentsGreenPower::value))
						.orElseThrow();
		final String selectedServer =
				connectableServersForGreenSource.get(selectedGreenSource).stream()
						.min(comparingDouble(AgentsTraffic::value))
						.orElseThrow()
						.name();

		targetAgent = new AID(selectedGreenSource.name(), AID.ISGUID);
		actionParameters = ImmutableConnectGreenSourceParameters.builder()
				.serverName(selectedServer)
				.build();
		postActionHandler = () ->
				managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().stream()
						.filter(agent -> agent.getName().equals(selectedGreenSource.name().split("@")[0]))
						.forEach(greenSource -> greenSource.getConnectedSevers().add(selectedServer.split("@")[0]));

		return this;
	}

	@VisibleForTesting
	protected Map<String, List<AgentsTraffic>> getAvailableServersMap() {
		final List<String> aliveServers = managingAgent.monitor().getAliveAgents(SERVER);

		return managingAgent.getGreenCloudStructure().getCloudNetworkAgentsArgs().stream()
				.collect(toMap(AgentArgs::getName, cna -> getServersForCNA(cna.getName(), aliveServers)))
				.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@VisibleForTesting
	protected Map<String, Set<AgentsGreenPower>> getAvailableGreenSourcesMap() {
		final List<String> aliveGreenSources = managingAgent.monitor().getAliveAgents(GREEN_SOURCE);

		return managingAgent.getGreenCloudStructure().getCloudNetworkAgentsArgs().stream()
				.collect(toMap(AgentArgs::getName, cna -> getGreenSourcesForCNA(cna.getName(), aliveGreenSources)))
				.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@VisibleForTesting
	protected Map<AgentsGreenPower, List<AgentsTraffic>> getConnectableServersForGreenSources(
			final Map<String, List<AgentsTraffic>> serversForCloudNetworks,
			final Map<String, Set<AgentsGreenPower>> greenSourcesForCloudNetworks) {

		return greenSourcesForCloudNetworks.keySet().stream()
				.map(cloudNetwork -> {
					final List<AgentsTraffic> serversToConsider = serversForCloudNetworks.get(cloudNetwork);
					final Set<AgentsGreenPower> greenSourcesToConsider = greenSourcesForCloudNetworks.get(cloudNetwork);

					if (Objects.isNull(serversToConsider)) {
						return new HashMap<AgentsGreenPower, List<AgentsTraffic>>();
					}

					return greenSourcesToConsider.stream()
							.map(greenSource -> getConnectableServersForGreenSourcePerCNA(greenSource,
									serversToConsider))
							.filter(entry -> !entry.getValue().isEmpty())
							.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
				})
				.flatMap(map -> map.entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map.Entry<AgentsGreenPower, List<AgentsTraffic>> getConnectableServersForGreenSourcePerCNA(
			final AgentsGreenPower greenSource, final List<AgentsTraffic> serversToConsider) {
		final String greenSourceLocalName = greenSource.name().split("@")[0];

		final List<String> alreadyConnectedServers =
				managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().stream()
						.filter(gs -> gs.getName().equals(greenSourceLocalName))
						.findFirst().orElseThrow()
						.getConnectedSevers();

		final List<AgentsTraffic> availableForConnectionServers =
				serversToConsider.stream().collect(filtering(server ->
						!alreadyConnectedServers.contains(server.name().split("@")[0]), toList()));

		return new AbstractMap.SimpleEntry<>(greenSource, availableForConnectionServers);
	}

	@VisibleForTesting
	protected List<AgentsTraffic> getServersForCNA(final String cna, final List<String> aliveServers) {
		final List<String> serversForCNA = managingAgent.getGreenCloudStructure().getServersForCloudNetworkAgent(cna);
		final List<String> aliveServersForCNA = managingAgent.monitor()
				.getAliveAgentsIntersection(aliveServers, serversForCNA);

		return getAverageTrafficForServers(aliveServersForCNA).entrySet().stream()
				.filter(server -> server.getValue() <= SERVER_TRAFFIC_THRESHOLD)
				.map(entry -> new AgentsTraffic(entry.getKey(), entry.getValue()))
				.toList();
	}

	@VisibleForTesting
	protected Set<AgentsGreenPower> getGreenSourcesForCNA(final String cloudNetworkName,
			final List<String> aliveGreenSources) {
		final List<String> greenSourcesForCNA = managingAgent.getGreenCloudStructure()
				.getGreenSourcesForCloudNetwork(cloudNetworkName);

		final List<String> aliveSourcesForServer = managingAgent.monitor()
				.getAliveAgentsIntersection(aliveGreenSources, greenSourcesForCNA);
		final List<String> sourcesWithEnoughPower = getSourcesWithEnoughPower(aliveSourcesForServer);

		return getAverageTrafficForSources(sourcesWithEnoughPower).entrySet().stream()
				.filter(greenSource -> greenSource.getValue() <= GREEN_SOURCE_TRAFFIC_THRESHOLD)
				.map(entry -> new AgentsGreenPower(entry.getKey(), entry.getValue()))
				.collect(toSet());
	}

	private List<String> getSourcesWithEnoughPower(final List<String> aliveGreenSources) {
		return getAveragePowerForSources(aliveGreenSources).entrySet().stream()
				.filter(entry -> entry.getValue() >= GREEN_SOURCE_POWER_THRESHOLD)
				.map(Map.Entry::getKey)
				.toList();
	}

	@VisibleForTesting
	protected Map<String, Double> getAverageTrafficForSources(final List<String> aliveSourcesForServer) {
		final ToDoubleFunction<AgentData> getTrafficForGreenSource =
				data -> ((GreenSourceMonitoringData) data.monitoringData()).getCurrentTraffic();
		return getAverageValuesMap(GREEN_SOURCE_MONITORING, aliveSourcesForServer, getTrafficForGreenSource);
	}

	@VisibleForTesting
	protected Map<String, Double> getAveragePowerForSources(final List<String> aliveSourcesForServer) {
		final ToDoubleFunction<AgentData> getPowerForGreenSource =
				data -> ((AvailableGreenEnergy) data.monitoringData()).availablePowerPercentage();
		return getAverageValuesMap(AVAILABLE_GREEN_ENERGY, aliveSourcesForServer, getPowerForGreenSource);
	}

	@VisibleForTesting
	protected Map<String, Double> getAverageTrafficForServers(final List<String> aliveServersForCNA) {
		final ToDoubleFunction<AgentData> getTrafficForServer =
				data -> ((ServerMonitoringData) data.monitoringData()).getCurrentTraffic();
		return getAverageValuesMap(SERVER_MONITORING, aliveServersForCNA, getTrafficForServer);
	}

	@VisibleForTesting
	protected void setConnectableServersForGreenSource(
			Map<AgentsGreenPower, List<AgentsTraffic>> connectableServersForGreenSource) {
		this.connectableServersForGreenSource = connectableServersForGreenSource;
	}

	private Map<String, Double> getAverageValuesMap(final DataType dataType, final List<String> agentsOfInterest,
			final ToDoubleFunction<AgentData> averagingFunc) {
		if (agentsOfInterest.isEmpty()) {
			return Collections.emptyMap();
		}

		final List<AgentData> agentsData = managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypeAndAID(dataType, agentsOfInterest, MONITOR_SYSTEM_DATA_LONG_TIME_PERIOD);
		final List<String> agentsPresentInDatabase = agentsData.stream().map(AgentData::aid).toList();

		final Map<String, Double> agentsWithRecordsMap = agentsData.stream()
				.collect(Collectors.groupingBy(AgentData::aid, TreeMap::new, averagingDouble(averagingFunc)));
		final Map<String, Double> agentsWithNoRecords = getAgentsNotPresentInData(agentsPresentInDatabase,
				agentsOfInterest);
		agentsWithRecordsMap.putAll(agentsWithNoRecords);

		return agentsWithRecordsMap;
	}

	private Map<String, Double> getAgentsNotPresentInData(final List<String> presentAgents,
			final List<String> allConsideredAgents) {
		return allConsideredAgents.stream()
				.filter(agent -> !presentAgents.contains(agent))
				.collect(toMap(agent -> agent, agent -> 0.0));
	}
}
