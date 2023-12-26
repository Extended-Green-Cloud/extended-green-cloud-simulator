package org.greencloud.commons.utils.messaging.constants;

import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CHANGE_SERVER_RESOURCES_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CONNECT_GREEN_SOURCE_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DEACTIVATE_GREEN_SOURCE_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DISCONNECT_GREEN_SOURCE_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.ESTIMATED_PRICE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.EXECUTION_PRICE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_SOURCE_TRANSFER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ALERT_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.JOB_START_STATUS_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.NETWORK_ERROR_ALERT_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.NETWORK_ERROR_FINISH_ALERT_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.REGISTER_SERVER_RESOURCES_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.RMA_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores common message templates
 */
public class MessageTemplatesConstants {

	/**
	 * ADAPTATION TEMPLATES
	 */
	public static final MessageTemplate EXECUTE_ACTION_REQUEST = and(MatchPerformative(REQUEST),
			MatchProtocol(MessageProtocolConstants.EXECUTE_ACTION_PROTOCOL));

	/**
	 * RULE SET TEMPLATES
	 */
	public static final MessageTemplate LISTEN_FOR_RULE_SET_UPDATE_REQUEST = and(
			MatchPerformative(REQUEST), MatchProtocol(MessageProtocolConstants.CHANGE_RULE_SET_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_RULE_SET_REMOVAL_REQUEST = and(
			MatchPerformative(REQUEST), MatchProtocol(MessageProtocolConstants.REMOVE_RULE_SET_PROTOCOL));

	/**
	 * PRICE TEMPLATES
	 */
	public static final MessageTemplate LISTEN_FOR_PRICE_UPDATE_TEMPLATE =
			and(MatchPerformative(INFORM),
					or(MatchProtocol(EXECUTION_PRICE_MESSAGE), MatchProtocol(ESTIMATED_PRICE_MESSAGE)));

	/**
	 * SCHEDULER TEMPLATES
	 */
	public static final MessageTemplate LISTEN_FOR_NEW_CLIENT_JOB_TEMPLATE = and(
			MatchPerformative(INFORM), MatchProtocol(MessageProtocolConstants.ANNOUNCED_JOB_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_JOB_STATUS_UPDATE_TEMPLATE = and(
			MatchPerformative(INFORM), MatchProtocol(CHANGE_JOB_STATUS_PROTOCOL));

	/**
	 * REGIONAL MANAGER TEMPLATES
	 */
	public static final MessageTemplate LISTEN_FOR_NEW_SCHEDULED_JOB_TEMPLATE = and(
			MatchPerformative(CFP), MatchProtocol(MessageProtocolConstants.SCHEDULER_JOB_CFP_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_SERVER_STATUS_CHANGE_TEMPLATE = and(
			MatchPerformative(REQUEST),
			or(MatchProtocol(MessageProtocolConstants.DISABLE_SERVER_PROTOCOL), MatchProtocol(
					MessageProtocolConstants.ENABLE_SERVER_PROTOCOL))
	);
	public static final MessageTemplate LISTEN_FOR_SERVER_RESOURCE_INFORMATION_TEMPLATE = and(
			MatchPerformative(INFORM),
			MatchProtocol(REGISTER_SERVER_RESOURCES_PROTOCOL)
	);
	public static final MessageTemplate LISTEN_FOR_SERVER_RESOURCE_UPDATE_TEMPLATE = and(
			MatchPerformative(REQUEST),
			MatchProtocol(CHANGE_SERVER_RESOURCES_PROTOCOL)
	);
	public static final MessageTemplate LISTEN_FOR_SERVER_JOB_STATUS_UPDATE_TEMPLATE = or(
			and(MatchPerformative(INFORM), MatchProtocol(CHANGE_JOB_STATUS_PROTOCOL)),
			and(MatchPerformative(FAILURE), MatchProtocol(FAILED_JOB_PROTOCOL)));
	public static final MessageTemplate LISTEN_FOR_SERVER_TRANSFER_REQUEST_TEMPLATE = and(MatchPerformative(REQUEST),
			MatchProtocol(NETWORK_ERROR_ALERT_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_SERVER_TRANSFER_CONFIRMATION_TEMPLATE = or(
			and(MatchPerformative(INFORM), MatchProtocol(MessageProtocolConstants.CONFIRMED_TRANSFER_PROTOCOL)),
			and(MatchPerformative(FAILURE), MatchProtocol(FAILED_TRANSFER_PROTOCOL)));

	/**
	 * CLIENT TEMPLATES
	 */
	public static final MessageTemplate LISTEN_FOR_SCHEDULER_JOB_STATUS_UPDATE_TEMPLATE =
			and(MatchProtocol(CHANGE_JOB_STATUS_PROTOCOL), MatchPerformative(INFORM));

	/**
	 * SERVER TEMPLATES
	 */
	public static final MessageTemplate LISTEN_FOR_RMA_NEW_JOB_TEMPLATE = and(
			MatchPerformative(CFP), MatchProtocol(RMA_JOB_CFP_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_RMA_RESOURCE_REQUEST_TEMPLATE = and(
			MatchPerformative(REQUEST), MatchProtocol(REGISTER_SERVER_RESOURCES_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_GREEN_SOURCE_UPDATE_TEMPLATE = and(MatchPerformative(REQUEST),
			or(or(MatchProtocol(DEACTIVATE_GREEN_SOURCE_PROTOCOL), MatchProtocol(DISCONNECT_GREEN_SOURCE_PROTOCOL)),
					MatchProtocol(CONNECT_GREEN_SOURCE_PROTOCOL)));
	public static final MessageTemplate LISTEN_FOR_GREEN_SOURCE_POWER_SUPPLY_UPDATE_TEMPLATE = or(
			and(MatchPerformative(INFORM),
					or(MatchProtocol(SERVER_JOB_CFP_PROTOCOL), MatchProtocol(POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL))),
			and(MatchPerformative(FAILURE),
					or(MatchProtocol(FAILED_JOB_PROTOCOL), MatchProtocol(FAILED_TRANSFER_PROTOCOL))));
	public static final MessageTemplate LISTEN_FOR_MANUAL_FINISH_REQUEST_TEMPLATE = and(
			MatchPerformative(INFORM), MatchProtocol(MANUAL_JOB_FINISH_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_JOB_STATUS_CHECK_REQUEST_TEMPLATE = and(
			MatchPerformative(REQUEST), MatchProtocol(JOB_START_STATUS_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_POWER_SHORTAGE_TRANSFER_REQUEST_TEMPLATE = and(
			MatchPerformative(REQUEST), MatchProtocol(NETWORK_ERROR_ALERT_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_SOURCE_TRANSFER_CONFIRMATION = or(
			and(MatchPerformative(INFORM), MatchProtocol(POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL)),
			and(MatchPerformative(FAILURE), MatchProtocol(FAILED_SOURCE_TRANSFER_PROTOCOL)));
	public static final MessageTemplate LISTEN_FOR_POWER_SHORTAGE_FINISH_TEMPLATE = and(
			MatchPerformative(INFORM), MatchProtocol(NETWORK_ERROR_FINISH_ALERT_PROTOCOL));

	/**
	 * GREEN SOURCE TEMPLATES
	 */
	public static final MessageTemplate LISTEN_FOR_SERVER_ERROR_INFORMATION = and(MatchPerformative(INFORM),
			or(MatchProtocol(INTERNAL_SERVER_ERROR_ALERT_PROTOCOL),
					or(MatchProtocol(NETWORK_ERROR_FINISH_ALERT_PROTOCOL),
							MatchProtocol(INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL))));
	public static final MessageTemplate LISTEN_FOR_SERVER_POWER_RE_SUPPLY_REQUEST =
			and(MatchPerformative(REQUEST), MatchProtocol(SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_SERVER_NEW_JOB_TEMPLATE = and(
			MatchPerformative(CFP), MatchProtocol(SERVER_JOB_CFP_PROTOCOL));
	public static final MessageTemplate LISTEN_FOR_POWER_SUPPLY_UPDATE_TEMPLATE = and(
			MatchPerformative(INFORM), MatchProtocol(CHANGE_JOB_STATUS_PROTOCOL));
}
