package com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.listener.templates;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CONFIRMED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import com.greencloud.application.messages.domain.constants.MessageProtocolConstants;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in listener behaviours for power shortage in cloud network
 */
public class PowerShortageCloudMessageTemplates {

	public static final MessageTemplate SERVER_JOB_TRANSFER_REQUEST_TEMPLATE = and(MatchPerformative(REQUEST),
			MatchProtocol(MessageProtocolConstants.POWER_SHORTAGE_ALERT_PROTOCOL));
	public static final MessageTemplate SERVER_JOB_TRANSFER_CONFIRMATION_TEMPLATE = or(
			and(MatchPerformative(INFORM), MatchProtocol(CONFIRMED_TRANSFER_PROTOCOL)),
			and(MatchPerformative(FAILURE), MatchProtocol(FAILED_TRANSFER_PROTOCOL)));
}
