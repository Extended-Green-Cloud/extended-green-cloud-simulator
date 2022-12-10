package com.greencloud.application.agents.server.behaviour.df.templates;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CONNECT_GREEN_SOURCE_PROTOCOL;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in listener behaviours for DF registration
 */
public class DFServerMessageTemplates {
	public static final MessageTemplate GREEN_SOURCE_CONNECTION_TEMPLATE = and(MatchPerformative(REQUEST),
			MatchProtocol(CONNECT_GREEN_SOURCE_PROTOCOL));
}
