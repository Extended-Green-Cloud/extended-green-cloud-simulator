package com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.template;

import static com.greencloud.application.messages.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in power supply listening behaviours
 */
public class PowerSupplyMessageTemplates {

	public static final MessageTemplate POWER_SUPPLY_REQUEST_TEMPLATE = and(
			MatchPerformative(CFP), MatchProtocol(SERVER_JOB_CFP_PROTOCOL));
	public static final MessageTemplate POWER_SUPPLY_STATUS_TEMPLATE = and(
			MatchPerformative(INFORM), MatchProtocol(CHANGE_JOB_STATUS_PROTOCOL));
}
