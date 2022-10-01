package runner.factory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import com.greencloud.commons.args.AgentArgs;
import com.greencloud.commons.args.client.ClientAgentArgs;
import com.greencloud.commons.args.client.ImmutableClientAgentArgs;
import com.greencloud.commons.args.cloudnetwork.CloudNetworkArgs;
import com.greencloud.commons.args.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.server.ServerAgentArgs;
import com.gui.agents.AbstractAgentNode;
import com.gui.agents.ClientAgentNode;
import com.gui.agents.CloudNetworkAgentNode;
import com.gui.agents.GreenEnergyAgentNode;
import com.gui.agents.MonitoringAgentNode;
import com.gui.agents.ServerAgentNode;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import runner.domain.ScenarioArgs;

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

			return containerController.createNewAgent(clientAgent.getName(),
					"com.greencloud.application.agents.client.ClientAgent",
					new Object[] { startDate, endDate, clientAgent.getPower(), clientAgent.getJobId() });
		} else if (agentArgs instanceof ServerAgentArgs serverAgent) {
			return containerController.createNewAgent(serverAgent.getName(),
					"com.greencloud.application.agents.server.ServerAgent",
					new Object[] { serverAgent.getOwnerCloudNetwork(), serverAgent.getPrice(),
							serverAgent.getMaximumCapacity() });
		} else if (agentArgs instanceof CloudNetworkArgs cloudNetworkAgent) {
			return containerController.createNewAgent(cloudNetworkAgent.getName(),
					"com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent", new Object[] {});
		} else if (agentArgs instanceof GreenEnergyAgentArgs greenEnergyAgent) {
			return containerController.createNewAgent(greenEnergyAgent.getName(),
					"com.greencloud.application.agents.greenenergy.GreenEnergyAgent",
					new Object[] { greenEnergyAgent.getMonitoringAgent(),
							greenEnergyAgent.getOwnerSever(),
							greenEnergyAgent.getMaximumCapacity(),
							greenEnergyAgent.getPricePerPowerUnit(),
							greenEnergyAgent.getLatitude(),
							greenEnergyAgent.getLongitude(),
							greenEnergyAgent.getEnergyType() });
		} else if (agentArgs instanceof MonitoringAgentArgs monitoringAgent) {
			return containerController.createNewAgent(monitoringAgent.getName(),
					"com.greencloud.application.agents.monitoring.MonitoringAgent",
					new Object[] {});
		}
		return null;
	}

	@Override
	public AbstractAgentNode createAgentNode(AgentArgs agentArgs, ScenarioArgs scenarioArgs) {
		if (agentArgs instanceof ClientAgentArgs clientArgs) {
			return new ClientAgentNode(ImmutableClientAgentArgs.copyOf(clientArgs)
					.withStart(formatToDate(clientArgs.getStart()))
					.withEnd(formatToDate(clientArgs.getEnd())));
		}
		if (agentArgs instanceof CloudNetworkArgs cloudNetworkArgs) {
			final List<ServerAgentArgs> ownedServers = scenarioArgs.getServerAgentsArgs().stream()
					.filter(serverArgs -> serverArgs.getOwnerCloudNetwork().equals(cloudNetworkArgs.getName()))
					.toList();
			final double maximumCapacity = ownedServers.stream()
					.mapToDouble(server -> Double.parseDouble(server.getMaximumCapacity())).sum();
			final List<String> serverList = ownedServers.stream().map(ServerAgentArgs::getName).toList();

			return new CloudNetworkAgentNode(cloudNetworkArgs.getName(), maximumCapacity, serverList);
		}
		if (agentArgs instanceof GreenEnergyAgentArgs greenEnergyAgentArgs) {
			return new GreenEnergyAgentNode(greenEnergyAgentArgs);
		}
		if (agentArgs instanceof MonitoringAgentArgs monitoringAgentArgs) {
			final GreenEnergyAgentArgs ownerGreenSource = scenarioArgs.getGreenEnergyAgentsArgs().stream()
					.filter(greenSourceArgs -> greenSourceArgs.getMonitoringAgent()
							.equals(monitoringAgentArgs.getName()))
					.findFirst()
					.orElse(null);
			if (Objects.nonNull(ownerGreenSource)) {
				return new MonitoringAgentNode(monitoringAgentArgs.getName(), ownerGreenSource.getName());
			}
			return null;
		}
		if (agentArgs instanceof ServerAgentArgs serverAgentArgs) {
			final List<GreenEnergyAgentArgs> ownedGreenSources = scenarioArgs.getGreenEnergyAgentsArgs()
					.stream()
					.filter(greenEnergyArgs -> greenEnergyArgs.getOwnerSever().equals(serverAgentArgs.getName()))
					.toList();
			final List<String> greenSourceNames = ownedGreenSources.stream().map(GreenEnergyAgentArgs::getName)
					.toList();
			return new ServerAgentNode(serverAgentArgs.getName(),
					Double.parseDouble(serverAgentArgs.getMaximumCapacity()), serverAgentArgs.getOwnerCloudNetwork(),
					greenSourceNames);
		}
		return null;
	}

	private String formatToDate(final String value) {
		final Instant date = Instant.now().plus(Long.parseLong(value), ChronoUnit.HOURS);
		final String dateFormat = "dd/MM/yyyy HH:mm";
		return DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.of("UTC")).format(date);
	}
}
