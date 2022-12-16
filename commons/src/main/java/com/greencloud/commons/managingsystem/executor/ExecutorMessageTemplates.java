package com.greencloud.commons.managingsystem.executor;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;

import jade.lang.acl.MessageTemplate;

public final class ExecutorMessageTemplates {

	public static final String EXECUTE_ACTION_PROTOCOL = "EXECUTE_ACTION_PROTOCOL";
	public static final MessageTemplate EXECUTE_ACTION_REQUEST = and(MatchPerformative(REQUEST),
			MatchProtocol(EXECUTE_ACTION_PROTOCOL));

	public static final String ANNOUNCE_NETWORK_CHANGE_PROTOCOL = "ANNOUNCE_NETWORK_CHANGE_PROTOCOL";
	public static final MessageTemplate ANNOUNCE_NETWORK_CHANGE = and(MatchPerformative(INFORM),
			MatchProtocol(ANNOUNCE_NETWORK_CHANGE_PROTOCOL));

	private ExecutorMessageTemplates() {
	}
}
