package com.greencloud.application.agents.greenenergy.behaviour.sensor;

import static com.greencloud.application.domain.powershortage.PowerShortageCause.PHYSICAL_CAUSE;

import java.util.Objects;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.AnnounceSourcePowerShortage;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.AnnounceSourcePowerShortageFinish;
import com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants;
import com.gui.event.domain.AbstractEvent;
import com.gui.event.domain.PowerShortageEvent;

import jade.core.behaviours.TickerBehaviour;

/**
 * Behaviour listens for the outside world events
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
		// TODO add new event handling using websockets
		final AbstractEvent event = null;
		//myGreenEnergyAgent.getAgentNode().removeEventFromStack();

		if (Objects.nonNull(event)) {
			switch (event.getEventTypeEnum()) {
				case POWER_SHORTAGE -> {
					final PowerShortageEvent powerShortageEvent = (PowerShortageEvent) event;

					if (powerShortageEvent.isIndicateFinish()) {
						myGreenEnergyAgent.addBehaviour(new AnnounceSourcePowerShortageFinish(myGreenEnergyAgent));
					} else {
						myGreenEnergyAgent.addBehaviour(
								new AnnounceSourcePowerShortage(myGreenEnergyAgent, null, event.getOccurrenceTime(),
										(double) powerShortageEvent.getNewMaximumPower(), PHYSICAL_CAUSE));
					}
				}
			}
		}

	}
}
