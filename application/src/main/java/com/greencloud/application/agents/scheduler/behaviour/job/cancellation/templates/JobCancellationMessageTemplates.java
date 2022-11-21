package com.greencloud.application.agents.scheduler.behaviour.job.cancellation.templates;

import static jade.lang.acl.ACLMessage.CANCEL;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;

import jade.lang.acl.MessageTemplate;

public final class JobCancellationMessageTemplates {

	public static final String CANCEL_JOB_PROTOCOL = "CANCEL_JOB_PROTOCOL";
	public static final MessageTemplate CANCEL_JOB_ANNOUNCEMENT = and(MatchPerformative(CANCEL),
			MatchProtocol(CANCEL_JOB_PROTOCOL));

	private JobCancellationMessageTemplates() {
	}
}
