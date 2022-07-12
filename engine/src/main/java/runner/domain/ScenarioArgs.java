package runner.domain;

import static java.util.stream.Stream.concat;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import org.jetbrains.annotations.Nullable;
import runner.service.domain.ClientGenerationType;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Arguments of the entire scenario
 */
public class ScenarioArgs implements Serializable {

    @JacksonXmlElementWrapper(localName = "clientGenerationType")
    private ClientGenerationType clientGenerationType;
    @JacksonXmlElementWrapper(localName = "clientAgentsArgs")
    @Nullable
    private List<ImmutableClientAgentArgs> clientAgentsArgs;
    @JacksonXmlElementWrapper(localName = "cloudNetworkAgentsArgs")
    private List<ImmutableCloudNetworkArgs> cloudNetworkAgentsArgs;
    @JacksonXmlElementWrapper(localName = "serverAgentsArgs")
    private List<ImmutableServerAgentArgs> serverAgentsArgs;
    @JacksonXmlElementWrapper(localName = "monitoringAgentsArgs")
    private List<ImmutableMonitoringAgentArgs> monitoringAgentsArgs;
    @JacksonXmlElementWrapper(localName = "greenEnergyAgentsArgs")
    private List<ImmutableGreenEnergyAgentArgs> greenEnergyAgentsArgs;

    public ScenarioArgs() {
    }

    /**
     * Scenario constructor.
     *
     * @param clientGenerationType   client generation mode
     * @param clientAgentsArgs       list of client agents
     * @param cloudNetworkAgentsArgs list of cloud network agents
     * @param serverAgentsArgs       list of server agents
     * @param monitoringAgentsArgs   list of monitoring agents
     * @param greenEnergyAgentsArgs  list of green energy source agents
     */
    public ScenarioArgs(ClientGenerationType clientGenerationType,
                        List<ImmutableClientAgentArgs> clientAgentsArgs,
                        List<ImmutableCloudNetworkArgs> cloudNetworkAgentsArgs,
                        List<ImmutableServerAgentArgs> serverAgentsArgs,
                        List<ImmutableMonitoringAgentArgs> monitoringAgentsArgs,
                        List<ImmutableGreenEnergyAgentArgs> greenEnergyAgentsArgs) {
        this.clientGenerationType = clientGenerationType;
        this.clientAgentsArgs = clientAgentsArgs;
        this.cloudNetworkAgentsArgs = cloudNetworkAgentsArgs;
        this.serverAgentsArgs = serverAgentsArgs;
        this.monitoringAgentsArgs = monitoringAgentsArgs;
        this.greenEnergyAgentsArgs = greenEnergyAgentsArgs;
    }
    public ClientGenerationType getClientGenerationType() {return clientGenerationType; }

    public void setClientGenerationType(ClientGenerationType clientGenerationType){
        this.clientGenerationType = clientGenerationType;
    }

    public List<ImmutableClientAgentArgs> getClientAgentsArgs() {
        return clientAgentsArgs;
    }

    public void setClientAgentsArgs(List<ImmutableClientAgentArgs> clientAgentsArgs) {
        this.clientAgentsArgs = clientAgentsArgs;
    }

    public List<ImmutableCloudNetworkArgs> getCloudNetworkAgentsArgs() {
        return cloudNetworkAgentsArgs;
    }

    public void setCloudNetworkAgentsArgs(List<ImmutableCloudNetworkArgs> cloudNetworkAgentsArgs) {
        this.cloudNetworkAgentsArgs = cloudNetworkAgentsArgs;
    }

    public List<ImmutableServerAgentArgs> getServerAgentsArgs() {
        return serverAgentsArgs;
    }

    public void setServerAgentsArgs(List<ImmutableServerAgentArgs> serverAgentsArgs) {
        this.serverAgentsArgs = serverAgentsArgs;
    }

    public List<ImmutableMonitoringAgentArgs> getMonitoringAgentsArgs() {
        return monitoringAgentsArgs;
    }

    public void setMonitoringAgentsArgs(List<ImmutableMonitoringAgentArgs> args) {
        this.monitoringAgentsArgs = args;
    }

    public List<ImmutableGreenEnergyAgentArgs> getGreenEnergyAgentsArgs() {
        return greenEnergyAgentsArgs;
    }

    public void setGreenEnergyAgentsArgs(List<ImmutableGreenEnergyAgentArgs> args) {
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
        var firstArgs = Objects.nonNull(clientAgentsArgs) ? concat(cloudNetworkArgs, clientAgentsArgs.stream().map(AgentArgs.class::cast)) : cloudNetworkArgs;

        return concat(monitoringArgs,
                      concat(greenEnergyArgs,
                             concat(serverArgs, firstArgs))).toList();
    }
}
