package com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.templates;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.ANNOUNCED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;

import jade.lang.acl.MessageTemplate;

/**
 * Class stores all message templates used in listener behaviours for job scheduling in scheduler
 */
public class JobSchedulingMessageTemplates {

	public static final MessageTemplate NEW_JOB_ANNOUNCEMENT_TEMPLATE = and(MatchPerformative(INFORM),
			MatchProtocol(ANNOUNCED_JOB_PROTOCOL));
	public static final MessageTemplate JOB_UPDATE_TEMPLATE = and(MatchPerformative(INFORM),
			MatchProtocol(CHANGE_JOB_STATUS_PROTOCOL));
}
