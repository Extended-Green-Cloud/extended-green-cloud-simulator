package com.greencloud.application.agents.server.behaviour.powershortage.listener.templates;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;

import com.greencloud.application.messages.domain.constants.MessageProtocolConstants;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in listener behaviours for power shortage
 */
public class PowerShortageServerMessageTemplates {

	public static final MessageTemplate SOURCE_JOB_TRANSFER_REQUEST_TEMPLATE = and(MatchPerformative(REQUEST),
			MatchProtocol(MessageProtocolConstants.POWER_SHORTAGE_ALERT_PROTOCOL));
	public static final MessageTemplate SOURCE_JOB_TRANSFER_CONFIRMATION_TEMPLATE = and(MatchPerformative(INFORM),
			MatchProtocol(MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL));
	public static final MessageTemplate SOURCE_POWER_SHORTAGE_FINISH_TEMPLATE = and(MatchPerformative(INFORM),
			MatchProtocol(MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL));
}
