package com.greencloud.application.agents.cloudnetwork.behaviour.df.listener.templates;

import static com.greencloud.application.messages.constants.MessageProtocolConstants.DISABLE_SERVER_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.ENABLE_SERVER_PROTOCOL;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in Cloud Network listener behaviours for DF registration
 */
public class DFCloudNetworkMessageTemplates {

	public static final MessageTemplate SERVER_STATUS_CHANGE_TEMPLATE = and(
			MatchPerformative(REQUEST),
			or(MatchProtocol(DISABLE_SERVER_PROTOCOL), MatchProtocol(ENABLE_SERVER_PROTOCOL))
	);
}
