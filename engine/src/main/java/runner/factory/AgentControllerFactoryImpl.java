package runner.factory;

import com.gui.domain.Location;
import com.gui.domain.nodes.*;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import runner.domain.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class AgentControllerFactoryImpl implements AgentControllerFactory {

    private final ContainerController containerController;

    public AgentControllerFactoryImpl(ContainerController containerController) {
        this.containerController = containerController;
    }

    @Override
    public AgentController createAgentController(AgentArgs agentArgs)
            throws StaleProxyException {

        if (agentArgs instanceof ClientAgentArgs clientAgent) {
            final String startDate = formatToDate(clientAgent.getStart());
            final String endDate = formatToDate(clientAgent.getEnd());

            return containerController.createNewAgent(clientAgent.getName(), "agents.client.ClientAgent",
                                                      new Object[]{startDate, endDate, clientAgent.getPower(), clientAgent.getJobId()});
        } else if (agentArgs instanceof ServerAgentArgs serverAgent) {
            return containerController.createNewAgent(serverAgent.getName(), "agents.server.ServerAgent",
                                                      new Object[]{serverAgent.getOwnerCloudNetwork(), serverAgent.getPrice(), serverAgent.getMaximumCapacity()});
        } else if (agentArgs instanceof CloudNetworkArgs cloudNetworkAgent) {
            return containerController.createNewAgent(cloudNetworkAgent.getName(),
                                                      "agents.cloudnetwork.CloudNetworkAgent", new Object[]{});
        } else if (agentArgs instanceof GreenEnergyAgentArgs greenEnergyAgent) {
            return containerController.createNewAgent(greenEnergyAgent.getName(),
                                                      "agents.greenenergy.GreenEnergyAgent",
                                                      new Object[]{greenEnergyAgent.getMonitoringAgent(),
                                                              greenEnergyAgent.getOwnerSever(),
                                                              greenEnergyAgent.getMaximumCapacity(),
                                                              greenEnergyAgent.getPricePerPowerUnit(),
                                                              greenEnergyAgent.getLatitude(),
                                                              greenEnergyAgent.getLongitude(),
                                                              greenEnergyAgent.getEnergyType()});
        } else if (agentArgs instanceof MonitoringAgentArgs monitoringAgent) {
            return containerController.createNewAgent(monitoringAgent.getName(),
                                                      "agents.monitoring.MonitoringAgent",
                                                      new Object[]{});
        }
        return null;
    }

    @Override
    public AgentNode createAgentNode(AgentArgs agentArgs, ScenarioArgs scenarioArgs) {
        if (agentArgs instanceof ClientAgentArgs clientArgs) {
            final String startDate = formatToDate(clientArgs.getStart());
            final String endDate = formatToDate(clientArgs.getEnd());

            return new ClientAgentNode(clientArgs.getName(), clientArgs.getJobId(), startDate, endDate);
        }
        if (agentArgs instanceof CloudNetworkArgs cloudNetworkArgs) {
            final List<ImmutableServerAgentArgs> ownedServers = scenarioArgs.getServerAgentsArgs().stream()
                    .filter(serverArgs -> serverArgs.getOwnerCloudNetwork().equals(cloudNetworkArgs.getName()))
                    .toList();
            final double maximumCapacity = ownedServers.stream().mapToDouble(server -> Double.parseDouble(server.getMaximumCapacity())).sum();
            final List<String> serverList = ownedServers.stream().map(ImmutableServerAgentArgs::getName).toList();

            return new CloudNetworkAgentNode(cloudNetworkArgs.getName(), maximumCapacity, serverList);
        }
        if (agentArgs instanceof GreenEnergyAgentArgs greenEnergyAgentArgs) {
            return new GreenEnergyAgentNode(greenEnergyAgentArgs.getName(),
                                            Double.parseDouble(greenEnergyAgentArgs.getMaximumCapacity()),
                                            greenEnergyAgentArgs.getMonitoringAgent(),
                                            greenEnergyAgentArgs.getOwnerSever(),
                                            new Location(greenEnergyAgentArgs.getLatitude(), greenEnergyAgentArgs.getLongitude()));
        }
        if (agentArgs instanceof MonitoringAgentArgs monitoringAgentArgs) {
            final ImmutableGreenEnergyAgentArgs ownerGreenSource = scenarioArgs.getGreenEnergyAgentsArgs().stream()
                    .filter(greenSourceArgs -> greenSourceArgs.getMonitoringAgent().equals(monitoringAgentArgs.getName()))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(ownerGreenSource)) {
                return new MonitoringAgentNode(monitoringAgentArgs.getName(), ownerGreenSource.getName());
            }
            return null;
        }
        if (agentArgs instanceof ServerAgentArgs serverAgentArgs) {
            final List<ImmutableGreenEnergyAgentArgs> ownedGreenSources = scenarioArgs.getGreenEnergyAgentsArgs().stream()
                    .filter(greenEnergyArgs -> greenEnergyArgs.getOwnerSever().equals(serverAgentArgs.getName()))
                    .toList();
            final List<String> greenSourceNames = ownedGreenSources.stream().map(ImmutableGreenEnergyAgentArgs::getName).toList();
            return new ServerAgentNode(serverAgentArgs.getName(), Double.parseDouble(serverAgentArgs.getMaximumCapacity()), serverAgentArgs.getOwnerCloudNetwork(), greenSourceNames);
        }
        return null;
    }

    private String formatToDate(final String value) {
        final OffsetDateTime date = OffsetDateTime.now().plusHours(Long.parseLong(value));
        final String dateFormat = "dd/MM/yyyy HH:mm";
        return date.format(DateTimeFormatter.ofPattern(dateFormat));
    }
}
