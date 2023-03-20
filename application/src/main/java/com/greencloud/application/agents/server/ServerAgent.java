package com.greencloud.application.agents.server;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static com.database.knowledge.domain.action.AdaptationActionEnum.DISABLE_SERVER;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.ADAPTATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.COMMUNICATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.application.yellowpages.YellowPagesService.deregister;
import static com.greencloud.application.yellowpages.YellowPagesService.register;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_NAME;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_TYPE;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;

import com.database.knowledge.domain.action.AdaptationAction;
import com.greencloud.application.agents.server.behaviour.df.SubscribeGreenSourceService;
import com.greencloud.application.agents.server.behaviour.df.listener.ListenForCloudNetworkCapacityCheckRequest;
import com.greencloud.application.agents.server.behaviour.df.listener.ListenForGreenSourceServiceUpdate;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForJobStartCheckRequest;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForManualJobFinish;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForNewJob;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForPowerSupplyUpdate;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForServerJobCancellation;
import com.greencloud.application.agents.server.behaviour.powershortage.handler.HandleSourcePowerShortageJobs;
import com.greencloud.application.agents.server.behaviour.powershortage.listener.ListenForSourceJobTransferRequest;
import com.greencloud.application.agents.server.behaviour.powershortage.listener.ListenForSourcePowerShortageFinish;
import com.greencloud.application.agents.server.behaviour.sensor.SenseServerEvent;
import com.greencloud.application.agents.server.management.ServerAdaptationManagement;
import com.greencloud.application.agents.server.management.ServerCommunicationManagement;
import com.greencloud.application.agents.server.management.ServerStateManagement;
import com.greencloud.application.behaviours.ListenForAdaptationAction;
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
		if (Objects.nonNull(args) && args.length == 4) {
			this.ownerCloudNetworkAgent = new AID(args[0].toString(), AID.ISLOCALNAME);

			try {
				this.pricePerHour = parseDouble(args[1].toString());
				this.currentMaximumCapacity = parseInt(args[2].toString());
				this.initialMaximumCapacity = parseInt(args[2].toString());
				this.jobProcessingLimit = parseInt(args[3].toString());

			} catch (final NumberFormatException e) {
				logger.info("Some of the arguments are not a number!");
				doDelete();
			}

			register(this, SA_SERVICE_TYPE, SA_SERVICE_NAME, this.getOwnerCloudNetworkAgent().getName());
		} else {
			logger.info("Incorrect arguments: some parameters for server agent are missing");
			doDelete();
		}
	}

	@Override
	protected void initializeAgentManagements() {
		this.agentManagementServices = new EnumMap<>(Map.of(
				STATE_MANAGEMENT, new ServerStateManagement(this),
				ADAPTATION_MANAGEMENT, new ServerAdaptationManagement(this),
				COMMUNICATION_MANAGEMENT, new ServerCommunicationManagement(this)
		));
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
				new ListenForServerJobCancellation(),
				new ListenForCloudNetworkCapacityCheckRequest(this),
				new ListenForGreenSourceServiceUpdate(this),
				new ListenForAdaptationAction(this)
		);
	}

	@Override
	public boolean executeAction(AdaptationAction adaptationAction, AdaptationActionParameters actionParameters) {
		if (adaptationAction.getAction() == CHANGE_GREEN_SOURCE_WEIGHT) {
			return adapt()
					.changeGreenSourceWeights(((ChangeGreenSourceWeights) actionParameters).greenSourceName());
		}
		return false;
	}

	@Override
	public void executeAction(final AdaptationAction adaptationAction,
			final AdaptationActionParameters actionParameters, final ACLMessage adaptationMessage) {
		if (adaptationAction.getAction() == DISABLE_SERVER) {
			adapt().disableServer(adaptationMessage);
		}
	}

	@Override
	protected void takeDown() {
		deregister(this, SA_SERVICE_TYPE, SA_SERVICE_NAME, this.getOwnerCloudNetworkAgent().getName());
		super.takeDown();
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		initializeAgentManagements();

		// restoring default values
		this.pricePerHour = 20;
		this.jobProcessingLimit = 20;
	}
}
