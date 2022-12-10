package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static java.util.Collections.emptyList;
import static java.util.Collections.max;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.greencloud.commons.agentfactory.AgentFactory;
import com.greencloud.commons.agentfactory.AgentFactoryImpl;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.managingsystem.planner.AddServerActionParameters;
import com.greencloud.commons.managingsystem.planner.SystemAdaptationActionParameters;

import jade.core.Location;

/**
 * Class containing adaptation plan which realizes the action of adding new server to the system
 */
public class AddServerPlan extends AbstractPlan implements SystemPlan {

	private static final Logger logger = LoggerFactory.getLogger(AddServerPlan.class);

	protected static final double TRAFFIC_LOAD_THRESHOLD = 0.9;

	private final AgentFactory agentFactory;

	private List<AgentData> serversData;
	private Map<String, Double> serverNameToTrafficMap;

	public AddServerPlan(ManagingAgent managingAgent) {
		super(ADD_SERVER, managingAgent);
		agentFactory = new AgentFactoryImpl();
		serversData = emptyList();
	}

	/**
	 * The plan is executable if <b>all servers have traffic load over specified constant TRAFFIC_LOAD_THRESHOLD</b>
	 * value. This condition is required to make sure servers are not unnecessarily added to the not yet saturated
	 * green cloud network. If some server is not yet saturated a Green Source should be firstly added to it, as it
	 * such case it receives not enough power from connected Green Sources to saturate its computational capacity.
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		serversData = getServerData();
		return serversData.stream()
				.map(AgentData::monitoringData)
				.map(ServerMonitoringData.class::cast)
				.map(ServerMonitoringData::getCurrentTraffic)
				.mapToDouble(Double::doubleValue)
				.allMatch(traffic -> traffic >= TRAFFIC_LOAD_THRESHOLD);
	}

	private List<AgentData> getServerData() {
		return managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypes(List.of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD);
	}

	@Override
	public SystemAdaptationActionParameters getSystemAdaptationActionParameters() {
		return (SystemAdaptationActionParameters) this.actionParameters;
	}

	/**
	 * The condition is for the plan is that traffic for all servers is above the threshold. Hence, the CNA to which
	 * new server is added to should be the one with the highest average traffic load. Then CNAs of those servers are
	 * compared to find the one with the higher traffic load. Next step is to gather all information needed to add a
	 * server (together with dedicated Green Source and Monitoring Agent) to that CNA and pass that information to
	 * the ExecutorService.
	 *
	 * @return constructed {@link AddServerPlan} with required action parameters
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		serverNameToTrafficMap = serversData.stream().collect(toMap(AgentData::aid, this::getServerTraffic));

		Map<String, Double> cloudNetworkAgentsTraffic = managingAgent.getGreenCloudStructure()
				.getServerAgentsArgs().stream()
				.collect(groupingBy(ServerAgentArgs::getOwnerCloudNetwork,
						flatMapping(this::getServerTrafficByName, averagingDouble(Double::doubleValue))));

		if (cloudNetworkAgentsTraffic.isEmpty()) {
			return null;
		}

		String candidateCloudNetworkAgent = max(cloudNetworkAgentsTraffic.entrySet(),
				comparingDouble(Map.Entry::getValue)).getKey();

		ServerAgentArgs extraServerArguments = agentFactory.createDefaultServerAgent(candidateCloudNetworkAgent);
		MonitoringAgentArgs extraMonitoringAgentArguments = agentFactory.createMonitoringAgent();
		GreenEnergyAgentArgs extraGreenEnergyArguments = agentFactory.createDefaultGreenEnergyAgent(
				extraMonitoringAgentArguments.getName(), extraServerArguments.getName());
		Location targetLocation = findTargetLocation(candidateCloudNetworkAgent);

		if (isNull(targetLocation)) {
			logger.warn("Couldn't find target container for a new server!");
			return null;
		}

		actionParameters = new AddServerActionParameters(extraServerArguments, extraGreenEnergyArguments,
				extraMonitoringAgentArguments, targetLocation);

		return this;
	}

	private Double getServerTraffic(AgentData data) {
		return ((ServerMonitoringData) data.monitoringData()).getCurrentTraffic();
	}

	private Stream<Double> getServerTrafficByName(ServerAgentArgs serverArgs) {
		return serverNameToTrafficMap.entrySet()
				.stream()
				.filter(entry -> entry.getKey().contains(serverArgs.getName()))
				.map(Map.Entry::getValue);
	}

	private Location findTargetLocation(String candidateCloudNetwork) {
		var cloudNetworkContainer = managingAgent.getContainerLocations(candidateCloudNetwork);
		return isNull(cloudNetworkContainer)
				? managingAgent.getContainerLocations("Main-Container")
				: cloudNetworkContainer;
	}
}
