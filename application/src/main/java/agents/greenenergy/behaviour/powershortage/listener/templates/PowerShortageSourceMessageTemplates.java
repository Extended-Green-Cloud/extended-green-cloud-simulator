package agents.greenenergy.behaviour.powershortage.listener.templates;

import static messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_ALERT_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
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
			or(MatchProtocol(SERVER_POWER_SHORTAGE_ALERT_PROTOCOL),
					or(MatchProtocol(POWER_SHORTAGE_FINISH_ALERT_PROTOCOL),
							MatchProtocol(SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL))));
}
