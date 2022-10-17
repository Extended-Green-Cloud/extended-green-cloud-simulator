package com.greencloud.application.agents.server.behaviour.sensor;

import static com.greencloud.application.agents.server.domain.ServerAgentConstants.SERVER_ENVIRONMENT_SENSOR_TIMEOUT;
import static java.util.Objects.isNull;

import java.util.Optional;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.powershortage.announcer.AnnounceServerPowerShortageFinish;
import com.greencloud.application.agents.server.behaviour.powershortage.announcer.AnnounceServerPowerShortageStart;
import com.gui.agents.ServerAgentNode;
import com.gui.event.domain.PowerShortageEvent;

import jade.core.behaviours.TickerBehaviour;

/**
 * Behaviour listens for the outside world eventsQueue
 */
public class SenseServerEvent extends TickerBehaviour {

	private final ServerAgent myServerAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param myServerAgent agent which is executing the behaviour
	 */
	public SenseServerEvent(final ServerAgent myServerAgent) {
		super(myServerAgent, SERVER_ENVIRONMENT_SENSOR_TIMEOUT);
		this.myServerAgent = myServerAgent;
	}

	/**
	 * Method verifies if some outside event has occurred
	 */
	@Override
	protected void onTick() {
		var serverAgentNode = ((ServerAgentNode) myServerAgent.getAgentNode());

		if (isNull(serverAgentNode)) {
			return;
		}

		Optional<PowerShortageEvent> latestEvent = serverAgentNode.getEvent();
		latestEvent.ifPresent(event -> {
			if (event.isFinished()) {
				myServerAgent.addBehaviour(new AnnounceServerPowerShortageFinish(myServerAgent));
			} else {
				myServerAgent.addBehaviour(
						new AnnounceServerPowerShortageStart(myServerAgent, event.getOccurrenceTime(),
								event.getNewMaximumCapacity()));
			}
		});
	}
}

