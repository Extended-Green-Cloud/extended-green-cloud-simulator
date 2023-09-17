package org.greencloud.agentsystem.agents.greenenergy;

import static com.greencloud.commons.yellowpages.YellowPagesService.deregister;
import static com.greencloud.commons.yellowpages.YellowPagesService.register;
import static com.greencloud.commons.yellowpages.domain.DFServiceConstants.GS_SERVICE_NAME;
import static com.greencloud.commons.yellowpages.domain.DFServiceConstants.GS_SERVICE_TYPE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import com.greencloud.commons.args.agent.greenenergy.agent.enums.GreenEnergySourceTypeEnum;
import com.greencloud.commons.domain.location.ImmutableLocation;
import com.greencloud.commons.domain.location.Location;

import jade.core.AID;

/**
 * Agent representing the Green Energy Source that produces the power for the Servers
 */
public class GreenEnergyAgent extends AbstractGreenEnergyAgent {

	private static final Logger logger = getLogger(GreenEnergyAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length >= 10) {
			final AID monitoringAgent = new AID(args[0].toString(), AID.ISLOCALNAME);
			final AID ownerServer = new AID(args[1].toString(), AID.ISLOCALNAME);

			try {
				final double latitude = parseDouble(args[4].toString());
				final double longitude = parseDouble(args[5].toString());
				final int maximumGeneratorCapacity = parseInt(args[2].toString());
				final double pricePerPowerUnit = parseDouble(args[3].toString());
				final double weatherPredictionError = parseDouble(args[7].toString());
				final Location location = new ImmutableLocation(latitude, longitude);
				final GreenEnergySourceTypeEnum energyType = (GreenEnergySourceTypeEnum) args[6];

				this.properties = new GreenEnergyAgentProps(getName(), location, energyType, monitoringAgent,
						ownerServer, pricePerPowerUnit, weatherPredictionError, maximumGeneratorCapacity);

				// Additional argument indicates if the GreenSourceAgent is going to be moved to another container
				// In such case, its service should be registered after moving
				if (args.length != 10 || !parseBoolean(args[8].toString())) {
					register(this, getDefaultDF(), GS_SERVICE_TYPE, GS_SERVICE_NAME, ownerServer.getName());
				}

			} catch (final NumberFormatException e) {
				logger.info("Couldn't parse one of the numerical arguments");
				doDelete();
			}
		} else {
			logger.info("Incorrect arguments: some parameters for Green Source Agent are missing.");
			doDelete();
		}
	}

	@Override
	protected void takeDown() {
		deregister(this, getDefaultDF(), GS_SERVICE_TYPE, GS_SERVICE_NAME, properties.getOwnerServer().getName());
		super.takeDown();
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		register(this, getDefaultDF(), GS_SERVICE_TYPE, GS_SERVICE_NAME, properties.getOwnerServer().getName());
	}
}
