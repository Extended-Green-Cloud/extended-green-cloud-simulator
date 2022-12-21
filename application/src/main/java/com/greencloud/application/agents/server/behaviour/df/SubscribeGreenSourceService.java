package com.greencloud.application.agents.server.behaviour.df;

import static com.greencloud.application.agents.server.behaviour.df.logs.ServerDFLog.RECEIVE_GS_ANNOUNCEMENT_LOG;
import static com.greencloud.application.agents.server.behaviour.df.logs.ServerDFLog.SUBSCRIBE_GS_SERVICE_LOG;
import static com.greencloud.application.yellowpages.YellowPagesService.decodeSubscription;
import static com.greencloud.application.yellowpages.YellowPagesService.prepareSubscription;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.GS_SERVICE_TYPE;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.server.ServerAgent;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

/**
 * Behaviours subscribes Green Source service in the DF
 */
public class SubscribeGreenSourceService extends SubscriptionInitiator {

	private static final Logger logger = LoggerFactory.getLogger(SubscribeGreenSourceService.class);

	private final ServerAgent myServerAgent;

	private SubscribeGreenSourceService(final ServerAgent agent, ACLMessage subscription) {
		super(agent, subscription);
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent server subscribing to DF
	 * @return InitiateServiceSubscription
	 */
	public static SubscribeGreenSourceService create(final ServerAgent agent) {
		logger.info(SUBSCRIBE_GS_SERVICE_LOG);
		return new SubscribeGreenSourceService(agent, prepareSubscription(agent, GS_SERVICE_TYPE, agent.getName()));
	}

	/**
	 * Method is triggered when new Green Source defined service for given Server.
	 * It adds the Green Source to the Server's green sources list
	 *
	 * @param inform retrieved information
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		final List<AID> announcedGreenSources = decodeSubscription(inform);

		logger.info(RECEIVE_GS_ANNOUNCEMENT_LOG);
		myServerAgent.manageConfig().connectNewGreenSourcesToServer(announcedGreenSources);
	}
}
