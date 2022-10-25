package com.greencloud.application.agents.greenenergy.behaviour.sensor;

import static com.greencloud.application.domain.powershortage.PowerShortageCause.PHYSICAL_CAUSE;
import static java.util.Objects.isNull;

import java.util.Optional;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.AnnounceSourcePowerShortage;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.AnnounceSourcePowerShortageFinish;
import com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants;
import com.gui.agents.GreenEnergyAgentNode;
import com.gui.event.domain.PowerShortageEvent;

import jade.core.behaviours.TickerBehaviour;

/**
 * Behaviour listens and reads the environmental eventsQueue
 */
public class SenseGreenSourceEvent extends TickerBehaviour {

	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param myGreenEnergyAgent agent which is executing the behaviour
	 */
	public SenseGreenSourceEvent(final GreenEnergyAgent myGreenEnergyAgent) {
		super(myGreenEnergyAgent, GreenEnergyAgentConstants.GREEN_ENERGY_ENVIRONMENT_SENSOR_TIMEOUT);
		this.myGreenEnergyAgent = myGreenEnergyAgent;
	}

	/**
	 * Method verifies if some outside event has occurred
	 */
	@Override
	protected void onTick() {
		var greenEnergyAgentNode = (GreenEnergyAgentNode) myGreenEnergyAgent.getAgentNode();

		if (isNull(greenEnergyAgentNode)) {
			return;
		}

		Optional<PowerShortageEvent> latestEvent = greenEnergyAgentNode.getEvent();
		latestEvent.ifPresent(event -> {
			if (event.isFinished()) {
				myGreenEnergyAgent.addBehaviour(new AnnounceSourcePowerShortageFinish(myGreenEnergyAgent));
			} else {
				myGreenEnergyAgent.addBehaviour(new AnnounceSourcePowerShortage(myGreenEnergyAgent,
						null, event.getOccurrenceTime(), (double) event.getNewMaximumCapacity(), PHYSICAL_CAUSE));
			}
		});
	}
}
