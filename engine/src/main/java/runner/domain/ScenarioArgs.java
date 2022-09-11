package runner.domain;

import static java.util.stream.Stream.concat;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Arguments of the entire scenario
 */
public class ScenarioArgs implements Serializable {

	@JacksonXmlElementWrapper(localName = "clientAgentsArgs")
	@Nullable
	private List<ClientAgentArgs> clientAgentsArgs;
	@JacksonXmlElementWrapper(localName = "cloudNetworkAgentsArgs")
	private List<CloudNetworkArgs> cloudNetworkAgentsArgs;
	@JacksonXmlElementWrapper(localName = "serverAgentsArgs")
	private List<ServerAgentArgs> serverAgentsArgs;
	@JacksonXmlElementWrapper(localName = "monitoringAgentsArgs")
	private List<MonitoringAgentArgs> monitoringAgentsArgs;
	@JacksonXmlElementWrapper(localName = "greenEnergyAgentsArgs")
	private List<GreenEnergyAgentArgs> greenEnergyAgentsArgs;

	public ScenarioArgs() {
	}

	/**
	 * Scenario constructor.
	 *
	 * @param clientAgentsArgs       list of client agents
	 * @param cloudNetworkAgentsArgs list of cloud network agents
	 * @param serverAgentsArgs       list of server agents
	 * @param monitoringAgentsArgs   list of monitoring agents
	 * @param greenEnergyAgentsArgs  list of green energy source agents
	 */
	public ScenarioArgs(List<ClientAgentArgs> clientAgentsArgs,
			List<CloudNetworkArgs> cloudNetworkAgentsArgs,
			List<ServerAgentArgs> serverAgentsArgs,
			List<MonitoringAgentArgs> monitoringAgentsArgs,
			List<GreenEnergyAgentArgs> greenEnergyAgentsArgs) {
		this.clientAgentsArgs = clientAgentsArgs;
		this.cloudNetworkAgentsArgs = cloudNetworkAgentsArgs;
		this.serverAgentsArgs = serverAgentsArgs;
		this.monitoringAgentsArgs = monitoringAgentsArgs;
		this.greenEnergyAgentsArgs = greenEnergyAgentsArgs;
	}

	public List<ClientAgentArgs> getClientAgentsArgs() {
		return clientAgentsArgs;
	}

	public void setClientAgentsArgs(List<ClientAgentArgs> clientAgentsArgs) {
		this.clientAgentsArgs = clientAgentsArgs;
	}

	public List<CloudNetworkArgs> getCloudNetworkAgentsArgs() {
		return cloudNetworkAgentsArgs;
	}

	public void setCloudNetworkAgentsArgs(List<CloudNetworkArgs> cloudNetworkAgentsArgs) {
		this.cloudNetworkAgentsArgs = cloudNetworkAgentsArgs;
	}

	public List<ServerAgentArgs> getServerAgentsArgs() {
		return serverAgentsArgs;
	}

	public void setServerAgentsArgs(List<ServerAgentArgs> serverAgentsArgs) {
		this.serverAgentsArgs = serverAgentsArgs;
	}

	public List<MonitoringAgentArgs> getMonitoringAgentsArgs() {
		return monitoringAgentsArgs;
	}

	public void setMonitoringAgentsArgs(List<MonitoringAgentArgs> args) {
		this.monitoringAgentsArgs = args;
	}

	public List<GreenEnergyAgentArgs> getGreenEnergyAgentsArgs() {
		return greenEnergyAgentsArgs;
	}

	public void setGreenEnergyAgentsArgs(List<GreenEnergyAgentArgs> args) {
		this.greenEnergyAgentsArgs = args;
	}

	/**
	 * Method concatenates the scenario arguments into one stream
	 *
	 * @return stream of all scenario's agents' arguments
	 */
	public List<AgentArgs> getAgentsArgs() {
		var serverArgs = serverAgentsArgs.stream().map(AgentArgs.class::cast);
		var cloudNetworkArgs = cloudNetworkAgentsArgs.stream().map(AgentArgs.class::cast);
		var monitoringArgs = monitoringAgentsArgs.stream().map(AgentArgs.class::cast);
		var greenEnergyArgs = greenEnergyAgentsArgs.stream().map(AgentArgs.class::cast);
		var firstArgs = Objects.nonNull(clientAgentsArgs) ?
				concat(cloudNetworkArgs, clientAgentsArgs.stream().map(AgentArgs.class::cast)) :
				cloudNetworkArgs;

		return concat(monitoringArgs,
				concat(greenEnergyArgs,
						concat(serverArgs, firstArgs))).toList();
	}
}
