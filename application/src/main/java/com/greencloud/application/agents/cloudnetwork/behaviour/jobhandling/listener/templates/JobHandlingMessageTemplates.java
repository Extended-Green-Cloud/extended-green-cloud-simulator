package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.templates;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CLIENT_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CONFIRMED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FINISH_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in listener behaviours for job handling in cloud network
 */
public class JobHandlingMessageTemplates {

	public static final MessageTemplate NEW_JOB_REQUEST_TEMPLATE = and(MatchPerformative(CFP),
			MatchProtocol(CLIENT_JOB_CFP_PROTOCOL));
	public static final MessageTemplate JOB_STATUS_CHANGE_TEMPLATE = and(MatchPerformative(INFORM),
			or(or(MatchProtocol(FINISH_JOB_PROTOCOL), MatchProtocol(STARTED_JOB_PROTOCOL)),
					or(MatchProtocol(POWER_SHORTAGE_FINISH_ALERT_PROTOCOL), MatchProtocol(CONFIRMED_JOB_PROTOCOL))));

}
