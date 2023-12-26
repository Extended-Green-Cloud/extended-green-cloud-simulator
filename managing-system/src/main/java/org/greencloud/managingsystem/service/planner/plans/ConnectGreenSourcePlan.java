package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CONNECT_GREEN_SOURCE;
import static com.database.knowledge.domain.agent.DataType.AVAILABLE_GREEN_ENERGY;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.greencloud.commons.args.agent.AgentType.GREEN_ENERGY;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import org.greencloud.commons.args.adaptation.singleagent.ImmutableChangeGreenSourceConnectionParameters;
import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.planner.plans.domain.AgentsGreenPower;
import org.greencloud.managingsystem.service.planner.plans.domain.AgentsTraffic;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.AvailableGreenEnergy;
import com.database.knowledge.domain.goal.GoalEnum;
import com.google.common.annotations.VisibleForTesting;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of connecting new Green Source with given Server
 */
public class ConnectGreenSourcePlan extends AbstractPlan {

	private static final double SERVER_TRAFFIC_THRESHOLD = 0.7;
	private static final double GREEN_SOURCE_TRAFFIC_THRESHOLD = 0.5;
	private static final double GREEN_SOURCE_POWER_THRESHOLD = 0.7;

	private Map<AgentsGreenPower, List<AgentsTraffic>> connectableServersForGreenSource;

	public ConnectGreenSourcePlan(ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(CONNECT_GREEN_SOURCE, managingAgent, violatedGoal);
		connectableServersForGreenSource = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some GS alive in the system
	 * 2. there are some Green Sources which are connected to the Servers from one RMA and for which the
	 * average traffic during last 15s was not greater than 50% (i.e. there are idle green sources)
	 * 3. the available power for these Green Sources in last timestamp was on average equal to at least 70% of
	 * maximum capacity (i.e. the green sources were not on idle due to bad weather conditions)
	 * 4. there are some Servers in the Region which traffic is less than 90% (i.e. not all servers are using
	 * all operational power) and which are active
	 * 5. the list of Servers (satisfying the above thresholds) to which a Green Source (which also satisfy
	 * above thresholds) may connect, is not empty (i.e. the Green Sources can be connected to new Servers)
	 *
	 * @return boolean value indicating if the plan is executable
	 */
	@Override
	public boolean isPlanExecutable() {
		// verifying which server complies with thresholds
		final Map<String, List<AgentsTraffic>> serversForRegionalManagers = getAvailableServersMap();
		if (serversForRegionalManagers.isEmpty()) {
			return false;
		}

		// verifying which green sources comply with the thresholds
		final Map<String, Set<AgentsGreenPower>> greenSourcesForRegionalManagers = getAvailableGreenSourcesMap();
		if (greenSourcesForRegionalManagers.isEmpty()) {
			return false;
		}

		// verifying if green sources complying with thresholds can connect to new servers
		connectableServersForGreenSource =
				getConnectableServersForGreenSources(serversForRegionalManagers, greenSourcesForRegionalManagers);

		return !connectableServersForGreenSource.isEmpty();
	}

	/**
	 * Method constructs plan which connects an additional green source to the given server.
	 * The method selects:
	 * - Server with minimal traffic
	 * - Green Source with minimal power usage
	 * Upon executing the action, the structure of cloud network is updated.
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
		actionParameters = ImmutableChangeGreenSourceConnectionParameters.builder()
				.serverName(selectedServer)
				.build();
		postActionHandler = () ->
				managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().stream()
						.filter(agent -> agent.getName().equals(selectedGreenSource.name().split("@")[0]))
						.forEach(greenSource -> greenSource.getConnectedServers().add(selectedServer.split("@")[0]));

		return this;
	}

	@VisibleForTesting
	protected Map<String, List<AgentsTraffic>> getAvailableServersMap() {
		final List<String> activeServers = managingAgent.monitor().getActiveServers();

		return managingAgent.getGreenCloudStructure().getRegionalManagerAgentsArgs().stream()
				.collect(toMap(AgentArgs::getName, rma -> getServersForRMA(rma.getName(), activeServers)))
				.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@VisibleForTesting
	protected Map<String, Set<AgentsGreenPower>> getAvailableGreenSourcesMap() {
		final List<String> aliveGreenSources = managingAgent.monitor().getAliveAgents(GREEN_ENERGY);

		return managingAgent.getGreenCloudStructure().getRegionalManagerAgentsArgs().stream()
				.collect(toMap(AgentArgs::getName, rma -> getGreenSourcesForRMA(rma.getName(), aliveGreenSources)))
				.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@VisibleForTesting
	protected Map<AgentsGreenPower, List<AgentsTraffic>> getConnectableServersForGreenSources(
			final Map<String, List<AgentsTraffic>> serversForRegionalManagers,
			final Map<String, Set<AgentsGreenPower>> greenSourcesForRegionalManagers) {

		return greenSourcesForRegionalManagers.keySet().stream()
				.map(regionalManager -> {
					final List<AgentsTraffic> serversToConsider = serversForRegionalManagers.get(regionalManager);
					final Set<AgentsGreenPower> greenSourcesToConsider = greenSourcesForRegionalManagers.get(regionalManager);

					if (Objects.isNull(serversToConsider)) {
						return new HashMap<AgentsGreenPower, List<AgentsTraffic>>();
					}

					return greenSourcesToConsider.stream()
							.map(greenSource -> getConnectableServersForGreenSourcePerRMA(greenSource,
									serversToConsider))
							.filter(entry -> !entry.getValue().isEmpty())
							.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
				})
				.flatMap(map -> map.entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map.Entry<AgentsGreenPower, List<AgentsTraffic>> getConnectableServersForGreenSourcePerRMA(
			final AgentsGreenPower greenSource, final List<AgentsTraffic> serversToConsider) {
		final String greenSourceLocalName = greenSource.name().split("@")[0];

		final List<String> alreadyConnectedServers =
				managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().stream()
						.filter(gs -> gs.getName().equals(greenSourceLocalName))
						.findFirst().orElseThrow()
						.getConnectedServers();

		final List<AgentsTraffic> availableForConnectionServers =
				serversToConsider.stream().collect(filtering(server ->
						!alreadyConnectedServers.contains(server.name().split("@")[0]), toList()));

		return new AbstractMap.SimpleEntry<>(greenSource, availableForConnectionServers);
	}

	@VisibleForTesting
	protected List<AgentsTraffic> getServersForRMA(final String rma, final List<String> aliveServers) {
		final List<String> serversForRMA = managingAgent.getGreenCloudStructure().getServersForRegionalManagerAgent(rma);
		final List<String> aliveServersForRMA = managingAgent.monitor()
				.getAliveAgentsIntersection(aliveServers, serversForRMA);

		return managingAgent.monitor().getAverageTrafficForNetworkComponent(aliveServersForRMA, SERVER_MONITORING)
				.entrySet().stream()
				.filter(server -> server.getValue() <= SERVER_TRAFFIC_THRESHOLD)
				.map(entry -> new AgentsTraffic(entry.getKey(), entry.getValue()))
				.toList();
	}

	@VisibleForTesting
	protected Set<AgentsGreenPower> getGreenSourcesForRMA(final String regionalManagerName,
			final List<String> aliveGreenSources) {
		final List<String> greenSourcesForRMA = managingAgent.getGreenCloudStructure()
				.getGreenSourcesForRegionalManager(regionalManagerName);

		final List<String> aliveSourcesForServer = managingAgent.monitor()
				.getAliveAgentsIntersection(aliveGreenSources, greenSourcesForRMA);

		return managingAgent.monitor()
				.getAverageTrafficForNetworkComponent(getSourcesWithEnoughPower(aliveSourcesForServer),
						GREEN_SOURCE_MONITORING)
				.entrySet().stream()
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
	protected Map<String, Double> getAveragePowerForSources(final List<String> aliveSourcesForServer) {
		final ToDoubleFunction<AgentData> getPowerForGreenSource =
				data -> ((AvailableGreenEnergy) data.monitoringData()).availablePowerPercentage();
		return managingAgent.monitor()
				.getAverageValuesForAgents(AVAILABLE_GREEN_ENERGY, aliveSourcesForServer, getPowerForGreenSource);
	}

	@VisibleForTesting
	protected void setConnectableServersForGreenSource(
			Map<AgentsGreenPower, List<AgentsTraffic>> connectableServersForGreenSource) {
		this.connectableServersForGreenSource = connectableServersForGreenSource;
	}

}
