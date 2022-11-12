package com.greencloud.application.agents.client.behaviour.jobannouncement.listener.templates;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in listener behaviours for job announcement for client
 */
public class JobAnnouncementMessageTemplates {

	public static final MessageTemplate CLIENT_JOB_UPDATE_TEMPLATE =
			and(MatchProtocol(CHANGE_JOB_STATUS_PROTOCOL), MatchPerformative(INFORM));
}
