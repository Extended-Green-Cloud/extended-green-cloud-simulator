package com.greencloud.application.agents.client.behaviour.jobannouncement.listener.templates;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DELAYED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FINISH_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static jade.lang.acl.ACLMessage.FAILURE;
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

	public static final MessageTemplate CLIENT_JOB_UPDATE_TEMPLATE = or(
			and(or(or(MatchProtocol(FINISH_JOB_PROTOCOL), MatchProtocol(DELAYED_JOB_PROTOCOL)),
							or(MatchProtocol(CHANGE_JOB_STATUS_PROTOCOL), MatchProtocol(STARTED_JOB_PROTOCOL))),
					MatchPerformative(INFORM)),
			and(MatchProtocol(FAILED_JOB_PROTOCOL), MatchPerformative(FAILURE)));
}
