package com.greencloud.commons.scenario;

import static java.util.stream.Stream.concat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.cloudnetwork.CloudNetworkArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.scheduler.SchedulerAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;

/**
 * Arguments of the structure of Cloud Network in given scenario
 */
public class ScenarioStructureArgs implements Serializable {

	@JacksonXmlElementWrapper(localName = "managingAgent")
	private ManagingAgentArgs managingAgentArgs;
	@JacksonXmlElementWrapper(localName = "schedulerAgent")
	private SchedulerAgentArgs schedulerAgentArgs;
	@JacksonXmlElementWrapper(localName = "cloudNetworkAgentsArgs")
	private List<CloudNetworkArgs> cloudNetworkAgentsArgs;
	@JacksonXmlElementWrapper(localName = "serverAgentsArgs")
	private List<ServerAgentArgs> serverAgentsArgs;
	@JacksonXmlElementWrapper(localName = "monitoringAgentsArgs")
	private List<MonitoringAgentArgs> monitoringAgentsArgs;
	@JacksonXmlElementWrapper(localName = "greenEnergyAgentsArgs")
	private List<GreenEnergyAgentArgs> greenEnergyAgentsArgs;

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
			SchedulerAgentArgs schedulerAgentArgs,
			List<CloudNetworkArgs> cloudNetworkAgentsArgs,
			List<ServerAgentArgs> serverAgentsArgs,
			List<MonitoringAgentArgs> monitoringAgentsArgs,
			List<GreenEnergyAgentArgs> greenEnergyAgentsArgs) {
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

	public List<ServerAgentArgs> getServerAgentsArgs() {
		return serverAgentsArgs;
	}

	public List<MonitoringAgentArgs> getMonitoringAgentsArgs() {
		return monitoringAgentsArgs;
	}

	public List<GreenEnergyAgentArgs> getGreenEnergyAgentsArgs() {
		return greenEnergyAgentsArgs;
	}

	public SchedulerAgentArgs getSchedulerAgentArgs() {
		return schedulerAgentArgs;
	}

	public ManagingAgentArgs getManagingAgentArgs() {
		return managingAgentArgs;
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
