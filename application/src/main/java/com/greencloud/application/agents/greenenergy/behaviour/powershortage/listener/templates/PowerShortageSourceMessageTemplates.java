package com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.templates;

import static com.greencloud.application.messages.constants.MessageProtocolConstants.NETWORK_ERROR_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ALERT_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in listener behaviours for power shortage in Green Source
 */
public class PowerShortageSourceMessageTemplates {

	public static final MessageTemplate SERVER_POWER_SHORTAGE_INFORMATION_TEMPLATE = and(MatchPerformative(INFORM),
			or(MatchProtocol(INTERNAL_SERVER_ERROR_ALERT_PROTOCOL),
					or(MatchProtocol(NETWORK_ERROR_FINISH_ALERT_PROTOCOL),
							MatchProtocol(INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL))));

	public static final MessageTemplate SERVER_RE_SUPPLY_REQUEST_TEMPLATE =
			and(MatchPerformative(REQUEST), MatchProtocol(SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL));
}
