package org.greencloud.commons.args.scenario;

import static java.util.Objects.isNull;
import static java.util.stream.Stream.concat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.regionalmanager.factory.RegionalManagerArgs;
import org.greencloud.commons.args.agent.scheduler.factory.SchedulerArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

/**
 * Arguments of the structure of Regional Manager in given scenario
 */
@Getter
public class ScenarioStructureArgs implements Serializable {

	private ManagingAgentArgs managingAgentArgs;
	private SchedulerArgs schedulerAgentArgs;
	private List<RegionalManagerArgs> regionalManagerAgentsArgs;
	private List<ServerArgs> serverAgentsArgs;
	private List<MonitoringArgs> monitoringAgentsArgs;
	private List<GreenEnergyArgs> greenEnergyAgentsArgs;

	public ScenarioStructureArgs() {
	}

	/**
	 * Scenario constructor.
	 *
	 * @param managingAgentArgs         managing agent
	 * @param schedulerAgentArgs        scheduler agent
	 * @param regionalManagerAgentsArgs list of regional manager agents
	 * @param serverAgentsArgs          list of server agents
	 * @param monitoringAgentsArgs      list of monitoring agents
	 * @param greenEnergyAgentsArgs     list of green energy source agents
	 */
	public ScenarioStructureArgs(ManagingAgentArgs managingAgentArgs,
			SchedulerArgs schedulerAgentArgs,
			List<RegionalManagerArgs> regionalManagerAgentsArgs,
			List<ServerArgs> serverAgentsArgs,
			List<MonitoringArgs> monitoringAgentsArgs,
			List<GreenEnergyArgs> greenEnergyAgentsArgs) {
		this.managingAgentArgs = managingAgentArgs;
		this.schedulerAgentArgs = schedulerAgentArgs;
		this.regionalManagerAgentsArgs = new ArrayList<>(regionalManagerAgentsArgs);
		this.serverAgentsArgs = new ArrayList<>(serverAgentsArgs);
		this.monitoringAgentsArgs = new ArrayList<>(monitoringAgentsArgs);
		this.greenEnergyAgentsArgs = new ArrayList<>(greenEnergyAgentsArgs);
	}

	public List<RegionalManagerArgs> getRegionalManagerAgentsArgs() {
		return regionalManagerAgentsArgs;
	}

	/**
	 * Method retrieves servers connected to given regional manager agent
	 *
	 * @param regionalManagerAgentName name of the RMA of interest
	 * @return list of connected server
	 */
	public List<String> getServersForRegionalManagerAgent(final String regionalManagerAgentName) {
		return getServerAgentsArgs()
				.stream()
				.filter(agent -> agent.getOwnerRegionalManager().equals(regionalManagerAgentName))
				.map(AgentArgs::getName)
				.toList();
	}

	/**
	 * Method retrieves servers connected to given green source agent
	 *
	 * @param greenSourceName name of the green source or its AID
	 * @return list of connected server
	 */
	public List<String> getServersConnectedToGreenSource(final String greenSourceName) {
		return getGreenEnergyAgentsArgs()
				.stream()
				.filter(agent -> agent.getName().equals(greenSourceName.split("@")[0]))
				.map(GreenEnergyArgs::getConnectedServers)
				.flatMap(Collection::stream)
				.toList();
	}

	/**
	 * Method retrieves green sources connected to given server agent
	 *
	 * @param serverAgentName name of the Server of interest
	 * @return list of connected green sources
	 */
	public List<String> getGreenSourcesForServerAgent(final String serverAgentName) {
		return getGreenEnergyAgentsArgs()
				.stream()
				.filter(agent -> agent.getConnectedServers().contains(serverAgentName))
				.map(AgentArgs::getName)
				.toList();
	}

	/**
	 * Method retrieves green sources connected to given regional manager agent
	 *
	 * @param regionalManagerAgentName name of the Regional Manager of interest
	 * @return list of connected green sources
	 */
	public List<String> getGreenSourcesForRegionalManager(final String regionalManagerAgentName) {
		return getServersForRegionalManagerAgent(regionalManagerAgentName).stream()
				.map(this::getGreenSourcesForServerAgent)
				.flatMap(Collection::stream)
				.toList();
	}

	/**
	 * Method retrieves name of parent RMA for server with given name
	 *
	 * @param serverName name of the Server
	 * @return name of RMA or null if not found
	 */
	@Nullable
	public String getParentRMAForServer(final String serverName) {
		var serverArgs = serverAgentsArgs.stream()
				.filter(server -> server.getName().equals(serverName.split("@")[0]))
				.findFirst()
				.orElse(null);

		return isNull(serverArgs) ? null : serverArgs.getOwnerRegionalManager();
	}

	/**
	 * Method concatenates the scenario arguments into one stream
	 *
	 * @return stream of all scenario's agents' arguments
	 */
	public List<AgentArgs> getAgentsArgs() {
		var serverArgs = serverAgentsArgs.stream().map(AgentArgs.class::cast);
		var regionalManagerArgs = regionalManagerAgentsArgs.stream().map(AgentArgs.class::cast);
		var monitoringArgs = monitoringAgentsArgs.stream().map(AgentArgs.class::cast);
		var greenEnergyArgs = greenEnergyAgentsArgs.stream().map(AgentArgs.class::cast);
		var schedulerArgs = Stream.of(schedulerAgentArgs).map(AgentArgs.class::cast);
		var managingArgs = Stream.of(managingAgentArgs).map(AgentArgs.class::cast);

		return concat(managingArgs,
				concat(schedulerArgs,
						concat(monitoringArgs,
								concat(greenEnergyArgs,
										concat(serverArgs, regionalManagerArgs))))).toList();
	}
}
