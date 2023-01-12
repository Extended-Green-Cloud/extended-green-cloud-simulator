package com.greencloud.application.agents.server;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_AGENT_NAME;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.database.knowledge.domain.action.AdaptationAction;
import com.greencloud.application.agents.server.behaviour.df.SubscribeGreenSourceService;
import com.greencloud.application.agents.server.behaviour.df.listener.ListenForCloudNetworkInformationRequest;
import com.greencloud.application.agents.server.behaviour.df.listener.ListenForGreenSourceServiceUpdate;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForJobStartCheckRequest;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForNewJob;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForPowerSupplyUpdate;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.ListenForServerJobCancellation;
import com.greencloud.application.agents.server.behaviour.powershortage.handler.HandleSourcePowerShortageJobs;
import com.greencloud.application.agents.server.behaviour.powershortage.listener.ListenForSourceJobTransferRequest;
import com.greencloud.application.agents.server.behaviour.powershortage.listener.ListenForSourcePowerShortageFinish;
import com.greencloud.application.agents.server.behaviour.sensor.SenseServerEvent;
import com.greencloud.application.agents.server.management.ServerAdaptationManagement;
import com.greencloud.application.agents.server.management.ServerConfigManagement;
import com.greencloud.application.agents.server.management.ServerStateManagement;
import com.greencloud.application.behaviours.ListenForAdaptationAction;
import com.greencloud.application.behaviours.ReceiveGUIController;
import com.greencloud.application.yellowpages.YellowPagesService;
import com.greencloud.application.yellowpages.domain.DFServiceConstants;
import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;
import com.greencloud.commons.managingsystem.planner.ChangeGreenSourceWeights;

import jade.core.AID;
import jade.core.behaviours.Behaviour;

/**
 * Agent representing the Server Agent which executes the clients' jobs
 */
public class ServerAgent extends AbstractServerAgent {

	private static final Logger logger = LoggerFactory.getLogger(ServerAgent.class);

	/**
	 * Method run at the agent's start. In initialize the Server Agent based on the given by the user arguments,
	 * registers it in the DF and then runs the starting behaviours - listening for the job requests
	 */
	@Override
	protected void setup() {
		super.setup();
		MDC.put(MDC_AGENT_NAME, super.getLocalName());
		final Object[] args = getArguments();
		initializeAgent(args);
		YellowPagesService.register(this, DFServiceConstants.SA_SERVICE_TYPE, DFServiceConstants.SA_SERVICE_NAME,
				this.getOwnerCloudNetworkAgent().getName());
		addBehaviour(new ReceiveGUIController(this, behavioursRunAtStart()));
	}

	@Override
	protected void takeDown() {
		logger.info("I'm finished. Bye!");
		super.takeDown();
	}

	private void initializeAgent(final Object[] args) {
		if (Objects.nonNull(args) && args.length == 4) {
			this.stateManagement = new ServerStateManagement(this);
			this.configManagement = new ServerConfigManagement(this);
			this.adaptationManagement = new ServerAdaptationManagement(this);
			this.ownerCloudNetworkAgent = new AID(args[0].toString(), AID.ISLOCALNAME);
			try {
				this.manageConfig().setPricePerHour(Double.parseDouble(args[1].toString()));
				this.currentMaximumCapacity = Integer.parseInt(args[2].toString());
				this.initialMaximumCapacity = Integer.parseInt(args[2].toString());
				this.manageConfig().setJobProcessingLimit(Integer.parseInt(args[3].toString()));
			} catch (final NumberFormatException e) {
				logger.info("Some of the arguments are not a number!");
				doDelete();
			}
		} else {
			logger.info("Incorrect arguments: some parameters for server agent are missing - "
						+ "check the parameters in the documentation");
			doDelete();
		}
	}

	private List<Behaviour> behavioursRunAtStart() {
		return List.of(
				SubscribeGreenSourceService.create(this),
				new ListenForNewJob(),
				new ListenForPowerSupplyUpdate(),
				new ListenForSourceJobTransferRequest(),
				new SenseServerEvent(this),
				new ListenForJobStartCheckRequest(),
				new ListenForSourcePowerShortageFinish(),
				new HandleSourcePowerShortageJobs(this),
				new ListenForServerJobCancellation(),
				new ListenForCloudNetworkInformationRequest(this),
				new ListenForGreenSourceServiceUpdate(this),
				new ListenForAdaptationAction(this)
		);
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		this.stateManagement = new ServerStateManagement(this);
		this.configManagement = new ServerConfigManagement(this);
		this.adaptationManagement = new ServerAdaptationManagement(this);
		// restoring default values
		configManagement.setJobProcessingLimit(20);
		configManagement.setPricePerHour(20);
	}

	@Override
	public boolean executeAction(AdaptationAction adaptationAction, AdaptationActionParameters actionParameters) {
		if (adaptationAction.getAction() == CHANGE_GREEN_SOURCE_WEIGHT) {
			return adaptationManagement()
					.changeGreenSourceWeights(((ChangeGreenSourceWeights) actionParameters).greenSourceName());
		}

		return false;
	}
}
