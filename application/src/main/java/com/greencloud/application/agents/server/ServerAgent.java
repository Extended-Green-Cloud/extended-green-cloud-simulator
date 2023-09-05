package com.greencloud.application.agents.server;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static com.database.knowledge.domain.action.AdaptationActionEnum.DISABLE_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionEnum.ENABLE_SERVER;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.ADAPTATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.COMMUNICATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.RESOURCE_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.application.yellowpages.YellowPagesService.deregister;
import static com.greencloud.application.yellowpages.YellowPagesService.register;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_NAME;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_TYPE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.greencloud.application.agents.server.behaviour.df.SubscribeGreenSourceService;
import com.greencloud.application.agents.server.behaviour.df.listener.ListenForGreenSourceServiceUpdate;
import com.greencloud.application.agents.server.behaviour.errorhandling.handler.HandleSourcePowerShortageJobs;
import com.greencloud.application.agents.server.behaviour.errorhandling.listener.ListenForSourceJobTransferRequest;
import com.greencloud.application.agents.server.behaviour.errorhandling.listener.ListenForSourcePowerShortageFinish;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForJobStartCheckRequest;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForManualJobFinish;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForNewJob;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForPowerSupplyUpdate;
import com.greencloud.application.agents.server.behaviour.sensor.SenseServerEvent;
import com.greencloud.application.agents.server.management.ServerAdaptationManagement;
import com.greencloud.application.agents.server.management.ServerCommunicationManagement;
import com.greencloud.application.agents.server.management.ServerResourceManagement;
import com.greencloud.application.agents.server.management.ServerStateManagement;
import com.greencloud.application.behaviours.ListenForAdaptationAction;
import com.greencloud.commons.domain.resources.HardwareResources;
import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;
import com.greencloud.commons.managingsystem.planner.ChangeGreenSourceWeights;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 * Agent representing the Server which executes the clients' jobs
 */
public class ServerAgent extends AbstractServerAgent {

	private static final Logger logger = getLogger(ServerAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length >= 8) {
			this.ownerCloudNetworkAgent = new AID(args[0].toString(), AID.ISLOCALNAME);

			try {
				this.pricePerHour = parseDouble(args[1].toString());
				this.maxPowerConsumption = parseInt(args[2].toString());
				this.idlePowerConsumption = parseInt(args[3].toString());
				this.jobProcessingLimit = parseInt(args[4].toString());
				this.resources = (HardwareResources) (args[5]);

				// Additional argument indicates if the ServerAgent is going to be moved to another container
				// In such case, its service should be registered after moving
				if (args.length != 8 || !parseBoolean(args[6].toString())) {
					register(this, getDefaultDF(), SA_SERVICE_TYPE, SA_SERVICE_NAME,
							this.getOwnerCloudNetworkAgent().getName());
				}

			} catch (final NumberFormatException e) {
				logger.info("Some of the arguments are not a number!");
				doDelete();
			}
		} else {
			logger.info("Incorrect arguments: some parameters for server agent are missing");
			doDelete();
		}
	}

	@Override
	protected void initializeAgentManagements() {
		this.agentManagementServices = new EnumMap<>(
				Map.of(STATE_MANAGEMENT, new ServerStateManagement(this),
						ADAPTATION_MANAGEMENT, new ServerAdaptationManagement(this),
						RESOURCE_MANAGEMENT, new ServerResourceManagement(this),
						COMMUNICATION_MANAGEMENT, new ServerCommunicationManagement(this)));
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		return List.of(
				SubscribeGreenSourceService.create(this),
				new ListenForNewJob(),
				new ListenForPowerSupplyUpdate(),
				new ListenForSourceJobTransferRequest(),
				new SenseServerEvent(this),
				new ListenForJobStartCheckRequest(),
				new ListenForSourcePowerShortageFinish(),
				new HandleSourcePowerShortageJobs(this),
				new ListenForManualJobFinish(),
				new ListenForGreenSourceServiceUpdate(this),
				new ListenForAdaptationAction(this)
		);
	}

	@Override
	public boolean executeAction(AdaptationActionEnum adaptationActionEnum,
			AdaptationActionParameters actionParameters) {
		if (adaptationActionEnum == CHANGE_GREEN_SOURCE_WEIGHT) {
			return adapt().changeGreenSourceWeights(((ChangeGreenSourceWeights) actionParameters).greenSourceName());
		}
		return false;
	}

	@Override
	public void executeAction(final AdaptationActionEnum adaptationActionEnum,
			final AdaptationActionParameters actionParameters, final ACLMessage adaptationMessage) {
		if (adaptationActionEnum == DISABLE_SERVER) {
			adapt().disableServer(adaptationMessage);
		} else if (adaptationActionEnum == ENABLE_SERVER) {
			adapt().enableServer(adaptationMessage);
		}
	}

	@Override
	protected void takeDown() {
		deregister(this, getDefaultDF(), SA_SERVICE_TYPE, SA_SERVICE_NAME, this.getOwnerCloudNetworkAgent().getName());
		super.takeDown();
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		register(this, getDefaultDF(), SA_SERVICE_TYPE, SA_SERVICE_NAME, this.getOwnerCloudNetworkAgent().getName());

		// restoring default values
		this.pricePerHour = 20;
		this.jobProcessingLimit = 20;
	}
}
