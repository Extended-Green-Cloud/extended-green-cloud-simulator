package com.greencloud.application.agents.server.behaviour.sensor;

import static com.greencloud.application.agents.server.constants.ServerAgentConstants.SERVER_ENVIRONMENT_SENSOR_TIMEOUT;
import static java.util.Objects.isNull;

import java.util.Optional;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.errorhandling.announcer.AnnounceInternalServerErrorFinish;
import com.greencloud.application.agents.server.behaviour.errorhandling.announcer.AnnounceInternalServerErrorStart;
import com.gui.agents.ServerAgentNode;
import com.gui.event.domain.PowerShortageEvent;

import jade.core.behaviours.TickerBehaviour;

/**
 * Behaviour listens and reads environmental eventsQueue to which new events associated with Server Agent
 * are being added
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

		final Optional<PowerShortageEvent> latestEvent = serverAgentNode.getEvent();
		latestEvent.ifPresent(event -> {
			if (event.isFinished()) {
				myServerAgent.addBehaviour(new AnnounceInternalServerErrorFinish(myServerAgent));
			} else {
				myServerAgent.addBehaviour(new AnnounceInternalServerErrorStart(myServerAgent, event));
			}
		});
	}
}

