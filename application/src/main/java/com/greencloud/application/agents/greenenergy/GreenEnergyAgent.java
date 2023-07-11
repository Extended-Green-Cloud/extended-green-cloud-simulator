package com.greencloud.application.agents.greenenergy;

import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.ADAPTATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.POWER_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.application.yellowpages.YellowPagesService.deregister;
import static com.greencloud.application.yellowpages.YellowPagesService.register;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.GS_SERVICE_NAME;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.GS_SERVICE_TYPE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.ListenForServerPowerInformation;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.ListenForServerReSupplyRequest;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.ListenForGreenEnergyJobCancellation;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.ListenForPowerSupplyRequest;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.ListenForPowerSupplyStatus;
import com.greencloud.application.agents.greenenergy.behaviour.sensor.SenseGreenSourceEvent;
import com.greencloud.application.agents.greenenergy.behaviour.weathercheck.monitor.ReportWeatherShortages;
import com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.RequestWeatherPeriodically;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyAdaptationManagement;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyStateManagement;
import com.greencloud.application.agents.greenenergy.management.GreenPowerManagement;
import com.greencloud.commons.agent.greenenergy.GreenEnergySourceTypeEnum;
import com.greencloud.commons.domain.location.ImmutableLocation;
import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;
import com.greencloud.commons.managingsystem.planner.AdjustGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.ChangeGreenSourceConnectionParameters;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 * Agent representing the Green Energy Source that produces the power for the Servers
 */
public class GreenEnergyAgent extends AbstractGreenEnergyAgent {

	private static final Logger logger = getLogger(GreenEnergyAgent.class);

	private AID ownerServer;

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length >= 10) {
			this.monitoringAgent = new AID(args[0].toString(), AID.ISLOCALNAME);
			this.ownerServer = new AID(args[1].toString(), AID.ISLOCALNAME);

			try {
				final int maxCapacity = parseInt(args[2].toString());
				final double latitude = parseDouble(args[4].toString());
				final double longitude = parseDouble(args[5].toString());

				this.initialMaximumCapacity = maxCapacity;
				this.currentMaximumCapacity = maxCapacity;
				this.pricePerPowerUnit = parseDouble(args[3].toString());
				this.weatherPredictionError = parseDouble(args[7].toString());
				this.location = new ImmutableLocation(latitude, longitude);
				this.energyType = args[6] instanceof String type ?
						GreenEnergySourceTypeEnum.valueOf(type) :
						(GreenEnergySourceTypeEnum) args[6];

				// Additional argument indicates if the GreenSourceAgent is going to be moved to another container
				// In such case, its service should be registered after moving
				if (args.length != 11 || !parseBoolean(args[8].toString())) {
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
	protected void initializeAgentManagements() {
		this.agentManagementServices = new EnumMap<>(Map.of(
				ADAPTATION_MANAGEMENT, new GreenEnergyAdaptationManagement(this),
				STATE_MANAGEMENT, new GreenEnergyStateManagement(this),
				POWER_MANAGEMENT, new GreenPowerManagement(this)
		));
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		return List.of(
				new ListenForPowerSupplyRequest(this),
				new ListenForPowerSupplyStatus(this),
				new SenseGreenSourceEvent(this),
				new ListenForServerPowerInformation(this),
				new ListenForServerReSupplyRequest(this),
				new RequestWeatherPeriodically(this),
				new ListenForGreenEnergyJobCancellation(),
				new ReportWeatherShortages(this)
		);
	}

	@Override
	public boolean executeAction(final AdaptationActionEnum adaptationActionEnum,
			final AdaptationActionParameters actionParameters) {
		return switch (adaptationActionEnum) {
			case INCREASE_GREEN_SOURCE_ERROR, DECREASE_GREEN_SOURCE_ERROR ->
					adapt().adaptAgentWeatherPredictionError((AdjustGreenSourceErrorParameters) actionParameters);
			default -> false;
		};
	}

	@Override
	public void executeAction(AdaptationActionEnum adaptationActionEnum, AdaptationActionParameters actionParameters,
			ACLMessage adaptationMessage) {
		switch (adaptationActionEnum) {
			case CONNECT_GREEN_SOURCE ->
					adapt().connectNewServerToGreenSource((ChangeGreenSourceConnectionParameters) actionParameters,
							adaptationMessage);
			case DISCONNECT_GREEN_SOURCE ->
					adapt().disconnectGreenSourceFromServer((ChangeGreenSourceConnectionParameters) actionParameters,
							adaptationMessage);
		}
	}

	@Override
	protected void takeDown() {
		deregister(this, getDefaultDF(), GS_SERVICE_TYPE, GS_SERVICE_NAME, ownerServer.getName());
		super.takeDown();
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		register(this, getDefaultDF(), GS_SERVICE_TYPE, GS_SERVICE_NAME, ownerServer.getName());
	}
}
