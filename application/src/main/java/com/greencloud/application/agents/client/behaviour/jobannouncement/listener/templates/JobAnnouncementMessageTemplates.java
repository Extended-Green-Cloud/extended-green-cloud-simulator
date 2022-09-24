package com.greencloud.application.agents.client.behaviour.jobannouncement.listener.templates;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.BACK_UP_POWER_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DELAYED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FINISH_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.GREEN_POWER_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in listener behaviours for job announcement for client
 */
public class JobAnnouncementMessageTemplates {

	public static final MessageTemplate CLIENT_JOB_UPDATE_TEMPLATE = and(
			or(or(or(MatchProtocol(FINISH_JOB_PROTOCOL), MatchProtocol(DELAYED_JOB_PROTOCOL)),
							or(MatchProtocol(BACK_UP_POWER_JOB_PROTOCOL), MatchProtocol(STARTED_JOB_PROTOCOL))),
					MatchProtocol(GREEN_POWER_JOB_PROTOCOL)),
			MatchPerformative(INFORM));
}
