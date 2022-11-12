package com.greencloud.application.agents.client.behaviour.jobannouncement.initiator;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.logs.JobAnnouncementInitiatorLog.SEND_INFORM_TO_SCHEDULER;
import static com.greencloud.application.agents.client.domain.ClientAgentConstants.SCHEDULER_AGENT;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobAnnouncementMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.domain.job.ClientJob;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour sends the information announcing new job to Scheduler Agent
 */
public class InitiateNewJobAnnouncement extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(InitiateNewJobAnnouncement.class);

	private final transient ClientJob job;
	private final ClientAgent myClientAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent agent executing the behaviour
	 * @param job   the job that the client want to have executed
	 */
	public InitiateNewJobAnnouncement(final Agent agent, final ClientJob job) {
		this.myClientAgent = (ClientAgent) agent;
		this.job = job;
	}

	@Override
	public void action() {
		logger.info(SEND_INFORM_TO_SCHEDULER);
		final AID schedulerAgent = (AID) getParent().getDataStore().get(SCHEDULER_AGENT);
		myClientAgent.send(prepareJobAnnouncementMessage(schedulerAgent, job));
	}
}
