package com.gui.agents.greenenergy;

import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static org.greencloud.commons.args.agent.AgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.JOB_ON_HOLD_STATUSES;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static org.greencloud.commons.utils.job.JobUtils.getJobSuccessRatio;
import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.util.Optional;

import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.args.agent.greenenergy.node.GreenEnergyNodeArgs;

import com.gui.agents.EGCSNetworkNode;
import com.gui.event.AbstractEvent;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.message.ImmutableUpdateServerConnectionMessage;
import com.gui.message.domain.ImmutableServerConnection;

/**
 * Agent node class representing the green energy source
 */
public class GreenEnergyNode extends EGCSNetworkNode<GreenEnergyNodeArgs, GreenEnergyAgentProps>
		implements Serializable {

	public GreenEnergyNode() {
		super();
	}

	/**
	 * Green energy source node constructor
	 *
	 * @param greenEnergyNodeArgs arguments provided for green energy agent creation
	 */
	public GreenEnergyNode(GreenEnergyNodeArgs greenEnergyNodeArgs) {
		super(greenEnergyNodeArgs, GREEN_ENERGY);
	}

	/**
	 * Function updates current value of weather prediction error
	 *
	 * @param value new weather prediction error value
	 */
	public void updatePredictionError(final double value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value * 100)
				.agentName(agentName)
				.type("SET_WEATHER_PREDICTION_ERROR")
				.build());
	}

	/**
	 * Function updates currently supplied energy amount
	 *
	 * @param energy currently supplied energy
	 */
	public void updateEnergyInUse(final double energy) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(energy)
				.agentName(agentName)
				.type("UPDATE_ENERGY_IN_USE")
				.build());
	}

	/**
	 * Function updates the amount of available green energy for given agent
	 *
	 * @param value amount of available green energy
	 */
	public void updateGreenEnergyAmount(final double value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_AVAILABLE_GREEN_ENERGY")
				.build());
	}

	/**
	 * Function updates in the GUI the connection state for given server
	 *
	 * @param serverName  name of the server connected/disconnected to Green Source
	 * @param isConnected flag indicating if the server should be connected/disconnected
	 */
	public void updateServerConnection(final String serverName, final boolean isConnected) {
		getAgentsWebSocket().send(ImmutableUpdateServerConnectionMessage.builder()
				.agentName(this.agentName)
				.data(ImmutableServerConnection.builder()
						.isConnected(isConnected)
						.serverName(serverName)
						.build())
				.build());

	}

	public Optional<AbstractEvent> getEvent() {
		return ofNullable(eventsQueue.poll());
	}

	@Override
	public void updateGUI(final GreenEnergyAgentProps props) {
		final double successRatio = getJobSuccessRatio(props.getJobCounters().get(ACCEPTED).getCount(),
				props.getJobCounters().get(FAILED).getCount());
		final double energyInUse = props.getCurrentEnergyInUse();
		final double traffic = energyInUse / props.getMaximumGeneratorCapacity();
		final int jobsOnHold = getJobCount(props.getServerJobs(), JOB_ON_HOLD_STATUSES);
		final boolean isActive =
				props.getCurrentEnergyInUse() > 0 || getJobCount(props.getServerJobs(), JOB_ON_HOLD_STATUSES) > 0;

		updateJobsCount(getJobCount(props.getServerJobs()));
		updateJobsOnHoldCount(jobsOnHold);
		updateTraffic(traffic);
		updateEnergyInUse(energyInUse);
		updateIsActive(isActive);
		updateCurrentJobSuccessRatio(successRatio);
		saveMonitoringData(props);
	}

	@Override
	public void saveMonitoringData(final GreenEnergyAgentProps props) {
		final double successRatio = getJobSuccessRatio(props.getJobCounters().get(ACCEPTED).getCount(),
				props.getJobCounters().get(FAILED).getCount());

		final GreenSourceMonitoringData greenSourceMonitoring = ImmutableGreenSourceMonitoringData.builder()
				.weatherPredictionError(props.getWeatherPredictionError())
				.successRatio(successRatio)
				.currentTraffic(props.getCurrentEnergyInUse() / props.getMaximumGeneratorCapacity())
				.isBeingDisconnected(props.getGreenSourceDisconnection().isBeingDisconnected())
				.build();
		writeMonitoringData(GREEN_SOURCE_MONITORING, greenSourceMonitoring, props.getAgentName());
	}
}
