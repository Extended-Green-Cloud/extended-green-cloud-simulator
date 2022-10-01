package com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.template;

import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import com.greencloud.application.messages.domain.constants.MessageProtocolConstants;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in power supply listening behaviours
 */
public class PowerSupplyMessageTemplates {

	public static final MessageTemplate POWER_SUPPLY_REQUEST_TEMPLATE = and(MatchPerformative(CFP),
			MatchProtocol(MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL));
	public static final MessageTemplate POWER_SUPPLY_STATUS_TEMPLATE = and(MatchPerformative(INFORM),
			or(MatchProtocol(MessageProtocolConstants.FINISH_JOB_PROTOCOL), MatchProtocol(
					MessageProtocolConstants.STARTED_JOB_PROTOCOL)));
}
