package com.greencloud.application.agents.cloudnetwork;

import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.application.yellowpages.YellowPagesService.deregister;
import static com.greencloud.application.yellowpages.YellowPagesService.prepareDF;
import static com.greencloud.application.yellowpages.YellowPagesService.register;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.CNA_SERVICE_NAME;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.CNA_SERVICE_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;

import com.greencloud.application.agents.cloudnetwork.behaviour.df.listener.ListenForServerStatusChange;
import com.greencloud.application.agents.cloudnetwork.behaviour.df.subscribe.FindSchedulerAgent;
import com.greencloud.application.agents.cloudnetwork.behaviour.df.subscribe.SubscribeServerService;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.ListenForJobStatusChange;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.ListenForScheduledJob;
import com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.listener.ListenForServerJobTransferRequest;
import com.greencloud.application.agents.cloudnetwork.management.CloudNetworkStateManagement;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;

/**
 * Agent representing the network component that orchestrates work in part of the Cloud Network
 */
public class CloudNetworkAgent extends AbstractCloudNetworkAgent {

	private static final Logger logger = getLogger(CloudNetworkAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		if (args.length == 4) {
			this.parentDFAddress = prepareDF(args[0].toString(), args[1].toString());
		} else {
			logger.error("Incorrect arguments: some parameters for CNA are missing");
			doDelete();
		}
	}

	@Override
	protected void initializeAgentManagements() {
		this.agentManagementServices.put(STATE_MANAGEMENT, new CloudNetworkStateManagement(this));
	}

	@Override
	protected void takeDown() {
		deregister(this, parentDFAddress, CNA_SERVICE_TYPE, CNA_SERVICE_NAME);
		super.takeDown();
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		register(this, parentDFAddress, CNA_SERVICE_TYPE, CNA_SERVICE_NAME);

		return List.of(
				prepareDFBehaviour(),
				SubscribeServerService.create(this),
				new ListenForJobStatusChange(),
				new ListenForServerJobTransferRequest(),
				new ListenForServerStatusChange(this)
		);
	}

	private SequentialBehaviour prepareDFBehaviour() {
		var startingBehaviour = new SequentialBehaviour(this);
		startingBehaviour.addSubBehaviour(new FindSchedulerAgent());
		startingBehaviour.addSubBehaviour(new ListenForScheduledJob());
		return startingBehaviour;
	}
}
