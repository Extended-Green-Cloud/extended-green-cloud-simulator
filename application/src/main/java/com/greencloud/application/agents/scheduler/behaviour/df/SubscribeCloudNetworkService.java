package com.greencloud.application.agents.scheduler.behaviour.df;

import static com.greencloud.application.agents.scheduler.behaviour.df.logs.SchedulerDFLog.RECEIVE_CNA_ANNOUNCEMENT_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.df.logs.SchedulerDFLog.SUBSCRIBE_CNA_SERVICE_LOG;
import static com.greencloud.application.yellowpages.YellowPagesService.decodeSubscription;
import static com.greencloud.application.yellowpages.YellowPagesService.prepareSubscription;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.CNA_SERVICE_TYPE;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.scheduler.SchedulerAgent;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

/**
 * Behaviours subscribes CNA service in the DF
 */
public class SubscribeCloudNetworkService extends SubscriptionInitiator {

	private static final Logger logger = LoggerFactory.getLogger(SubscribeCloudNetworkService.class);

	private final SchedulerAgent mySchedulerAgent;

	private SubscribeCloudNetworkService(final SchedulerAgent agent, ACLMessage subscription) {
		super(agent, subscription);
		this.mySchedulerAgent = (SchedulerAgent) myAgent;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent scheduler subscribing to DF
	 * @return InitiateServiceSubscription
	 */
	public static SubscribeCloudNetworkService create(final SchedulerAgent agent) {
		logger.info(SUBSCRIBE_CNA_SERVICE_LOG);
		return new SubscribeCloudNetworkService(agent, prepareSubscription(agent, CNA_SERVICE_TYPE));
	}

	@Override
	protected void handleInform(ACLMessage inform) {
		final List<AID> announcedCNAList = decodeSubscription(inform);

		logger.info(RECEIVE_CNA_ANNOUNCEMENT_LOG);
		mySchedulerAgent.getAvailableCloudNetworks().addAll(announcedCNAList);
	}
}
