package org.greencloud.agentsystem.agents.greenenergy;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.greencloud.commons.constants.DFServiceConstants.GS_SERVICE_NAME;
import static org.greencloud.commons.constants.DFServiceConstants.GS_SERVICE_TYPE;
import static org.jrba.utils.yellowpages.YellowPagesRegister.deregister;
import static org.jrba.utils.yellowpages.YellowPagesRegister.register;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.function.BooleanSupplier;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.location.ImmutableLocation;
import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.slf4j.Logger;

import jade.core.AID;

/**
 * Agent representing the Green Energy Source that produces the power for the Servers.
 */
public class GreenEnergyAgent extends AbstractGreenEnergyAgent {

	private static final Logger logger = getLogger(GreenEnergyAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length >= 11) {
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

				completeAgentRegistration(args);
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

	private void completeAgentRegistration(final Object[] args) {
		final BooleanSupplier isAgentMoved = () -> args.length != 11 && !parseBoolean(args[8].toString());
		final String ownerName = properties.getOwnerServer().getName();

		if (isAgentMoved.getAsBoolean()) {
			register(this, getDefaultDF(), GS_SERVICE_TYPE, GS_SERVICE_NAME, ownerName);
		}
	}
}
