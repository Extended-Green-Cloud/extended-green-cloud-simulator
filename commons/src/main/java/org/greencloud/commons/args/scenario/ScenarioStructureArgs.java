package org.greencloud.commons.args.scenario;

import static java.util.Objects.isNull;
import static java.util.stream.Stream.concat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.cloudnetwork.factory.CloudNetworkArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.scheduler.factory.SchedulerArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.jetbrains.annotations.Nullable;

/**
 * Arguments of the structure of Cloud Network in given scenario
 */
public class ScenarioStructureArgs implements Serializable {

	private ManagingAgentArgs managingAgentArgs;
	private SchedulerArgs schedulerAgentArgs;
	private List<CloudNetworkArgs> cloudNetworkAgentsArgs;
	private List<ServerArgs> serverAgentsArgs;
	private List<MonitoringArgs> monitoringAgentsArgs;
	private List<GreenEnergyArgs> greenEnergyAgentsArgs;

	public ScenarioStructureArgs() {
	}

	/**
	 * Scenario constructor.
	 *
	 * @param managingAgentArgs      managing agent
	 * @param schedulerAgentArgs     scheduler agent
	 * @param cloudNetworkAgentsArgs list of cloud network com.greencloud.application.agents
	 * @param serverAgentsArgs       list of server com.greencloud.application.agents
	 * @param monitoringAgentsArgs   list of monitoring com.greencloud.application.agents
	 * @param greenEnergyAgentsArgs  list of green energy source com.greencloud.application.agents
	 */
	public ScenarioStructureArgs(ManagingAgentArgs managingAgentArgs,
			SchedulerArgs schedulerAgentArgs,
			List<CloudNetworkArgs> cloudNetworkAgentsArgs,
			List<ServerArgs> serverAgentsArgs,
			List<MonitoringArgs> monitoringAgentsArgs,
			List<GreenEnergyArgs> greenEnergyAgentsArgs) {
		this.managingAgentArgs = managingAgentArgs;
		this.schedulerAgentArgs = schedulerAgentArgs;
		this.cloudNetworkAgentsArgs = new ArrayList<>(cloudNetworkAgentsArgs);
		this.serverAgentsArgs = new ArrayList<>(serverAgentsArgs);
		this.monitoringAgentsArgs = new ArrayList<>(monitoringAgentsArgs);
		this.greenEnergyAgentsArgs = new ArrayList<>(greenEnergyAgentsArgs);
	}

	public List<CloudNetworkArgs> getCloudNetworkAgentsArgs() {
		return cloudNetworkAgentsArgs;
	}

	public List<ServerArgs> getServerAgentsArgs() {
		return serverAgentsArgs;
	}

	public List<MonitoringArgs> getMonitoringAgentsArgs() {
		return monitoringAgentsArgs;
	}

	public List<GreenEnergyArgs> getGreenEnergyAgentsArgs() {
		return greenEnergyAgentsArgs;
	}

	public SchedulerArgs getSchedulerAgentArgs() {
		return schedulerAgentArgs;
	}

	public ManagingAgentArgs getManagingAgentArgs() {
		return managingAgentArgs;
	}

	/**
	 * Method retrieves servers connected to given cloud network agent
	 *
	 * @param cloudNetworkAgentName name of the CNA of interest
	 * @return list of connected server
	 */
	public List<String> getServersForCloudNetworkAgent(final String cloudNetworkAgentName) {
		return getServerAgentsArgs()
				.stream()
				.filter(agent -> agent.getOwnerCloudNetwork().equals(cloudNetworkAgentName))
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
	 * Method retrieves green sources connected to given cloud network agent
	 *
	 * @param cloudNetworkAgentName name of the Cloud Network of interest
	 * @return list of connected green sources
	 */
	public List<String> getGreenSourcesForCloudNetwork(final String cloudNetworkAgentName) {
		return getServersForCloudNetworkAgent(cloudNetworkAgentName).stream()
				.map(this::getGreenSourcesForServerAgent)
				.flatMap(Collection::stream)
				.toList();
	}

	/**
	 * Method retrieves name of parent CNA for server with given name
	 *
	 * @param serverName name of the Server
	 * @return name of CNA or null if not found
	 */
	@Nullable
	public String getParentCNAForServer(final String serverName) {
		var serverArgs = serverAgentsArgs.stream()
				.filter(server -> server.getName().equals(serverName.split("@")[0]))
				.findFirst()
				.orElse(null);

		return isNull(serverArgs) ? null : serverArgs.getOwnerCloudNetwork();
	}

	/**
	 * Method concatenates the scenario arguments into one stream
	 *
	 * @return stream of all scenario's com.greencloud.application.agents' arguments
	 */
	public List<AgentArgs> getAgentsArgs() {
		var serverArgs = serverAgentsArgs.stream().map(AgentArgs.class::cast);
		var cloudNetworkArgs = cloudNetworkAgentsArgs.stream().map(AgentArgs.class::cast);
		var monitoringArgs = monitoringAgentsArgs.stream().map(AgentArgs.class::cast);
		var greenEnergyArgs = greenEnergyAgentsArgs.stream().map(AgentArgs.class::cast);
		var schedulerArgs = Stream.of(schedulerAgentArgs).map(AgentArgs.class::cast);
		var managingArgs = Stream.of(managingAgentArgs).map(AgentArgs.class::cast);

		return concat(managingArgs,
				concat(schedulerArgs,
						concat(monitoringArgs,
								concat(greenEnergyArgs,
										concat(serverArgs, cloudNetworkArgs))))).toList();
	}
}
