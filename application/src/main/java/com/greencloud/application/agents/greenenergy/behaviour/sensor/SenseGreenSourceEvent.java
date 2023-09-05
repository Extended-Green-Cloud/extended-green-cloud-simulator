package com.greencloud.application.agents.greenenergy.behaviour.sensor;

import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.GREEN_ENERGY_ENVIRONMENT_SENSOR_TIMEOUT;
import static com.greencloud.commons.args.event.powershortage.PowerShortageCause.PHYSICAL_CAUSE;
import static java.util.Objects.isNull;

import java.util.Optional;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.AnnounceSourcePowerShortage;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.AnnounceSourcePowerShortageFinish;
import com.gui.agents.GreenEnergyAgentNode;
import com.gui.event.domain.PowerShortageEvent;

import jade.core.behaviours.TickerBehaviour;

/**
 * Behaviour listens and reads the environmental eventsQueue to which new events associated with Green Source Agent
 * are being added
 */
public class SenseGreenSourceEvent extends TickerBehaviour {

	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param myGreenEnergyAgent agent which is executing the behaviour
	 */
	public SenseGreenSourceEvent(final GreenEnergyAgent myGreenEnergyAgent) {
		super(myGreenEnergyAgent, GREEN_ENERGY_ENVIRONMENT_SENSOR_TIMEOUT);
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

		final Optional<PowerShortageEvent> latestEvent = greenEnergyAgentNode.getEvent();

		latestEvent.ifPresent(event -> {
			if (event.isFinished() && event.getCause().equals(PHYSICAL_CAUSE)) {
				myGreenEnergyAgent.addBehaviour(new AnnounceSourcePowerShortageFinish(myGreenEnergyAgent));
			} else {
				myGreenEnergyAgent.addBehaviour(new AnnounceSourcePowerShortage(myGreenEnergyAgent, null,
						event.getOccurrenceTime(), 0D, event.getCause()));
				myGreenEnergyAgent.manage().getShortagesAccumulator().getAndIncrement();
			}
		});
	}
}
