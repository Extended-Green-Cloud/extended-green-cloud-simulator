package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CONNECT_GREEN_SOURCE;
import static com.database.knowledge.domain.agent.DataType.AVAILABLE_GREEN_ENERGY;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_LONG_TIME_PERIOD;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.greencloud.managingsystem.agent.ManagingAgent;

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

	private Map<Map.Entry<String, Double>, Map<String, Double>> connectableServersForGreenSource;

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
		final Map<String, Map<String, Double>> serversForCloudNetworks = getAvailableServersMap();
		if (serversForCloudNetworks.isEmpty()) {
			return false;
		}

		// verifying which green sources comply with the thresholds
		final Map<String, Map<String, Double>> greenSourcesForCloudNetworks = getAvailableGreenSourcesMap();
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

		final Map.Entry<String, Double> selectedGreenSource =
				connectableServersForGreenSource.keySet().stream()
						.min(Comparator.comparingDouble(Map.Entry::getValue))
						.orElseThrow();
		final String selectedServer =
				connectableServersForGreenSource.get(selectedGreenSource).entrySet().stream()
						.min(Comparator.comparingDouble(Map.Entry::getValue))
						.orElseThrow()
						.getKey();

		targetAgent = new AID(selectedGreenSource.getKey(), AID.ISGUID);
		actionParameters = ImmutableConnectGreenSourceParameters.builder()
				.serverName(selectedServer)
				.build();
		return this;
	}

	@VisibleForTesting
	protected Map<String, Map<String, Double>> getAvailableServersMap() {
		final List<String> aliveServers = managingAgent.monitor().getAliveAgents(SERVER);

		return managingAgent.getGreenCloudStructure().getCloudNetworkAgentsArgs().stream()
				.collect(toMap(AgentArgs::getName, cna -> getServersForCNA(cna.getName(), aliveServers)))
				.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@VisibleForTesting
	protected Map<String, Map<String, Double>> getAvailableGreenSourcesMap() {
		final List<String> aliveGreenSources = managingAgent.monitor().getAliveAgents(GREEN_SOURCE);

		return managingAgent.getGreenCloudStructure().getCloudNetworkAgentsArgs().stream()
				.collect(toMap(AgentArgs::getName, cna -> getGreenSourcesForCNA(cna.getName(), aliveGreenSources)))
				.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@VisibleForTesting
	protected Map<Map.Entry<String, Double>, Map<String, Double>> getConnectableServersForGreenSources(
			final Map<String, Map<String, Double>> serversForCloudNetworks,
			final Map<String, Map<String, Double>> greenSourcesForCloudNetworks) {

		return greenSourcesForCloudNetworks.keySet().stream()
				.map(cloudNetwork -> {
					final Map<String, Double> serversToConsider = serversForCloudNetworks.get(cloudNetwork);
					final Set<Map.Entry<String, Double>> greenSourcesToConsider =
							greenSourcesForCloudNetworks.get(cloudNetwork).entrySet();

					return greenSourcesToConsider.stream()
							.map(greenSource -> getConnectableServersForGreenSourcePerCNA(greenSource,
									serversToConsider))
							.filter(entry -> !entry.getValue().isEmpty())
							.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
				})
				.flatMap(map -> map.entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map.Entry<Map.Entry<String, Double>, Map<String, Double>> getConnectableServersForGreenSourcePerCNA(
			final Map.Entry<String, Double> greenSource,
			final Map<String, Double> serversToConsider) {
		final String greenSourceLocalName = greenSource.getKey().split("@")[0];

		final List<String> alreadyConnectedServers =
				managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().stream()
						.filter(gs -> gs.getName().equals(greenSourceLocalName))
						.findFirst().orElseThrow()
						.getConnectedSevers();

		final Map<String, Double> availableForConnectionServers =
				serversToConsider.entrySet().stream()
						.filter(server -> !alreadyConnectedServers.contains(server.getKey().split("@")[0]))
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		return new AbstractMap.SimpleEntry<>(greenSource, availableForConnectionServers);
	}

	@VisibleForTesting
	protected Map<String, Double> getServersForCNA(final String cna, final List<String> aliveServers) {
		final List<String> serversForCNA = managingAgent.getGreenCloudStructure().getServersForCloudNetworkAgent(cna);
		final List<String> aliveServersForCNA = getAliveAgentsIntersection(aliveServers, serversForCNA);

		return getAverageTrafficForServers(aliveServersForCNA).entrySet().stream()
				.filter(server -> server.getValue() <= SERVER_TRAFFIC_THRESHOLD)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@VisibleForTesting
	protected Map<String, Double> getGreenSourcesForCNA(final String cloudNetworkName,
			final List<String> aliveGreenSources) {
		final List<String> greenSourcesForCNA = managingAgent.getGreenCloudStructure()
				.getGreenSourcesForCloudNetwork(cloudNetworkName);

		final List<String> aliveSourcesForServer = getAliveAgentsIntersection(aliveGreenSources, greenSourcesForCNA);
		final List<String> sourcesWithEnoughPower = getSourcesWithEnoughPower(aliveSourcesForServer);

		return getAverageTrafficForSources(sourcesWithEnoughPower).entrySet().stream()
				.filter(greenSource -> greenSource.getValue() <= GREEN_SOURCE_TRAFFIC_THRESHOLD)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
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

	public void setConnectableServersForGreenSource(
			Map<Map.Entry<String, Double>, Map<String, Double>> connectableServersForGreenSource) {
		this.connectableServersForGreenSource = connectableServersForGreenSource;
	}

	private Map<String, Double> getAverageValuesMap(final DataType dataType, final List<String> agentsOfInterest,
			final ToDoubleFunction<AgentData> averagingFunc) {
		if (agentsOfInterest.isEmpty()) {
			return Collections.emptyMap();
		}

		return managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypeAndAID(dataType, agentsOfInterest,
						MONITOR_SYSTEM_DATA_LONG_TIME_PERIOD).stream()
				.collect(Collectors.groupingBy(
						AgentData::aid,
						TreeMap::new,
						averagingDouble(averagingFunc)
				));
	}

	private List<String> getAliveAgentsIntersection(List<String> allALiveAgents, List<String> allAgentsOfType) {
		final Predicate<String> isAgentNameValid = agentName -> allAgentsOfType.contains(agentName.split("@")[0]);

		return allALiveAgents.stream().filter(isAgentNameValid).toList();
	}
}
