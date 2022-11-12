package runner.domain;

import static java.util.stream.Stream.concat;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.cloudnetwork.CloudNetworkArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.scheduler.SchedulerAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;

/**
 * Arguments of the structure of Cloud Network in given scenario
 */
public class ScenarioStructureArgs implements Serializable {

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
	 * @param cloudNetworkAgentsArgs list of cloud network com.greencloud.application.agents
	 * @param serverAgentsArgs       list of server com.greencloud.application.agents
	 * @param monitoringAgentsArgs   list of monitoring com.greencloud.application.agents
	 * @param greenEnergyAgentsArgs  list of green energy source com.greencloud.application.agents
	 */
	public ScenarioStructureArgs(List<CloudNetworkArgs> cloudNetworkAgentsArgs,
			List<ServerAgentArgs> serverAgentsArgs,
			List<MonitoringAgentArgs> monitoringAgentsArgs,
			List<GreenEnergyAgentArgs> greenEnergyAgentsArgs) {
		this.cloudNetworkAgentsArgs = cloudNetworkAgentsArgs;
		this.serverAgentsArgs = serverAgentsArgs;
		this.monitoringAgentsArgs = monitoringAgentsArgs;
		this.greenEnergyAgentsArgs = greenEnergyAgentsArgs;
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
		var schedulerArgs = List.of(schedulerAgentArgs).stream().map(AgentArgs.class::cast);

		return concat(schedulerArgs,
				concat(monitoringArgs,
						concat(greenEnergyArgs,
								concat(serverArgs, cloudNetworkArgs)))).toList();
	}
}
